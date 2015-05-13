package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

/**
 * Class containing information about the map to be solved. Currently contains
 * TSP logic as well, accessed via various method calls.
 * 
 * TODO Break different types of TSP solutions into their own classes
 * 
 * @author alex
 *
 */
public class MapWrapper {
	private static final Logger logger = Logger.getLogger(MapWrapper.class.getName());

	private final Set<Sector> sectors;
	private final Map<Sector, Map<Sector, Integer>> shortestPaths;
	
	private final List<TspNode> seeds; //each seed is a path used to initialize the search for an optimal path
	private boolean useSeedsOnly; //if true, all solutions considered will derive from seed paths
	
	private TspNode bestPath;
	
	private final Map<CacheKey, List<Sector>> cachedRoutes = new HashMap<>();
	
	//bounds optimization variables
	private final Sector[] sectorList;
	private final Map<Sector, Integer> sectorMap;
	private final boolean[] usedSectors; 
	private final int numSectors;
	
	public Set<Sector> getSectors() {return sectors;}
	public Map<Sector, Map<Sector, Integer>> getShortestPaths() {return shortestPaths;}
	
	/**
	 * Creates a new map represented by the provided sectors and calculates the
	 * shortest paths between them.  Does not provide any seed paths.
	 * @param sectors
	 */
	public MapWrapper(Set<Sector> sectors){
		this.sectors = sectors;
		this.shortestPaths = TspUtilities.calculateShortestPathsStatic(sectors);
		this.seeds = new ArrayList<>();
		useSeedsOnly = false;
		sectorList = new Sector[sectors.size() + 1];
		sectorMap = new HashMap<>();
		usedSectors = new boolean[sectors.size() +1];
		int i = 1;
		for(Sector s : sectors){
			sectorMap.put(s, i);
			sectorList[i++] = s;
		}
		numSectors = sectors.size();
	}
	
	/**
	 * Creates a new map represented by the provided sectors and calculates the
	 * shortest paths between them.  Uses the provided seed paths as starting points
	 * for possible optimal routes.
	 * @param sectors
	 * @param seeds
	 */
	public MapWrapper(Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly){
		this(sectors);
		this.useSeedsOnly = useSeedsOnly;
		
		for(List<Sector> seed: seeds){
			this.seeds.add(new TspNode(seed, getBoundForPath(seed)));
		}
	}
	
