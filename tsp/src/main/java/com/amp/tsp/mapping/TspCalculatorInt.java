package com.amp.tsp.mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class TspCalculatorInt implements Runnable {
	
	private static final Logger logger = Logger.getLogger(TspCalculatorInt.class.getName());
	
	private static int count = 0;
	
	private final AtomicInteger bound;
	private final AtomicReference<int[]> bestPath;
	private final Queue<TspNode2> queue;
	private final MapWrapper mw;
	
	private final int threadNum;
	
	//bounds optimization variables, local copies
	private final Sector[] sectorList;
	private final Map<Sector, Integer> sectorMap;
	private final boolean[] usedSectors; 
	private final boolean[] usedSectorsSwap; 
	private final int numSectors;

	public TspCalculatorInt(AtomicInteger bound, AtomicReference<int[]> bestPath, 
			Queue<TspNode2> initialQueue, MapWrapper mapWrapper) {
		this.bound = bound;
		this.bestPath = bestPath;
		this.queue = initialQueue;
		this.mw = mapWrapper;
		
		this.threadNum = count++;
		
		Set<Sector> sectors = mw.getSectors();
		
		//make local copies of these
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
		
		TspNode2 longest = queue.peek();
		
		while(!queue.isEmpty()){
			TspNode2 curr = queue.poll();
			
//			logger.info(threadNum + " evaluating " + TspUtilities.routeString(TspUtilities.sectorList(curr.getPath(),  sectorList)));
			
			if(curr.getLength() > longest.getLength()){
				longest = curr;
			}
			
			if(count++ % 100000 == 0){
				StringBuilder sb = new StringBuilder();
				sb.append("Trace:").append(System.lineSeparator());
				sb.append("\tQueue size: ").append(queue.size()).append(System.lineSeparator());
				sb.append("\tCurrent bound: ").append(curr.getBound()).append(System.lineSeparator());
				if(bestPath.get() != null) {
					sb.append("\tBest Complete Path: ").append(TspUtilities.routeString(bestPath.get(), sectorList));
				} else {
					sb.append("\tPath: (" + curr.getLength() + "/" + numSectors + ") ").append(TspUtilities.routeString(curr.getPath(), sectorList));
					sb.append("\n\tLongest Current Path: (" + longest.getLength() + "/" + numSectors + ") ").append(TspUtilities.routeString(longest.getPath(), sectorList));
				}
				logger.info(sb.toString());				
			}
			
			//this part is pretty cheap and cannot be interleaved with other threads, so just
			//synchronize it all
			synchronized(mw){
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
//				logger.info("Ending: " + TspUtilities.routeString(curr.getEnding(), sectorList));
				
				//always mark ending sectors as used even if not a complete path
				for(int s : curr.getEnding()){
					usedSectors[s] = true;
				}
				
				//full path, check if it's good
				if(curr.getLength() + curr.getEnding().length == numSectors){
					for(int s : curr.getEnding()){
						curr.addNode(s);
					}
					synchronized(mw){
						//this corrupts usedSectors, but no problem since we continue after this
						int currBound = mw.getBoundForPathThreadSafe(curr.getPath(), usedSectorsSwap);
						logger.info("Full path (" + currBound + ") " + TspUtilities.routeString(curr.getPath(), sectorList));
						if(currBound < bound.get()){
							bestPath.set(curr.getPath());
							bound.set(currBound);
						}
					}
					continue;
				}
				
				for(int i = 1; i <= numSectors; i++){
					if(!usedSectors[i]){
						int[] newPath = Arrays.copyOf(curr.getPath(), numSectors);
						newPath[curr.getLength()] = i;
						int newBound = mw.getBoundForPathThreadSafe(newPath, usedSectorsSwap);
						if(newBound <= bound.get()){
							queue.add(new TspNode2(newBound, newPath, curr.getEnding(), curr.getLength() + 1));
						}
					}
				}
				continue;

			}
			
			//Add all next steps to queue (which will sort them by bound)
			for(int i = 1; i <= numSectors; i++){
				if(!usedSectors[i]){
					int[] newPath = Arrays.copyOf(curr.getPath(), numSectors);
					newPath[curr.getLength()] = i;
					int newBound = mw.getBoundForPathThreadSafe(newPath, usedSectorsSwap);
					if(newBound <= bound.get()){
						queue.add(new TspNode2(newBound, newPath, curr.getEnding(), curr.getLength() + 1));
					}
					
				}
			}
		}
		
	}

}
