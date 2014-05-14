package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * ForkJoinAction implementation
 * @author alex
 *
 */
public class TspCalcAction extends RecursiveAction {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TspCalcAction.class.getName());
	
	//Statistics gathering across all actions
	private static final AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());
	private static final AtomicInteger count = new AtomicInteger(0);
	private static final AtomicInteger omitted = new AtomicInteger(0);

	private final Queue<TspNode> queue;
	private final Set<Sector> sectors;
	private final MapWrapper mw;
	private final int depthThreshold;

	public TspCalcAction(Queue<TspNode> queue, MapWrapper mw, int depthThreshold) {
		this.depthThreshold = depthThreshold;

		this.queue = queue;
		this.sectors = mw.getSectors();
		this.mw = mw;
	}

	@Override
	protected void compute() {
		if(queue.isEmpty()) {
			logger.info("Empty queue, thread returning");
			return;
		}
		
		int bound = mw.getBound();
		
		TspNode curr = queue.poll();
		
		int c = count.incrementAndGet();
		if(System.currentTimeMillis() - lastTime.get() > 10000){
			lastTime.set(System.currentTimeMillis());
			logger.info(String.format("Processed: %d Omitted: %d CurrentBound: %d QueueSize: %d", 
					c, omitted.get(), curr.getBound(), queue.size()));
		}
		
		if(curr.getBound() >= bound){
			omitted.incrementAndGet();
			return;
		}
		
		if(curr.getPath().size() == sectors.size()){
			logger.info("Cost " + curr.getBound() + " path found, saving");
			logger.info(TspUtilities.routeString(curr.getPath()));
			mw.setPossibleBestPath(curr);
			return;
		}
		
		Set<TspCalcAction> tasks = new HashSet<>();
		
		Set<Sector> unvisited = new HashSet<>(sectors);
		unvisited.removeAll(curr.getPath());
		for(Sector s: unvisited){
			List<Sector> newPath = new ArrayList<>(curr.getPath());
			newPath.add(s);
			TspNode newNode = new TspNode(newPath, mw.getBoundForPath(newPath));
			if(newNode.getBound() <= bound){
				//when we hit the depthThreshold, spawn off threads to solve individual subproblems
				if (unvisited.size() == depthThreshold){
					Queue<TspNode> newQueue = new PriorityBlockingQueue<>();
					newQueue.add(newNode);
					tasks.add(new TspCalcAction(newQueue, mw, depthThreshold));
				} else {
					queue.add(newNode);
					new TspCalcAction(queue, mw, depthThreshold).invoke();
				}
			}
		}
		if(tasks.size() > 0){
			invokeAll(tasks);
		}
	}

}
