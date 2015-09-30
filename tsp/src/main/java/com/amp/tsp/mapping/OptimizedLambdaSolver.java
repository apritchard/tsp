package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class OptimizedLambdaSolver extends OptimizedTspSolver {

	/**
	 * @see TspSolver#TspSolver(Set)
	 */
	public OptimizedLambdaSolver(Set<Sector> sectors) {
		super(sectors);
	}

	/**
	 * @see TspSolver#TspSolver(Set, List, boolean)
	 */
	public OptimizedLambdaSolver(Set<Sector> sectors,
			List<List<Sector>> seeds, boolean useSeedsOnly) {
		super(sectors, seeds, useSeedsOnly);
	}

	/**
	 * @see TspSolver#TspSolver(Set, List)
	 */
	public OptimizedLambdaSolver(Set<Sector> sectors,
			List<Constraint> constraints) {
		super(sectors, constraints);
	}

	@Override
	public List<Sector> solve() {
//		final boolean[] usedSectors = new boolean[numSectors+1];
		final Queue<TspNode2> queue = new PriorityBlockingQueue<>(TspNode2.queueFrom(getInitialNodes(), sectorMap));
		
		//start with max bound and no best path
		AtomicInteger bound = new AtomicInteger(Integer.MAX_VALUE);
		AtomicReference<byte[]> bestPath = new AtomicReference<byte[]>();
		
		
		
		Consumer<TspNode2> nodeConsumer = node -> {
			if(node.getLength() == numSectors) {
				bestPath.set(node.getPath());
				bound.set(node.getBound());
			} else {
				boolean[] usedSectors = new boolean[numSectors+1];
				Arrays.fill(usedSectors, false);
				for(byte i = 0; i < node.getLength(); i++){
					usedSectors[node.getPath()[i]] = true;
				}				
				IntStream.rangeClosed(1, numSectors)
					.filter(i -> !usedSectors[i])
					.boxed()
					.map(i -> {
						byte[] newPath = Arrays.copyOf(node.getPath(), numSectors);
						newPath[node.getLength()] = i.byteValue();
						return new TspNode2(getBoundForPath(newPath, usedSectors), newPath, node.getEnding(), node.getLength() + 1);
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
		
		List<Sector> retList = TspUtilities.sectorList(bestPath.get(), sectorList);
		return retList;
	}

}
