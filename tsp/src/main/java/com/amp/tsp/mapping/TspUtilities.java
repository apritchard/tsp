package com.amp.tsp.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class TspUtilities {
	private final static Logger logger = Logger.getLogger(TspUtilities.class.getName());
	
	/**
	 * Calculates the distance from all Sectors to all other Sectors
	 * 
	 * @param sectors A Set of connected Sectors
	 * @return Map in which map.get(s1).get(s2) returns the distance between s1 and s2
	 */
	public static Map<Sector, Map<Sector, Integer>> calculateShorestPaths(Set<Sector> sectors){
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
	
	public static String routeString(List<Sector> route){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < route.size() -1; i++){
			sb.append(route.get(i)).append(", ");
		}
		sb.append(route.get(route.size()-1));
		return sb.toString();
	}

}
