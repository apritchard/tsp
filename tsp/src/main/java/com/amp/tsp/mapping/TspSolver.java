package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public abstract class TspSolver {
	private static final Logger logger = Logger.getLogger(MapWrapper.class.getName());

	private final Set<Sector> sectors;
	private final Map<Sector, Map<Sector, Integer>> shortestPaths;
	
	private final List<TspNode> seeds;
	private final boolean useSeedsOnly;
	
	private TspNode bestPath;
	
	private final Map<CacheKey, List<Sector>> cachedRoutes = new HashMap<>();
	
	public Set<Sector> getSectors() {return sectors;}
	public Map<Sector, Map<Sector, Integer>> getShortestPaths() {return shortestPaths;}
	
	/**
	 * Creates a new map represented by the provided sectors and calculates the
	 * shortest paths between them.  Does not provide any seed paths.
	 * @param sectors
	 */
	public TspSolver(Set<Sector> sectors){
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
	public TspSolver(Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly){
		this.sectors = sectors;
		this.shortestPaths = TspUtilities.calculateShortestPathsStatic(sectors);
		this.useSeedsOnly = useSeedsOnly;
		
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
				try{
					bound += getDistance(path.get(i),path.get(i+1));
				} catch (Exception e){
					System.out.println("hmm");
					throw e;
				}
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
}
