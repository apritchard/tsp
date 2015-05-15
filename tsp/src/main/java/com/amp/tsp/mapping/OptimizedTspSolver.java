package com.amp.tsp.mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class OptimizedTspSolver extends TspSolver {
	//bounds optimization variables
	protected final Sector[] sectorList;
	protected final Map<Sector, Byte> sectorMap;
	protected final int numSectors;
	
	/**
	 * @see TspSolver#TspSolver(Set)
	 */
	public OptimizedTspSolver(Set<Sector> sectors) {
		super(sectors);
		sectorList = new Sector[sectors.size() + 1];
		sectorMap = new HashMap<>();
		byte i = 1;
		for(Sector s : sectors){
			sectorMap.put(s, i);
			sectorList[i++] = s;
		}
		numSectors = sectors.size();
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List, boolean)
	 */
	public OptimizedTspSolver(Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly){
		super(sectors, seeds, useSeedsOnly);
		sectorList = new Sector[sectors.size() + 1];
		sectorMap = new HashMap<>();
		byte i = 1;
		for(Sector s : sectors){
			sectorMap.put(s, i);
			sectorList[i++] = s;
		}
		numSectors = sectors.size();
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List)
	 */
	public OptimizedTspSolver(Set<Sector> sectors, List<Constraint> constraints){
		super(sectors, constraints);
		sectorList = new Sector[sectors.size() + 1];
		sectorMap = new HashMap<>();
		byte i = 1;
		for(Sector s : sectors){
			sectorMap.put(s, i);
			sectorList[i++] = s;
		}
		numSectors = sectors.size();
	}
	
	/**
	 * Similar to {@link TspSolver#getBoundForPath(List)} but operates on
	 * a byte[] for performance. 
	 * @param path An array of sector ids in the order they have been visited
	 * @param usedSectors Swapspace array used to track which sectors have been visited. 
	 * 	 Does not need to be populated, but will be dirtied.
	 * @return
	 */
	public int getBoundForPath(final byte[] path, boolean[] usedSectors){
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
					lowest = Math.min(shortestPaths.get(sectorList[k]).get(sectorList[j]), lowest);
				}
				bound += lowest;
			}
			return bound;
		}
	}
	
	protected void logState(int queueSize, int currentBound, byte[] bestPath, byte[] longestPath){
		if(bestPath != null) {
			super.logState(queueSize, currentBound, TspUtilities.sectorList(bestPath, sectorList), null);
		} else {
			super.logState(queueSize, currentBound, null, TspUtilities.sectorList(longestPath, sectorList));
		}
	}
}
