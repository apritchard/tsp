package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MultiOptimizedTspSolver extends OptimizedTspSolver {

	private final TspSolver theSolver = this; //for synchronization
	
	AtomicInteger bound;
	AtomicReference<byte[]> bestPath;
	Queue<TspNode2> queue;
	
	/**
	 * @see TspSolver#TspSolver(Set)
	 */
	public MultiOptimizedTspSolver(Set<Sector> sectors) {
		super(sectors);
	}

	/**
	 * @see TspSolver#TspSolver(Set, List, boolean)
	 */
	public MultiOptimizedTspSolver(Set<Sector> sectors,
			List<List<Sector>> seeds, boolean useSeedsOnly) {
		super(sectors, seeds, useSeedsOnly);
	}

	/**
	 * @see TspSolver#TspSolver(Set, List)
	 */
	public MultiOptimizedTspSolver(Set<Sector> sectors,
			List<Constraint> constraints) {
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
		int numThreads = Runtime.getRuntime().availableProcessors() * 2;

		queue = new PriorityBlockingQueue<>(TspNode2.queueFrom(getInitialNodes(), sectorMap));
		bestPath = new AtomicReference<>();
		bound = new AtomicInteger(Integer.MAX_VALUE);
		
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < numThreads; i++) {
			TspCalculatorInt tspCalc = new TspCalculatorInt();
			executor.execute(tspCalc);
		}

		// Wait for them to finish
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.warning("Threads prematurely interrupted; program may not have finished.");
		}

		if (bestPath.get() == null) {
			 logger.warning("No complete path found, longest Path not saved");
			 return new ArrayList<>();
		} else {
			logger.info("Processing finished, returning best path.");
			return TspUtilities.sectorList(bestPath.get(), sectorList);
		}
	}
	
	public class TspCalculatorInt implements Runnable {
		
		//bounds optimization variables, local copies
		private final Sector[] sectorList;
		private final Map<Sector, Integer> sectorMap;
		private final boolean[] usedSectors; 
		private final boolean[] usedSectorsSwap; 
		private final int numSectors;

		public TspCalculatorInt() {
			//make thread-local copies of these
			sectorList = new Sector[sectors.size() + 1];
			sectorMap = new HashMap<>();
			usedSectors = new boolean[sectors.size() +1];
			usedSectorsSwap = new boolean[sectors.size() +1];
			int i = 1;
			for(Sector s : sectors){
				sectorMap.put(s, i);
				sectorList[i++] = s;
			}
			numSectors = sectors.size();
		}

		@Override
		public void run() {
			int count = 0;
			
			while(!queue.isEmpty()){
				TspNode2 curr = queue.poll();
				
				if(count++ % 100000 == 0){
					//save time by not tracking best path, use curr path instead
					logState(queue.size(), curr.getBound(), bestPath.get(), curr.getPath());
				}
				
				//this part is pretty cheap and cannot be interleaved with other threads, so just synchronize it all
				synchronized(theSolver){
					//we're not going to have anything better than our current at this point, so return 
					if(curr.getBound() > bound.get()){
						logger.info("Searched all bounds less than " + bound + ", exiting");
						return;
					}
					
					//if the current path covers all sectors, it's a full path, so set it as our new best
					if(curr.getLength() == numSectors) {
						if(curr.getBound() < bound.get()){
							logger.info("Cost " + curr.getBound() + " path found, saving");
							logger.info(TspUtilities.routeString(TspUtilities.sectorList(curr.getPath(), sectorList)));
							bestPath.set(curr.getPath());
							bound.set(curr.getBound());					
						}
						continue;
					}
				}

				//mark currently used sectors
				Arrays.fill(usedSectors, false);
				for(int s : curr.getPath()){
					usedSectors[s] = true;
				}

				//handle case in which an ending is specified
				if(curr.getEnding() != null){
					
					//always mark ending sectors as used even if not a complete path
					for(int s : curr.getEnding()){
						usedSectors[s] = true;
					}
					
					//full path, check if it's good
					if(curr.getLength() + curr.getEnding().length == numSectors){
						for(byte s : curr.getEnding()){
							curr.addNode(s);
						}
						synchronized(theSolver){
							int currBound = getBoundForPath(curr.getPath(), usedSectorsSwap);
							if(currBound < bound.get()){
								logger.info("Full path (" + currBound + ") " + TspUtilities.routeString(curr.getPath(), sectorList));
								bestPath.set(curr.getPath());
								bound.set(currBound);
							}
						}
						continue;
					}
				}
				
				//Add all next steps to queue (which will sort them by bound)
				for(byte i = 1; i <= numSectors; i++){
					if(!usedSectors[i]){
						byte[] newPath = Arrays.copyOf(curr.getPath(), numSectors);
						newPath[curr.getLength()] = i;
						int newBound = getBoundForPath(newPath, usedSectorsSwap);
						if(newBound <= bound.get()){
							queue.add(new TspNode2(newBound, newPath, curr.getEnding(), curr.getLength() + 1));
						}
					}
				}
			}
			
			System.gc();
		}
	}
}
