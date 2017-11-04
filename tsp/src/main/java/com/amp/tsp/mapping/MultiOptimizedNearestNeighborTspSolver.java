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
import java.util.stream.Collectors;

import com.amp.tsp.mapping.TspSolution.TspBuilder;

public class MultiOptimizedNearestNeighborTspSolver extends OptimizedTspSolver {

	private final TspSolver theSolver = this; //for synchronization

	AtomicInteger bestBoundPathLength;
	AtomicInteger bound;
	AtomicReference<byte[]> bestPath;
	Queue<TspNode2> queue;
	
	private Map<Byte, byte[]> nearestNeighbors;
	private int nNearest;
	
	/**
	 * @see TspSolver#TspSolver(TspBuilder)
	 */
	public MultiOptimizedNearestNeighborTspSolver(TspBuilder builder) {
		super(builder);
	}
	
	public MultiOptimizedNearestNeighborTspSolver(int n, TspBuilder builder){
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
		
		int numThreads = Runtime.getRuntime().availableProcessors() * 2;

		queue = new PriorityBlockingQueue<>(TspNode2.queueFrom(getInitialNodes(), sectorMap));
		bestPath = new AtomicReference<>();
		bound = new AtomicInteger(Integer.MAX_VALUE);
		bestBoundPathLength = new AtomicInteger(0);

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
			logger.warn("Threads prematurely interrupted; program may not have finished.");
		}

		if (bestPath.get() == null) {
			 logger.warn("No complete path found, longest Path not saved");
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
					//save time by not tracking best path before it's complete, use curr path instead
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
				for(byte i = 0; i < curr.getLength(); i++){
					usedSectors[curr.getPath()[i]] = true;
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
				
				byte[] currPath = curr.getPath();
				byte[] nearest = nearestNeighbors.get(currPath[curr.getLength()-1]);
				int cnt = 0;

				//Add all next steps to queue (which will sort them by bound)
				for(byte i : nearest){
					if(!usedSectors[i]){
						byte[] newPath = Arrays.copyOf(curr.getPath(), numSectors);
						newPath[curr.getLength()] = i;
						int newBound = getBoundForPath(newPath, usedSectorsSwap);
						if(newBound <= bound.get()){
							queue.add(new TspNode2(newBound, newPath, curr.getEnding(), curr.getLength() + 1));
							if (progressFrame != null && curr.getLength() > bestBoundPathLength.get()) {
								bestBoundPathLength.set(curr.getLength());
								progressFrame.setProgress(curr.getLength()+1);
							}
						}
						if (++cnt == nNearest){
							break;
						}						
					}
				}
			}
			
			System.gc();
		}
	}
}
