package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Concurrently runnable tsp calculator.  Each instance has its
 * own queue, but checks against a shared bound so that it can
 * take advantage of improvements to the bound made by other instances.
 * @author alex
 *
 */
public class TspCalculator implements Runnable{
	private final Logger logger;

	private final Queue<TspNode> queue;
	
	private final AtomicInteger bound;
	private final AtomicReference<List<Sector>> bestPath;
	private final AtomicReference<List<Sector>> longestPath;
	
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
			
			//TODO Still more logging that could be moved
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
			synchronized(mw){
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
					synchronized(mw){
						int currBound = mw.getBoundForPath(full);
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
					TspNode newNode = new TspNode(newPath, mw.getBoundForPath(newPath), curr.getEnding());
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
				TspNode newNode = new TspNode(newPath, mw.getBoundForPath(newPath));
				if(newNode.getBound() <= bound.get()){
					queue.add(newNode);
				}
			}
		}
		
	}

}
