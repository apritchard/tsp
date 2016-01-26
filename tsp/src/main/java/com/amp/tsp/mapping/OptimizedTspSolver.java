package com.amp.tsp.mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amp.tsp.mapping.TspSolution.TspBuilder;

/**
 * This solver uses a version of getBoundForPath that operates on
 * a byte[] instead of a List<Sector> for performance.
 * @author alex
 */
public abstract class OptimizedTspSolver extends TspSolver {
	//bounds optimization variables
	protected final Sector[] sectorList;
	protected final Map<Sector, Byte> sectorMap;
	protected final int numSectors;

	/**
	 * @see TspSolver#TspSolver(TspBuilder)
	 */
	public OptimizedTspSolver(TspBuilder builder){
		super(builder);
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
			usedSectors[path[0]] = true;
			int first, second;
			for(first = 0, second=1; second < numSectors ; first++, second++){
				if(path[second] == 0){
					break;
				}
				bound += getDistance(sectorList[path[first]], sectorList[path[second]]);
				usedSectors[path[second]] = true;
			}
			
			//if this is the complete path, we're done
			if(second == numSectors){
				return bound;
			}
			
			//then add the minimum distance out from each remaining nodes
			for(int i = 1; i <= numSectors ; i++){
				//if we've already traveled to this sector, skip it
				if(usedSectors[i]) continue;
				
				//otherwise, find the nearest sector we haven't visited
				int lowest = Integer.MAX_VALUE;
				for(int j = 1; j <= numSectors ; j++){
					//if you can't get from j to k, skip it
					if(!shortestPaths.get(sectorList[i]).containsKey(sectorList[j])) continue;
					//if we've visited it, skip it unless it's the last sector in our path
					if(usedSectors[j] && j != path[first]) continue;
					lowest = Math.min(shortestPaths.get(sectorList[j]).get(sectorList[i]), lowest);
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
