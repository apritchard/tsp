package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
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
	
	public int getBoundForPath(List<Sector> path) {
		
		int bound = IntStream.range(0, path.size() - 1)
				.boxed()
				.reduce(0, (a, b) -> a + getDistance(path.get(b), path.get(b + 1)));
		
		Predicate<Sector> notInPath = s -> !path.contains(s);
		Predicate<Sector> lastInPath = s -> s.equals(path.get(path.size()-1));
		ToIntFunction<Sector> minOfNextSteps = s1 -> shortestPaths.get(s1).keySet().stream()
				.filter(lastInPath.or(notInPath))
				.mapToInt(s2 -> shortestPaths.get(s2).get(s1))
				.min().orElse(0);
		
		bound += shortestPaths.keySet().stream()
				.filter(notInPath)
				.mapToInt(minOfNextSteps)
				.sum();
		
		return bound;
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

		Consumer<TspNode> nodeConsumer = node -> {
			synchronized (bound) {
				if(node.getPath().size() == sectors.size() && node.getBound() < bound.get()) {
					bestPath.set(node.getPath());
					bound.set(node.getBound());
					return;
				}
			}
			sectors.stream()
				.filter(s -> !node.getPath().isEmpty() && !node.getPath().contains(s))
				.map(s -> {
					List<Sector> newPath = new ArrayList<>(node.getPath());
					newPath.add(s);
					return new TspNode(newPath, getBoundForPath(newPath));
				})
				.filter(newNode -> newNode.getBound() <= bound.get())
				.forEach(queue::add);
		};
		
		Supplier<TspNode> nodeSupplier = () -> {
				synchronized(queue){
					return queue.isEmpty() ? new TspNode(new ArrayList<>(),   Integer.MAX_VALUE) : queue.poll();
				}
		};
		
		Stream.generate(nodeSupplier)
			.parallel()
			.peek(nodeConsumer)
			.filter(s -> s.getBound() > bound.get())
			.findAny();
		
		// if queue is empty and we haven't returned, then either we found no complete paths
		// (bestPath will be null), or the very last path we checked is the best path
		// (unlikely, but possible), in which case return it.
		return bestPath.get();
	}

}