	/**
	 * Create a new map represented by the provided sectors and calculates the
	 * shortest path between them. Uses the provided constraints that specify both
	 * starting and ending points.
	 * @param sectors
	 * @param constraints
	 */
	public MapWrapper(Set<Sector> sectors, List<Constraint> constraints){
		this(sectors);
		this.useSeedsOnly = true;
		
		for(Constraint constraint : constraints){
			if(constraint.getStarting().isEmpty() && !constraint.getEnding().isEmpty()){
				//annoying - ending only, can't reverse because of asymmetry, so make 1 seed for each sector
				for(Sector s : sectors){
					List<Sector> l = new ArrayList<>();
					l.add(s);
					this.seeds.add(	new TspNode(l, getBoundForPath(l), constraint.getEnding()));
				}
			} else if (!constraint.getStarting().isEmpty() && constraint.getEnding().isEmpty()) {
				logger.info("Starting points but no ending");
				//if only a starting, then use the seed-only approach
				this.seeds.add(new TspNode(constraint.getStarting(), getBoundForPath(constraint.getStarting())));
			} else if(!constraint.getStarting().isEmpty() && !constraint.getEnding().isEmpty()){
				logger.info("Both starting and ending points");
				//both starting and ending constraint
				this.seeds.add(new TspNode(constraint.getStarting(), getBoundForPath(constraint.getStarting()), constraint.getEnding()));
			} else {
				//neither starting nor ending nodes, skip
				continue;
			}
		}
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Sector s : sectors){
			sb.append(s.toString());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	/**
	 * @return The shortest weighted distance between the two provided sectors
	 */
	public Integer getDistance(Sector s1, Sector s2){
		try{
			return shortestPaths.get(s1).get(s2);
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Creates the initial queue of paths. This queue consists of
	 * a single one-node path for each second, as well as any seeds
	 * that were provided on Map creation.
	 * 
	 * @return A queue of possible paths, ordered by their lower bound cost
	 */
	private Queue<TspNode> getInitialNodes(){
		PriorityQueue<TspNode> nodes;
		
		//if we've seeded this run, initialize the queue with the seeds
		if(seeds != null && seeds.size() > 0) {
			nodes = new PriorityQueue<>(seeds);
		} else {
			nodes = new PriorityQueue<>();
		}
		
		//return only the seeds if useSeedsOnly
		if(useSeedsOnly && !nodes.isEmpty()){
			return nodes;
		}
		
		//otherwise, create one starting node for each sector
		for(Sector s : sectors){
			List<Sector> l = new ArrayList<>();
			l.add(s);
			TspNode node = new TspNode(l, getBoundForPath(l));
			nodes.add(node);
		}
		return nodes;
		
	}
	
	/**
	 * Solve using a multi-threaded fork-join pool implementation. Uses
	 * a default depthThreshold.
	 * @return The optimal path
	 */
	public List<Sector> calcTspForkJoin(){
		//Performance seems fairly constant across depths, as long as >1
		int depthThreshold = 19;
		return calcTspForkJoin(depthThreshold);
	}
	
	/**
	 * Solve using a multi-threaded fork-join pool implementation.
	 * @param depthThreshold The path size at which to begin independent solutions in their own threads.
	 * @return The optimal path
	 */
	public List<Sector> calcTspForkJoin(int depthThreshold){
		ForkJoinPool fjp = new ForkJoinPool();
		fjp.invoke(new TspCalcAction(new PriorityBlockingQueue<TspNode>(getInitialNodes()), this, depthThreshold));
		return bestPath.getPath();
	}
	
	public List<Sector> calcTspMultiInt(){
		AtomicInteger bound = new AtomicInteger(Integer.MAX_VALUE);
		AtomicReference<int[]> bestPath = new AtomicReference<>();
		
		int numThreads = Runtime.getRuntime().availableProcessors() * 2;
		Queue<TspNode2> initialQueue = new PriorityBlockingQueue<>(TspNode2.queueFrom(getInitialNodes(), sectorMap));
		
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for(int i = 0; i < numThreads ; i++){
			TspCalculatorInt tspCalc = new TspCalculatorInt(bound, bestPath, initialQueue, this);
			executor.execute(tspCalc);
		}

		//Wait for them to finish
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.warning("Threads prematurely interrupted; program may not have finished.");
		}
		
		if(bestPath.get() == null){
//			logger.warning("No complete path found, returning longest path");
//			return longestPath.get();
			return new ArrayList<Sector>();
		} else {
			logger.info("Processing finished, returning best path.");
			return TspUtilities.sectorList(bestPath.get(), sectorList);
		}
		
	}
	
	/**
	 * Solve using a multi-threaded approach by creating two threads for each available
	 * processor and dividing the queue of paths amongst them.
	 * @return The optimal path
	 */
	public List<Sector> calcTspMulti(){
		//create atomic references for use across threads
		AtomicInteger bound = new AtomicInteger(Integer.MAX_VALUE);
		AtomicReference<List<Sector>> bestPath = new AtomicReference<>();
		AtomicReference<List<Sector>> longestPath = new AtomicReference<>();
		longestPath.set(new ArrayList<Sector>());

		int numThreads = Runtime.getRuntime().availableProcessors() * 2;
		Queue<TspNode> initialQueue = getInitialNodes();

		//Number of threads minimum of 2x processors or size of initial queue.
		numThreads = Math.min(numThreads, initialQueue.size());
		
		//Create one queue per thread
		List<Queue<TspNode>> queues = new ArrayList<>();
		for(int i = 0; i < numThreads; i++){
			queues.add(new PriorityQueue<TspNode>());
		}
		
		//Split the paths amongst the queues
		int q = 0;
		for(TspNode node : initialQueue){
			queues.get(q).add(node);
			q = (q + 1) % numThreads;
		}
		
		//Start all the calculator threads
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for(int i = 0; i < queues.size() ; i++){
			TspCalculator tspCalc = new TspCalculator(bound, bestPath, longestPath, queues.get(i), i, this);
			executor.execute(tspCalc);
		}
		
		//Wait for them to finish
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.warning("Threads prematurely interrupted; program may not have finished.");
		}
		
		if(bestPath.get() == null){
			logger.warning("No complete path found, returning longest path");
			return longestPath.get();
		} else {
			logger.info("Processing finished, returning best path.");
			return bestPath.get();
		}
		
	}
	
	public List<Sector> calcTspInt(){
		Queue<TspNode2> queue = TspNode2.queueFrom(getInitialNodes(), sectorMap);
		
		//start with max bound and no best path
		int bound = Integer.MAX_VALUE;
		int[] bestPath = null;
		TspNode2 longest = queue.peek();
		
		int count = 0;
		while(!queue.isEmpty()){
			TspNode2 curr = queue.poll();
			
			if(curr.getBound() > bound){
				logger.info("Searched all bounds less than " + bound + ", exiting");
				List<Sector> retList = TspUtilities.sectorList(bestPath, sectorList);
				return retList;
			}
			
			if(curr.getLength() > longest.getLength()){
				longest = curr;
			}
			
			if(count++ % 100000 == 0){
				StringBuilder sb = new StringBuilder();
				sb.append("Trace:").append(System.lineSeparator());
				sb.append("\tQueue size: ").append(queue.size()).append(System.lineSeparator());
				sb.append("\tCurrent bound: ").append(curr.getBound()).append(System.lineSeparator());
				if(bestPath != null) {
					sb.append("\tBest Complete Path: ").append(TspUtilities.routeString(TspUtilities.sectorList(bestPath, sectorList)));
				} else {
					sb.append("\tLongest Current Path: (" + longest.getLength() + "/" + sectors.size() + ") ").append(TspUtilities.routeString(TspUtilities.sectorList(longest.getPath(), sectorList)));
				}
				logger.info(sb.toString());				
			}
			
			//if the current path covers all sectors, it's a full path, so set it as our next best
			if(curr.getLength() == numSectors){
				if(curr.getBound() < bound) {
					logger.info("Cost " + curr.getBound() + " path found, saving");
					logger.info(TspUtilities.routeString(TspUtilities.sectorList(curr.getPath(), sectorList)));
					bestPath = curr.getPath();
					bound = curr.getBound();
				}
				continue;
			}
			
			//Add all next steps to queue (which will sort them by bound)
			Arrays.fill(usedSectors, false);
			for(int i = 0; i < curr.getLength(); i++){
				usedSectors[curr.getPath()[i]] = true;
			}
			for(int i = 1; i <= numSectors; i++){
				if(!usedSectors[i]){
					int[] newPath = Arrays.copyOf(curr.getPath(), numSectors);
					newPath[curr.getLength()] = i;
					int newBound = getBoundForPath(newPath);
					if(newBound <= bound){
						queue.add(new TspNode2(newBound, newPath, curr.getEnding(), curr.getLength() + 1));
					}
					
				}
			}
		}
		
		List<Sector> retList = TspUtilities.sectorList(bestPath, sectorList);
		return retList;
	}
	
	/**
	 * Calculate shortest route using a single-threaded branch and bound algorithm.
	 * @return The optimal path
	 */
	public List<Sector> calcTsp(){
		Queue<TspNode> queue = getInitialNodes();
		
		//start with max bound and no best path
		int bound = Integer.MAX_VALUE;
		List<Sector> bestPath = null;
		
		int i = 0;
		List<Sector> longestPath = new ArrayList<>(); //this is just to view partial progress in logging
		while(!queue.isEmpty()){
			TspNode curr = queue.poll();
			
			if(curr.getPath().size() > longestPath.size()){
				longestPath = curr.getPath();
			}
			
			//TODO Extract logging code from individual implementations
			if(i++%10000 == 0){
				StringBuilder sb = new StringBuilder();
				sb.append("Trace:").append(System.lineSeparator());
				sb.append("\tQueue size: ").append(queue.size()).append(System.lineSeparator());
				sb.append("\tCurrent bound: ").append(curr.getBound()).append(System.lineSeparator());
				if(bestPath != null) {
					sb.append("\tBest Complete Path: ").append(bestPath);
				} else {
					sb.append("\tLongest Current Path: (" + longestPath.size() + "/" + sectors.size() + ") ").append(longestPath);
				}
				logger.info(sb.toString());
			}
			
			//we're not going to have anything better than our current at this point, so return 
			if(curr.getBound() > bound){
				logger.info("Searched all bounds less than " + bound + ", exiting");
				return bestPath;
			}
			
			//if the current path covers all sectors, it's a full path, so set it as our new best
			if(curr.getPath().size() == sectors.size() && curr.getBound() < bound) {
				logger.info("Cost " + curr.getBound() + " path found, saving");
				logger.info(TspUtilities.routeString(curr.getPath()));
				bestPath = curr.getPath();
				bound = curr.getBound();
				continue;
			}
			
			//TODO investigate dynamic programming-style caching further

			//Add all next steps to queue (which will sort them by bound)
			Set<Sector> unvisited = new HashSet<>(sectors);
			unvisited.removeAll(curr.getPath());
			
			for(Sector s : unvisited){
				List<Sector> newPath = new ArrayList<>(curr.getPath());
				newPath.add(s);
				TspNode newNode = new TspNode(newPath, getBoundForPath(newPath));
				if(newNode.getBound() <= bound){
					queue.add(newNode);
				}
			}
		}
		
		//if queue is empty and we haven't returned, then either we found no complete paths
		// (bestPath will be null), or the very last path we checked is the best path
		// (unlikely, but possible), in which case return it.
		return bestPath;
	}
	
	public int getBoundForPath(final int[] path){
		return getBoundForPathThreadSafe(path, usedSectors);
	}
	
	public int getBoundForPathThreadSafe(final int[] path, boolean[] usedSectors){
		int bound = 0;
		
		if(path.length == 1 || path[1] == 0 ){
			return bound;
		} else {
			//bound = cost of current steps + minimum edge from each unvisited node
			
			Arrays.fill(usedSectors, false);
			
			//sum the cost of each step so far and populate usedSectors
			int i = 0;
			usedSectors[path[i]] = true;
			while(i+1 < numSectors && path[i] > 0 && path[i+1] > 0){
				bound += getDistance(sectorList[path[i]], sectorList[path[i+1]]);
				usedSectors[path[i+1]] = true;
//				logger.info(sectorList[path[i]] + " to " + sectorList[path[i+1]] + ", total Bound: " + bound);
				i++;
			}
			
			//if this is the complete path, we're done
			if(i+1 == numSectors){
				return bound;
			}
			
			//then add the minimum distance out from each remaining nodes
			for(int j = 1; j <= numSectors ; j++){
				//if we've already traveled to this sector, skip it
				if(usedSectors[j]) continue;
				
				//otherwise, find the nearest sector we haven't visited
				int lowest = Integer.MAX_VALUE;
				for(int k = 1; k <= numSectors ; k++){
					//if you can't get from j to k, skip it
					if(!shortestPaths.get(sectorList[j]).containsKey(sectorList[k])) continue;
					//if we've visited it, skip it unless it's the last sector in our path
					if(usedSectors[k] && k != path[i]) continue;
//					logger.info("saving lower of " + sectorList[k] + " to " + sectorList[j]+ "(" + shortestPaths.get(sectorList[k]).get(sectorList[j]) +  ") and " + lowest);
					lowest = Math.min(shortestPaths.get(sectorList[k]).get(sectorList[j]), lowest);
				}
				bound += lowest;
//				logger.info("bound: " + bound);
			}
//			logger.info("Final bound for " + TspUtilities.routeString(TspUtilities.sectorList(path, sectorList)) + ": " + bound);
			return bound;
		}
	}
	
	/**
	 * Calculate and return the lower bound for the cost of the provided
	 * path on this particular map.
	 * @param path Partial or complete path for which to calculate the lower bound.
	 * @return
	 */
	public int getBoundForPath(List<Sector> path){
		int bound = 0;
		
		if(path.size() == 1){
			return bound; 
		} else {
			//bound = cost of current steps + minimum edge from each unvisited node
			
			//sum the cost of each step so far
			int steps = path.size() - 1;
			for(int i = 0; i < steps; i++){
				bound += getDistance(path.get(i),path.get(i+1));
//				logger.info(path.get(i) + " to " + path.get(i+1) + ", total Bound: " + bound);
			}
			
			//if this is the complete path, we're done
			if(path.size() == shortestPaths.size()){
				return bound;
			}
			
			//then add the minimum distance out from each remaining nodes
			for(Sector s1 : shortestPaths.keySet()){
				//if we've already traveled to this sector, skip it
				if(path.contains(s1)) continue;
				
				//otherwise, find the nearest sector we haven't visited
				int lowest = Integer.MAX_VALUE;
				for(Sector s2 : shortestPaths.get(s1).keySet()){
					//if we've visited it, skip it unless it's the last sector in our path
					if(!s2.equals(path.get(path.size()-1)) && path.contains(s2)) continue;
//					logger.info("saving lower of " + s2 + " to " + s1 + "(" + shortestPaths.get(s2).get(s1) +  ") and " + lowest);
					lowest = Math.min(shortestPaths.get(s2).get(s1), lowest);
				}
				bound += lowest; 
//				logger.info("bound: " + bound);
			}
//			logger.info("Final bound for " + TspUtilities.routeString(path) + ": " + bound);
			return bound;
		}
	}
	
	/**
	 * If the provided path has a lower bound than the current best path,
	 * sets the best path to the provided path.
	 * 
	 * Used to update MapWrapper from external threads.
	 * @param path
	 */
	public synchronized void setPossibleBestPath(TspNode path){
		if(bestPath == null	|| path.getBound() < bestPath.getBound()){
			bestPath = path;
		} 
	}
	
	/**
	 * Synchronized for use by external threads.
	 * 
	 * @return the bound of the current best path for this map.
	 */
	public synchronized int getBound(){
		return bestPath == null? Integer.MAX_VALUE : bestPath.getBound();
	}
}
