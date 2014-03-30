package com.amp.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import com.amp.parse.MapParser;

public class MapWrapper {
	private static final Logger logger = Logger.getLogger(MapWrapper.class.getName());

	private Set<Sector> sectors;
	private Map<Sector, Map<Sector, Integer>> shortestPaths;
	private List<TspNode> seeds;
	
	public MapWrapper(Set<Sector> sectors){
		this.sectors = sectors;
		seeds = new ArrayList<>();
		
		calcShortestPaths();
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
	
	/**
	 * Calculates the shortest path from all sectors to all other sectors 
	 * using floyd-warshall algorithm.
	 */
	private void calcShortestPaths(){
		shortestPaths = new HashMap<>();

		//initialize shortest paths with neighbor values, 0 for self, and max_value for everything else
		for(Sector s1 : sectors){
			shortestPaths.put(s1, s1.getEdgeList());
			for(Sector s2 : sectors){
				if(s1.equals(s2)){ 
					shortestPaths.get(s1).put(s2, 0);
				} else if(!s1.getEdgeList().containsKey(s2)){
					shortestPaths.get(s1).put(s2, Integer.MAX_VALUE); 
				}
			}
		}
		
		//floyd-warshall to update shortest paths
		for(Sector sk : sectors){
			for(Sector si : sectors) {
				for(Sector sj : sectors){
					int i2j = shortestPaths.get(si).get(sj);
					int i2k = shortestPaths.get(si).get(sk);
					int k2j = shortestPaths.get(sk).get(sj);
					int i2k2j = i2k + k2j;
					if(i2k2j < 0) {
						i2k2j = Integer.MAX_VALUE; //deal with overflow
					}
					if(i2j > i2k2j){
						shortestPaths.get(si).put(sj, i2k2j);
					}
				}
			}
		}
		
		//remove path to self
		for(Sector s1 : sectors){
			shortestPaths.get(s1).remove(s1);
		}
	}
	
	public List<Sector> calcTsp(){
		Queue<TspNode> queue = new PriorityQueue<>();
		
		//start with adding a path beginning at each sector
		for(Sector s : sectors){
			List<Sector> l = new ArrayList<>();
			l.add(s);
			queue.add(new TspNode(l));
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
				logger.fine(sb.toString());
			}
			
			//we're not going to have anything better than our current at this point, so return 
			if(curr.getBound() > bound){
				logger.info("Searched all bounds less than " + bound + ", exiting");
				return bestPath;
			}
			
			//if the current path covers all sectors, it's a full path, so set it as our new best
			if(curr.getPath().size() == sectors.size() && curr.getBound() < bound) {
				logger.info("Cost " + curr.getBound() + " path found, saving");
				logger.info(routeString(curr.getPath()));
				bestPath = curr.getPath();
				bound = curr.getBound();
				continue;
			}
			
			Set<Sector> unvisited = new HashSet<>(sectors);
			unvisited.removeAll(curr.getPath());
			for(Sector s : unvisited){
				List<Sector> newPath = new ArrayList<>(curr.getPath());
				newPath.add(s);
				TspNode newNode = new TspNode(newPath);
				if(newNode.getBound() <= bound){
					queue.add(newNode);
				}
			}
		}
		

		return null;
	}
	

	/**
	 * Wrapper for a path that calculates its bound on construction
	 * and is comparable so that it has a natural ordering for priority queuing
	 */
	private class TspNode implements Comparable<TspNode>{

		private int bound = 0;
		private List<Sector> path;
		
		/**
		 * @param path A list of sectors traveled so far, with the 
		 * 		  path[0] representing the first sector to visit
		 */
		public TspNode(List<Sector> path){
			this.path = path;
			
			//Immediately calculate the bound for this path
			if(path.size() == 1){
				bound = 0;  //don't bother to calculate bound for starting nodes 
			} else {
				//bound = cost of current steps + minimum edge from each unvisited node
				
				//sum the cost of each step so far
				int steps = path.size() - 1;
				for(int i = 0; i < steps; i++){
					bound += getDistance(path.get(i),path.get(i+1));
				}
				
				//if this is the complete path, we're done
				if(path.size() == shortestPaths.size()){
					return;
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
			}
		}
		
		/**
		 * @return The bound of the path defined by this node
		 */
		public int getBound(){
			return bound;
		}
		
		/**
		 * @return The sectors traveled, in order from first to last.
		 */
		public List<Sector> getPath(){
			return path;
		}
		
		@Override
		public int compareTo(TspNode other) {
			return this.bound - other.bound;
		}

	}
	
	private static String routeString(List<Sector> route){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < route.size() -1; i++){
			sb.append(route.get(i)).append(", ");
		}
		sb.append(route.get(route.size()-1));
		return sb.toString();
	}

}
