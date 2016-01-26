package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.amp.tsp.mapping.TspSolution.TspBuilder;

public class NearestNeighborSolver extends TspSolver {

	private Map<Sector, List<Sector>> nearestNeighbors;
	private int nNearest;
	
	/**
	 * @see TspSolver#TspSolver(TspBuilder)
	 */
	public NearestNeighborSolver(TspBuilder builder) {
		super(builder);
	}

	
	public NearestNeighborSolver(int n, TspBuilder builder) {
		super(builder);
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
			
			nearestNeighbors.put(s, sectorList);
		});
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

		Queue<TspNode> queue = getInitialNodes();

		// start with max bound and no best path
		int bound = Integer.MAX_VALUE;
		List<Sector> bestPath = null;

		int i = 0;
		List<Sector> longestPath = new ArrayList<>(); // this is just to view
														// partial progress in
														// logging
		while (!queue.isEmpty()) {
			TspNode curr = queue.poll();

			if (curr.getPath().size() > longestPath.size()) {
				longestPath = curr.getPath();
			}

			if (i++ % 10000 == 0) {
				logState(queue.size(), curr.getBound(), bestPath, longestPath);
			}

			// we're not going to have anything better than our current at this point, so return
			if (curr.getBound() > bound) {
				logger.info("Searched all bounds less than " + bound
						+ ", exiting");
				return bestPath;
			}

			// if the current path covers all sectors, it's a full path, so set it as our new best
			if (curr.getPath().size() == sectors.size()
					&& curr.getBound() < bound) {
				logger.info("Cost " + curr.getBound() + " path found, saving");
				logger.info(TspUtilities.routeString(curr.getPath()));
				bestPath = curr.getPath();
				bound = curr.getBound();
				continue;
			}

			// Add all next steps to queue (which will sort them by bound)
			List<Sector> currPath = curr.getPath();
			Set<Sector> unvisited = new LinkedHashSet<>(nearestNeighbors.get(currPath.get(currPath.size()-1)));
			unvisited.removeAll(curr.getPath());
			
			int cnt = 0;
			for (Sector s : unvisited) {
				List<Sector> newPath = new ArrayList<>(curr.getPath());
				newPath.add(s);
				TspNode newNode = new TspNode(newPath, getBoundForPath(newPath));
				if (newNode.getBound() <= bound) {
					queue.add(newNode);
				}
				if (++cnt == nNearest){
					break;
				}
			}
		}

		// if queue is empty and we haven't returned, then either we found no complete paths
		// (bestPath will be null), or the very last path we checked is the best path
		// (unlikely, but possible), in which case return it.
		return bestPath;
	}
}
