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
		int i = 1;
		for(Sector s : sectors){
			sectorMap.put(s, i);
			sectorList[i++] = s;
		}
		numSectors = sectors.size();
	}

	@Override
	public void run() {
		
		while(!queue.isEmpty()){
			TspNode2 curr = queue.poll();
			
//			logger.info(threadNum + " evaluating " + TspUtilities.routeString(TspUtilities.sectorList(curr.getPath(),  sectorList)));
			
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
			
			//Add all next steps to queue (which will sort them by bound)
			Arrays.fill(usedSectors, false);
			for(int i = 0; i < curr.getLength(); i++){
				usedSectors[curr.getPath()[i]] = true;
			}
			for(int i = 1; i <= numSectors; i++){
				if(!usedSectors[i]){
					int[] newPath = Arrays.copyOf(curr.getPath(), numSectors);
					newPath[curr.getLength()] = i;
					int newBound = mw.getBoundForPathThreadSafe(newPath, usedSectors);
					if(newBound <= bound.get()){
						queue.add(new TspNode2(newBound, newPath, curr.getEnding(), curr.getLength() + 1));
					}
					
				}
			}
		}
		
	}

}
