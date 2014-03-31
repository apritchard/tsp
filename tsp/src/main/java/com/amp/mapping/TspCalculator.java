package com.amp.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class TspCalculator implements Runnable{
	private final Logger logger;
	
	
	AtomicInteger bound;
	AtomicReference<List<Sector>> bestPath;
	AtomicReference<List<Sector>> longestPath;
	Queue<TspNode> queue;
	
	private final Set<Sector> sectors;
	private final MapWrapper mw;
	
	public TspCalculator(
			AtomicInteger bound, 
			AtomicReference<List<Sector>> bestPath, 
			AtomicReference<List<Sector>> longestPath, 
			Queue<TspNode> queue, 
			int threadNumber,
			MapWrapper mw){
		logger = Logger.getLogger(MapWrapper.class.getName() + "(#" + threadNumber + ")");
		
		this.bound = bound;
		this.bestPath = bestPath;
		this.longestPath = longestPath;
		
		this.queue = queue;
		this.sectors = mw.getSectors();
		this.mw = mw;
	}

	@Override
	public void run() {
		
		int i = 0;
		
		while(!queue.isEmpty()){
			TspNode curr = queue.poll();
			
			int longestSize = longestPath.get().size();
			if(curr.getPath().size() > longestSize){
				longestPath.set(curr.getPath());
				longestSize = curr.getPath().size();
			}
			
			if(i++%10000 == 0){
				StringBuilder sb = new StringBuilder();
				sb.append("Trace:").append(System.lineSeparator());
				sb.append("\tQueue size: ").append(queue.size()).append(System.lineSeparator());
				sb.append("\tCurrent bound: ").append(curr.getBound()).append(System.lineSeparator());
				if(bestPath.get() != null) {
					sb.append("\tBest Complete Path: ").append(bestPath.get());
				} else {
					sb.append("\tLongest Current Path: (" + longestSize + "/" + sectors.size() + ") ").append(longestPath.get());
				}
				logger.info(sb.toString());
			}
			
			//we're not going to have anything better than our current at this point, so return 
			if(curr.getBound() > bound.get()){
				logger.info("Searched all bounds less than " + bound + ", exiting");
				return;
			}
			
			//if the current path covers all sectors, it's a full path, so set it as our new best
			if(curr.getPath().size() == sectors.size() && curr.getBound() < bound.get()) {
				logger.info("Cost " + curr.getBound() + " path found, saving");
				logger.info(TspUtilities.routeString(curr.getPath()));
				bestPath.set(curr.getPath());
				bound.set(curr.getBound());
				continue;
			}
			
			Set<Sector> unvisited = new HashSet<>(sectors);
			unvisited.removeAll(curr.getPath());
			for(Sector s : unvisited){
				List<Sector> newPath = new ArrayList<>(curr.getPath());
				newPath.add(s);
				TspNode newNode = new TspNode(newPath, mw.getBoundForPath(newPath));
				if(newNode.getBound() <= bound.get()){
					queue.add(newNode);
				}
			}
		}
		
	}

}
