package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.amp.tsp.mapping.TspSolution.TspBuilder;

public class ForkJoinTspSolver extends TspSolver {
	
	//Statistics gathering across all actions
	private static final AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());
	private static final AtomicInteger count = new AtomicInteger(0);
	private static final AtomicInteger omitted = new AtomicInteger(0);
	
	private TspNode bestPath;
	
	/**
	 * @see TspSolver#TspSolver(TspBuilder)
	 */
	public ForkJoinTspSolver(TspBuilder builder) {
		super(builder);
	}

	/**
	 * Solve using a multi-threaded fork-join pool implementation. Uses
	 * a default depthThreshold.
	 * @return The optimal path
	 */
	public List<Sector> solve(){
		//Performance seems fairly constant across depths, as long as >1
		int depthThreshold = 19;
		return solve(depthThreshold);
	}
	
	/**
	 * Solve using a multi-threaded fork-join pool implementation.
	 * @param depthThreshold The path size at which to begin independent solutions in their own threads.
	 * @return The optimal path
	 */
	public List<Sector> solve(int depthThreshold){
		ForkJoinPool fjp = new ForkJoinPool();
		fjp.invoke(new TspCalcAction(new PriorityBlockingQueue<TspNode>(getInitialNodes()), depthThreshold));
		return bestPath.getPath();
	}
	
	private synchronized int getBound(){
		return bestPath == null? Integer.MAX_VALUE : bestPath.getBound();
	}
	
	private synchronized void setPossibleBestPath(TspNode path){
		if(bestPath == null	|| path.getBound() < bestPath.getBound()){
			bestPath = path;
		} 
	}
	
	public class TspCalcAction extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		
		private final Queue<TspNode> queue;
		private final int depthThreshold;

		public TspCalcAction(Queue<TspNode> queue, int depthThreshold) {
			this.depthThreshold = depthThreshold;

			this.queue = queue;
		}

		@Override
		protected void compute() {
			if(queue.isEmpty()) {
				logger.info("Empty queue, thread returning");
				return;
			}
			
			int bound = getBound();
			
			TspNode curr = queue.poll();
			
			int c = count.incrementAndGet();
			if(System.currentTimeMillis() - lastTime.get() > 10000){
				lastTime.set(System.currentTimeMillis());
				logger.info(String.format("Processed: %d Omitted: %d CurrentBound: %d QueueSize: %d", 
						c, omitted.get(), curr.getBound(), queue.size()));
			}
			
			//We've found the best path already, bail out of this Action.
			if(curr.getBound() >= bound){
				omitted.incrementAndGet();
				return;
			}
			
			//If current path is complete, submit it as a possible best path and bail
			if(curr.getPath().size() == sectors.size()){
				logger.info("Cost " + curr.getBound() + " path found, saving");
				logger.info(TspUtilities.routeString(curr.getPath()));
				setPossibleBestPath(curr);
				return;
			}
			
			Set<TspCalcAction> tasks = new HashSet<>();
			
			Set<Sector> unvisited = new HashSet<>(sectors);
			unvisited.removeAll(curr.getPath());
			for(Sector s: unvisited){
				List<Sector> newPath = new ArrayList<>(curr.getPath());
				newPath.add(s);
				TspNode newNode = new TspNode(newPath, getBoundForPath(newPath));
				if(newNode.getBound() <= bound){
					//when we hit the depthThreshold, spawn off threads to solve individual subproblems
					if (unvisited.size() == depthThreshold){
						Queue<TspNode> newQueue = new PriorityBlockingQueue<>();
						newQueue.add(newNode);
						tasks.add(new TspCalcAction(newQueue, depthThreshold));
					} else {
						//otherwise just add this node to the queue and recurse
						queue.add(newNode);
						new TspCalcAction(queue, depthThreshold).invoke();
					}
				}
			}
			if(tasks.size() > 0){
				invokeAll(tasks);
			}
		}

	}
}
