package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MultiTspSolver extends TspSolver {
	
	private final TspSolver theSolver = this; //for synchronization

	/**
	 * @see TspSolver#TspSolver(Set)
	 */
	public MultiTspSolver(Set<Sector> sectors) {
		super(sectors);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List, boolean)
	 */
	public MultiTspSolver(Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly){
		super(sectors, seeds, useSeedsOnly);
	}
	
	/**
	 * @see TspSolver#TspSolver(Set, List)
	 */
	public MultiTspSolver(Set<Sector> sectors, List<Constraint> constraints){
		super(sectors, constraints);
	}	

	/**
	 * Calculate shortest route using a multi-threaded branch and bound algorithm.
	 * 
	 * @return The optimal path
	 */
	@Override
	public List<Sector> solve() {

		// create atomic references for use across threads
		AtomicInteger bound = new AtomicInteger(Integer.MAX_VALUE);
		AtomicReference<List<Sector>> bestPath = new AtomicReference<>();
		AtomicReference<List<Sector>> longestPath = new AtomicReference<>();
		longestPath.set(new ArrayList<Sector>());

		int numThreads = Runtime.getRuntime().availableProcessors() * 2;
		Queue<TspNode> initialQueue = getInitialNodes();

		// Number of threads minimum of 2x processors or size of initial queue.
		numThreads = Math.min(numThreads, initialQueue.size());

		// Create one queue per thread
		List<Queue<TspNode>> queues = new ArrayList<>();
		for (int i = 0; i < numThreads; i++) {
			queues.add(new PriorityQueue<TspNode>());
		}

		// Split the paths amongst the queues
		int q = 0;
		for (TspNode node : initialQueue) {
			queues.get(q).add(node);
			q = (q + 1) % numThreads;
		}

		// Start all the calculator threads
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < queues.size(); i++) {
			TspCalculator tspCalc = new TspCalculator(bound, bestPath,
					longestPath, queues.get(i), i);
			executor.execute(tspCalc);
		}

		// Wait for them to finish
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.warn("Threads prematurely interrupted; program may not have finished.");
		}

		if (bestPath.get() == null) {
			logger.warn("No complete path found, returning longest path");
			return longestPath.get();
		} else {
			logger.info("Processing finished, returning best path.");
			return bestPath.get();
		}
	}
	
	/**
	 * Concurrently runnable tsp calculator.  Each instance has its
	 * own queue, but checks against a shared bound so that it can
	 * take advantage of improvements to the bound made by other instances.
	 */
	class TspCalculator implements Runnable{
		private final Queue<TspNode> queue;
		
		private final AtomicInteger bound;
		private final AtomicReference<List<Sector>> bestPath;
		private final AtomicReference<List<Sector>> longestPath;
		
		public TspCalculator(
				AtomicInteger bound, 
				AtomicReference<List<Sector>> bestPath, 
				AtomicReference<List<Sector>> longestPath, 
				Queue<TspNode> queue, 
				int threadNumber){
			
			this.bound = bound;
			this.bestPath = bestPath;
			this.longestPath = longestPath;
			
			this.queue = queue;
		}

		@Override
		public void run() {
			
			int i = 0;
			
			while(!queue.isEmpty()){
				TspNode curr = queue.poll();
				
				if(curr.getPath().size() > longestPath.get().size()){
					longestPath.set(curr.getPath());
				}
				
				if(i++%10000 == 0){
					logState(queue.size(), curr.getBound(), bestPath.get(), longestPath.get());
				}
				
				//we're not going to have anything better than our current at this point, so return 
				if(curr.getBound() > bound.get()){
					logger.info("Searched all bounds less than " + bound + ", exiting");
					return;
				}
				
				//if the current path covers all sectors, it's a full path, so set it as our new best
				synchronized(theSolver){
					if(curr.getPath().size() == sectors.size()) {
						if(curr.getBound() < bound.get()){
							logger.info("Cost " + curr.getBound() + " path found, saving");
							logger.info(TspUtilities.routeString(curr.getPath()));
							bestPath.set(curr.getPath());
							bound.set(curr.getBound());
						}
						continue;
					}
				}
				
				//Expand search to the next step and add to queue
				Set<Sector> unvisited = new HashSet<>(sectors);
				unvisited.removeAll(curr.getPath());
				
				//handle case in which an ending is specified
				if(curr.getEnding() != null){
					unvisited.removeAll(curr.getEnding());
					if(unvisited.isEmpty()){
						logger.info("Ending found");
						List<Sector> full = new ArrayList<Sector>(curr.getPath());
						full.addAll(curr.getEnding());
						synchronized(theSolver){
							int currBound = getBoundForPath(full);
							if(currBound < bound.get()){
								bestPath.set(full);
								bound.set(currBound);
							}
						}
						continue;
					}
					for(Sector s : unvisited){
						if(curr.getEnding().contains(s)){
							continue;
						}
						List<Sector> newPath = new ArrayList<>(curr.getPath());
						newPath.add(s);
						TspNode newNode = new TspNode(newPath, getBoundForPath(newPath), curr.getEnding());
						if(newNode.getBound() <= bound.get()){
							queue.add(newNode);
						}
					}
					continue;
				}
				
				//no ending specified, use faster method
				for(Sector s : unvisited){
					List<Sector> newPath = new ArrayList<>(curr.getPath());
					newPath.add(s);
					TspNode newNode = new TspNode(newPath, getBoundForPath(newPath));
					if(newNode.getBound() <= bound.get()){
						queue.add(newNode);
					}
				}
			}
			
		}

	}	
	
}
