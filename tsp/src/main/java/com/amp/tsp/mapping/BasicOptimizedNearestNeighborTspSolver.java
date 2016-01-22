package com.amp.tsp.mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class BasicOptimizedNearestNeighborTspSolver extends OptimizedTspSolver {

	private Map<Byte, byte[]> nearestNeighbors;
	private int nNearest;
	
	/**
	 * @see TspSolver#TspSolver(Set)
	 */
	public BasicOptimizedNearestNeighborTspSolver(Set<Sector> sectors) {
		super(sectors);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List, boolean)
	 */
	public BasicOptimizedNearestNeighborTspSolver(Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly){
		super(sectors, seeds, useSeedsOnly);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List)
	 */
	public BasicOptimizedNearestNeighborTspSolver(Set<Sector> sectors, List<Constraint> constraints){
		super(sectors, constraints);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set)
	 */
	public BasicOptimizedNearestNeighborTspSolver(int n, Set<Sector> sectors) {
		super(sectors);
		setNNearest(n);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List, boolean)
	 */
	public BasicOptimizedNearestNeighborTspSolver(int n, Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly){
		super(sectors, seeds, useSeedsOnly);
		setNNearest(n);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List)
	 */
	public BasicOptimizedNearestNeighborTspSolver(int n, Set<Sector> sectors, List<Constraint> constraints){
		super(sectors, constraints);
		setNNearest(n);
	}
	
	private void orderNeighbors(){
		nearestNeighbors = new HashMap<>();
		sectors.stream().forEach(s -> {
			
			List<Sector> sectorList = sectors.stream()
				.filter(s1 -> !s1.equals(s))
				.filter(s1 -> shortestPaths.get(s).containsKey(s1))
				.sorted((s1, s2) -> shortestPaths.get(s).get(s1).compareTo(shortestPaths.get(s).get(s2)))
				.collect(Collectors.toList());
			
			nearestNeighbors.put(sectorMap.get(s), sectorListToByteArray(sectorList));
		});
	}
	
	private byte[] sectorListToByteArray(List<Sector> sectors){
		byte[] array = new byte[sectors.size()];
		int i = 0;
		for(Sector s : sectors){
			array[i++] = sectorMap.get(s);
		}
		return array;
	}
	
	
	private void setNNearest(int n){
		this.nNearest = n;
	}		

	/**
	 * Calculate shortest route using a single-threaded branch and bound
	 * algorithm.
	 * 
	 * @return The optimal path
	 */
	@Override
	public List<Sector> solve() {
		if(nearestNeighbors == null){
			orderNeighbors();
		}
		if(nNearest < 1){
			nNearest = 1;
		}
		if(nNearest > sectors.size()){
			nNearest = sectors.size();
		}		
		
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
			
			byte[] currPath = curr.getPath();
			byte[] nearest = nearestNeighbors.get(currPath[curr.getLength()-1]);
			int cnt = 0;
			
			//Add all next steps to queue (which will sort them by bound)
			Arrays.fill(usedSectors, false);
			for(byte i = 0; i < curr.getLength(); i++){
				usedSectors[curr.getPath()[i]] = true;
			}
			for(byte i : nearest)
				if(!usedSectors[i]){
					byte[] newPath = Arrays.copyOf(curr.getPath(), numSectors);
					newPath[curr.getLength()] = i;
					int newBound = getBoundForPath(newPath, usedSectors);
					if(newBound <= bound){
						queue.add(new TspNode2(newBound, newPath, curr.getEnding(), curr.getLength() + 1));
					}
					if(++cnt == nNearest){
						break;
				}
			}
		}
		
		List<Sector> retList = TspUtilities.sectorList(bestPath, sectorList);
		return retList;
	}
}
