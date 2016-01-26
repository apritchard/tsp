package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.amp.tsp.mapping.TspSolution.TspBuilder;

public class BasicTspSolver extends TspSolver {

	/**
	 * @see TspSolver#TspSolver(TspBuilder)
	 */
	public BasicTspSolver(TspBuilder builder){
		super(builder);
	}

	/**
	 * Calculate shortest route using a single-threaded branch and bound
	 * algorithm.
	 * 
	 * @return The optimal path
	 */
	@Override
	public List<Sector> solve() {

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
			Set<Sector> unvisited = new HashSet<>(sectors);
			unvisited.removeAll(curr.getPath());

			for (Sector s : unvisited) {
				List<Sector> newPath = new ArrayList<>(curr.getPath());
				newPath.add(s);
				TspNode newNode = new TspNode(newPath, getBoundForPath(newPath));
				if (newNode.getBound() <= bound) {
					queue.add(newNode);
				}
			}
		}

		// if queue is empty and we haven't returned, then either we found no complete paths
		// (bestPath will be null), or the very last path we checked is the best path
		// (unlikely, but possible), in which case return it.
		return bestPath;
	}
}
