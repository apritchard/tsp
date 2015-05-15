package com.amp.tsp.mapping;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class BasicOptimizedTspSolver extends OptimizedTspSolver {

	/**
	 * @see TspSolver#TspSolver(Set)
	 */
	public BasicOptimizedTspSolver(Set<Sector> sectors) {
		super(sectors);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List, boolean)
	 */
	public BasicOptimizedTspSolver(Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly){
		super(sectors, seeds, useSeedsOnly);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List)
	 */
	public BasicOptimizedTspSolver(Set<Sector> sectors, List<Constraint> constraints){
		super(sectors, constraints);
	}

	/**
	 * Calculate shortest route using a single-threaded branch and bound
	 * algorithm.
	 * 
	 * @return The optimal path
	 */
	@Override
	public List<Sector> solve() {
		boolean[] usedSectors = new boolean[numSectors+1];
		Queue<TspNode2> queue = TspNode2.queueFrom(getInitialNodes(), sectorMap);
		
		//start with max bound and no best path
		int bound = Integer.MAX_VALUE;
		byte[] bestPath = null;
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
				logState(queue.size(), curr.getBound(), bestPath, longest.getPath());
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
			for(byte i = 0; i < curr.getLength(); i++){
				usedSectors[curr.getPath()[i]] = true;
			}
			for(byte i = 1; i <= numSectors; i++){
				if(!usedSectors[i]){
					byte[] newPath = Arrays.copyOf(curr.getPath(), numSectors);
					newPath[curr.getLength()] = i;
					int newBound = getBoundForPath(newPath, usedSectors);
					if(newBound <= bound){
						queue.add(new TspNode2(newBound, newPath, curr.getEnding(), curr.getLength() + 1));
					}
					
				}
			}
		}
		
		List<Sector> retList = TspUtilities.sectorList(bestPath, sectorList);
		return retList;
	}
}
