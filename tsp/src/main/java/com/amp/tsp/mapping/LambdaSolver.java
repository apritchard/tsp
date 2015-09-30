package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class LambdaSolver extends TspSolver {

	public LambdaSolver(Set<Sector> sectors) {
		super(sectors);
	}
	
	public LambdaSolver(Set<Sector> sectors, List<List<Sector>> seeds, boolean useSeedsOnly) {
		super(sectors, seeds, useSeedsOnly);
	}
	
	public LambdaSolver(Set<Sector> sectors, List<Constraint> constraints){
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

		Queue<TspNode> queue = new PriorityBlockingQueue<TspNode>(getInitialNodes());

		// start with max bound and no best path
		AtomicInteger bound = new AtomicInteger(Integer.MAX_VALUE);
		AtomicReference<List<Sector>> bestPath = new AtomicReference<>();

		int i = 0;
		List<Sector> longestPath = new ArrayList<>(); // this is just to view
														// partial progress in
														// logging

		
		Consumer<TspNode> nodeConsumer = node -> {
			if(node.getPath().size() == sectors.size() ) {
				bestPath.set(node.getPath());
				bound.set(node.getBound());
			} else {
				sectors.stream()
					.filter(s -> !node.getPath().contains(s))
					.map(s -> {
						List<Sector> newPath = new ArrayList<>(node.getPath());
						newPath.add(s);
						return new TspNode(newPath, getBoundForPath(newPath));
					})
					.filter(newNode -> newNode.getBound() <= bound.get())
					.forEach(queue::add);
			}
		};
		
		Stream.generate(() -> queue.poll())
			.parallel()
			.peek(nodeConsumer)
			.filter(s -> s.getBound() > bound.get())
			.findFirst();
		
		// if queue is empty and we haven't returned, then either we found no complete paths
		// (bestPath will be null), or the very last path we checked is the best path
		// (unlikely, but possible), in which case return it.
		return bestPath.get();
	}

}
