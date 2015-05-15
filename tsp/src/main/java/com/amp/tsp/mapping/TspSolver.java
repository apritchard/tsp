package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

public abstract class TspSolver {
	protected final Logger logger = Logger.getLogger(TspSolver.class.getName()); 
	protected final Set<Sector> sectors;
	protected final Map<Sector, Map<Sector, Integer>> shortestPaths;
	
	protected  final List<TspNode> seeds; //each seed is a path used to initialize the search for an optimal path
	protected  boolean useSeedsOnly; //if true, all solutions considered will derive from seed paths
	
	public Set<Sector> getSectors() {return sectors;}
	public Map<Sector, Map<Sector, Integer>> getShortestPaths() {return shortestPaths;}
	
	public abstract List<Sector> solve();
	
	/**
	 * Creates a new map represented by the provided sectors and calculates the
	 * shortest paths between them.  Does not provide any seed paths.
	 * @param sectors
	 */
	protected TspSolver(Set<Sector> sectors){
		this.sectors = sectors;
		this.shortestPaths = TspUtilities.calculateShortestPathsStatic(sectors);
		this.seeds = new ArrayList<>();
		useSeedsOnly = false;
	}	
	
	/**
	 * Creates a new map represented by the provided sectors and calculates the
	 * shortest paths between them.  Uses the provided seed paths as starting points
	 * for possible optimal routes.
	 * @param sectors
	 * @param seeds
	 */
	protected TspSolver(Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly){
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
	protected TspSolver(Set<Sector> sectors, List<Constraint> constraints){
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
				//if only a starting, then use the seed-only approach
				this.seeds.add(new TspNode(constraint.getStarting(), getBoundForPath(constraint.getStarting())));
			} else if(!constraint.getStarting().isEmpty() && !constraint.getEnding().isEmpty()){
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
	 * Creates the initial queue of paths. This queue consists of
	 * a single one-node path for each second, as well as any seeds
	 * that were provided on Map creation.
	 * 
	 * @return A queue of possible paths, ordered by their lower bound cost
	 */
	protected Queue<TspNode> getInitialNodes(){
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
					lowest = Math.min(shortestPaths.get(s2).get(s1), lowest);
				}
				bound += lowest; 
			}
			return bound;
		}
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
	
	protected void logState(int queueSize, int currentBound, List<Sector> bestPath, List<Sector> longestPath){
		StringBuilder sb = new StringBuilder();
		sb.append("Trace:").append(System.lineSeparator());
		sb.append("\tQueue size: ").append(queueSize).append(System.lineSeparator());
		sb.append("\tCurrent bound: ").append(currentBound).append(System.lineSeparator());
		if(bestPath != null) {
			sb.append("\tBest Complete Path: ").append(TspUtilities.routeString(bestPath));
		} else {
			sb.append("\tLongest Current Path: (" + longestPath.size() + "/" + sectors.size() + ") ").append(TspUtilities.routeString(longestPath));
		}
		logger.info(sb.toString());		
	}
}
