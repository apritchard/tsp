package com.amp.tsp.mapping;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

public class TspUtilities {
	private final static Logger logger = Logger.getLogger(TspUtilities.class.getName());
	
	
	/**
	 * Version of shortest paths that does not allow for passing through other
	 * nodes to get to your destination. Used to allow asymmetric warp point relationships
	 * @param sectors
	 * @return
	 */
	public static Map<Sector, Map<Sector, Integer>> calculateShortestPathsStatic(Set<Sector> sectors){
		Map<Sector, Map<Sector, Integer>> shortestPaths = new HashMap<>();
		
		int max = 0;
		for(Sector s1 : sectors){
			max = Math.max(max, Collections.max(s1.getEdgeList().values()));
		}
		for(Sector s1 : sectors){
			shortestPaths.put(s1,  s1.getEdgeList());
			for(Sector s2 : sectors){
				if(s1.equals(s2)){ 
					shortestPaths.get(s1).put(s2, 0);
				} else if(!s1.getEdgeList().containsKey(s2)){
					shortestPaths.get(s1).put(s2, max+1); 
				}
			}
		}
		
		//remove path to self
		for(Sector s1 : sectors){
			shortestPaths.get(s1).remove(s1);
		}
		return shortestPaths;
	}
	
	/**
	 * Calculates the distance from all Sectors to all other Sectors
	 * 
	 * @param sectors A Set of connected Sectors
	 * @return Map in which map.get(s1).get(s2) returns the distance between s1 and s2
	 */
	public static Map<Sector, Map<Sector, Integer>> calculateShortestPaths(Set<Sector> sectors){
		Map<Sector, Map<Sector, Integer>> shortestPaths = new HashMap<>();

		//initialize shortest paths with neighboring edges, 0 for self, and max_value for everything else
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
		
		return shortestPaths;
	}
	
	
	/**
	 * Save each ordered list of Sectors as the current best subpaths following a given Sector.
	 * For example, if the currentBestPath is A,B,C,D, we will save BCD as the path following A,
	 * CD as a path following B, and D as a path following C.  
	 * @param cache The subpath cache
	 * @param currentBestPath A complete path representing the current best path
	 */
	public static void cachePaths(Map<CacheKey, List<Sector>> cache, List<Sector> currentBestPath){
		int size = currentBestPath.size();
		logger.info("Caching " + routeString(currentBestPath));
		for(int i = 1; i < size; i++){
			//get the last i sectors in the path and cache them
			List<Sector> cachedValue = currentBestPath.subList(size-i, size);
			CacheKey cachedKey = new CacheKey(currentBestPath.get(i), new HashSet<>(cachedValue));
			logger.info("Best solution to " + cachedKey + ": " + routeString(cachedValue));
			if(!cache.containsKey(cachedKey)){
				cache.put(cachedKey, cachedValue);
			}
		}
	}
	
	/**
	 * Returns a readable string representation of a route
	 * @param route
	 * @return
	 */
	public static String routeString(List<Sector> route){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < route.size() -1; i++){
			sb.append(route.get(i)).append(", ");
		}
		sb.append(route.get(route.size()-1));
		return sb.toString();
	}
	
	public static Set<Sector> pointsToSectors(Map<String, Point> points, List<String> warpPoints){
		final int WARP_TIME = 20;
		Set<Sector> sectors = new HashSet<>();
		for(Entry<String, Point> entry : points.entrySet()){
			sectors.add(new Sector(entry.getKey()));
		}
		for(Sector s : sectors){
			for(Sector s2 : sectors){
				if(s.equals(s2)){
					continue;
				}
				int distance;
				if(warpPoints.contains(s2.getName())){
					distance = WARP_TIME;
				} else {
					distance =(int)points.get(s.getName()).distance(points.get(s2.getName())); 
				}
				s.addEdge(s2, distance);
			}
		}
		return sectors;
	}
	
	public static List<List<Sector>> stringsToSeeds(List<String> strings, Set<Sector> sectors){
		List<List<Sector>> seeds = new ArrayList<>();
		Map<Sector, Sector> lookup = new HashMap<>();
		for(Sector s : sectors){
			lookup.put(s, s);
		}
		for(String seed : strings){
			List<Sector> l = new ArrayList<>();
			l.add(lookup.get(new Sector(seed)));
			seeds.add(l);
		}
		return seeds;
	}
	
	public static List<Constraint> stringsToConstraints(List<String> startingPoints, List<String> endingPoints, Set<Sector> sectors){
		List<Constraint> constraints = new ArrayList<>();
		Map<Sector, Sector> lookup = new HashMap<>();
		for(Sector s : sectors){
			lookup.put(s, s);
		}
		List<Sector> startSeed = new ArrayList<>();
		if(startingPoints != null){
			for(String seed : startingPoints){
				startSeed.add(lookup.get(new Sector(seed)));
			}
		}
		List<Sector> endSeed = new ArrayList<>();
		if(endingPoints != null){
			for(String seed : endingPoints){
				endSeed.add(lookup.get(new Sector(seed)));
			}
		}
		
		Constraint c = new Constraint(startSeed, endSeed);
		constraints.add(c);
		return constraints;		
	}


	public static List<Sector> sectorList(int[] bestPath, Sector[] sectorList) {
		List<Sector> sectors = new ArrayList<>();
		for(int i = 0; i < bestPath.length; i++){
			if(sectorList[bestPath[i]] != null){
				sectors.add(sectorList[bestPath[i]]);
			}
		}
		return sectors;
	}

}
