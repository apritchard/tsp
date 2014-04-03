package com.amp.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

public class MapWrapper {
	private static final Logger logger = Logger.getLogger(MapWrapper.class.getName());

	private Set<Sector> sectors;
	private Map<Sector, Map<Sector, Integer>> shortestPaths;
	
	public Set<Sector> getSectors() {return sectors;}
	public Map<Sector, Map<Sector, Integer>> getShortestPaths() {return shortestPaths;}
	
	private List<TspNode> seeds;
	
	private TspNode bestPath;
	
	public MapWrapper(Set<Sector> sectors, List<List<Sector>> seeds){
		this.sectors = sectors;
		this.shortestPaths = TspUtilities.calculateShorestPaths(sectors);
		
		this.seeds = new ArrayList<>();
		for(List<Sector> seed: seeds){
			this.seeds.add(new TspNode(seed, getBoundForPath(seed)));
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
		return shortestPaths.get(s1).get(s2);
	}
	
	private Queue<TspNode> getInitialNodes(){
		//if we've seeded this run, just return the seeds
		if(seeds != null && seeds.size() > 0) {
			return new PriorityQueue<>(seeds);
		}
		
		//otherwise, create one starting node for each sector
		PriorityQueue<TspNode> nodes = new PriorityQueue<>();
		for(Sector s : sectors){
			List<Sector> l = new ArrayList<>();
			l.add(s);
			TspNode node = new TspNode(l, getBoundForPath(l));
			nodes.add(node);
		}
		return nodes;
		
	}
	
	public List<Sector> calcTspForkJoin(){
		ForkJoinPool fjp = new ForkJoinPool();
		
		fjp.invoke(new TspCalcAction(new PriorityBlockingQueue<TspNode>(getInitialNodes()), this, 20));
		return bestPath.getPath();
	}
	
	public List<Sector> calcTspMulti(){
		AtomicInteger bound = new AtomicInteger(Integer.MAX_VALUE);
		AtomicReference<List<Sector>> bestPath = new AtomicReference<>();
		AtomicReference<List<Sector>> longestPath = new AtomicReference<>();
		longestPath.set(new ArrayList<Sector>());

		int numThreads = Runtime.getRuntime().availableProcessors() * 2;
		
		if(seeds != null && seeds.size() > 0){
			numThreads = Math.min(numThreads, seeds.size());
		} else {
			numThreads = Math.min(numThreads,  sectors.size()); 
		}
		
		List<Queue<TspNode>> queues = new ArrayList<>();
		for(int i = 0; i < numThreads; i++){
			queues.add(new PriorityQueue<TspNode>());
		}
		
		int q = 0;
		if(seeds != null && seeds.size() > 0){
			logger.info(seeds.size() + " seeds found; initializing search space.");
			for(TspNode seed : seeds){
				queues.get(q).add(seed);
				q = (q + 1) % numThreads;
			}
		} else {
			logger.info("No seeds found, initializing with all single sectors.");
			for(Sector s : sectors){
				List<Sector> l = new ArrayList<>();
				l.add(s);
				queues.get(q).add(new TspNode(l, getBoundForPath(l)));
				q = (q + 1) % numThreads;
			}
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for(int i = 0; i < queues.size() ; i++){
			TspCalculator tspCalc = new TspCalculator(bound, bestPath, longestPath, queues.get(i), i, this);
			executor.execute(tspCalc);
		}
		
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
	
	public List<Sector> calcTsp(){
		Queue<TspNode> queue = new PriorityQueue<>();
		
		//start with adding a path beginning at each sector
		for(Sector s : sectors){
			List<Sector> l = new ArrayList<>();
			l.add(s);
			queue.add(new TspNode(l, getBoundForPath(l)));
		}
		
		//add the seeds
		for(TspNode seed : seeds){
			logger.info("Adding seed with bound " + seed.getBound());
			queue.add(seed);
		}
		
		int bound = Integer.MAX_VALUE;
		List<Sector> bestPath = null;
		
		int i = 0;
		List<Sector> longestPath = new ArrayList<>();
		while(!queue.isEmpty()){
			TspNode curr = queue.poll();
			
			if(curr.getPath().size() > longestPath.size()){
				longestPath = curr.getPath();
			}
			
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
		

		return null;
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
					
					lowest = Math.min(shortestPaths.get(s1).get(s2), lowest);
				}
				bound += lowest; 
			}
			return bound;
		}
	}
	
	public synchronized void setPossibleBestPath(TspNode path){
		if(bestPath == null	|| path.getBound() < bestPath.getBound()){
			bestPath = path;
		} 
	}
	
	public synchronized int getBound(){
		return bestPath == null? Integer.MAX_VALUE : bestPath.getBound();
	}
}
