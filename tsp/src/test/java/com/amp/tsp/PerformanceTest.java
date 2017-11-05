package com.amp.tsp;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amp.tsp.mapping.SimulatedAnnealingTspSolver;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.amp.tsp.mapping.BasicOptimizedNearestNeighborTspSolver;
import com.amp.tsp.mapping.BasicOptimizedTspSolver;
import com.amp.tsp.mapping.BasicTspSolver;
import com.amp.tsp.mapping.ForkJoinTspSolver;
import com.amp.tsp.mapping.LambdaSolver;
import com.amp.tsp.mapping.MultiOptimizedNearestNeighborTspSolver;
import com.amp.tsp.mapping.MultiOptimizedTspSolver;
import com.amp.tsp.mapping.MultiTspSolver;
import com.amp.tsp.mapping.NearestNeighborSolver;
import com.amp.tsp.mapping.OptimizedLambdaSolver;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspSolution;
import com.amp.tsp.mapping.TspSolution.TspBuilder;
import com.amp.tsp.mapping.TspSolver;
import com.amp.tsp.parse.MapParser;

/**
 * This class tests longer running examples and measures their performance
 * in addition to correctness.  
 * 
 * Looks like the simplest way to control sort order is by method name. If this class
 * (heaven forbid) has more than 100 methods, naming convention should be adjusted.
 * 
 * @author alex
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore
public class PerformanceTest {

	private static final Logger logger = Logger.getLogger(PerformanceTest.class);
	
	private Set<Sector> moderateSectors, longSectors, betaSectors;
	private List<List<Sector>> longSeeds;
	
	private TspBuilder moderateBuilder, longBuilder, betaBuilder, longSeedsBuilder;
	
	final int MODERATE_MIN_BOUND = 23;
	final int LONG_MIN_BOUND = 38;
	final int BETA_MIN_BOUND = 4068;
	
	@Before
	public void initialize(){
		URL moderateFile = PerformanceTest.class.getClassLoader().getResource("federation-space-boundaries.yaml");
		URL longFile = PerformanceTest.class.getClassLoader().getResource("boundaries.yaml");
		URL betaFile = PerformanceTest.class.getClassLoader().getResource("season10-beta-quadrant.yaml");
		moderateSectors = MapParser.parseMapFile(moderateFile);
		longSectors = MapParser.parseMapFile(longFile);
		betaSectors = MapParser.parseMapFile(betaFile);
		
		URL longSeedFile = PerformanceTest.class.getClassLoader().getResource("seeds-boundaries.yaml");
		longSeeds = MapParser.parseSeedFile(longSeedFile, longSectors);
		
		moderateBuilder = TspSolution.forSectors(moderateSectors).build();
		longBuilder = TspSolution.forSectors(longSectors).build();
		betaBuilder = TspSolution.forSectors(betaSectors).build();
		longSeedsBuilder = TspSolution.forSectors(longSectors).usingSeeds(longSeeds).required().build();
	}
	
	/**
	 * Compare performance of tsp implementations on a map of moderate size.
	 */
	@Test
	public void test00Moderate(){
		
		Function<TspSolver, String> classNameMapper = 
				(c) -> c.getClass().getSimpleName();
			
		Function<TspSolver, Double> solveTimer = 
				(s) -> {
					long start = System.nanoTime();
					List<Sector> route = s.solve();
					long time = System.nanoTime() - start;
					assertEquals("Incorrect bound for moderate tsp", MODERATE_MIN_BOUND, s.getBoundForPath(route));
					return (new BigDecimal(time)).divide(new BigDecimal(1000000000)).setScale(2, RoundingMode.HALF_UP).doubleValue();
				};
				
		Map<String, Double> results = Stream.of(
//				new BasicTspSolver(moderateBuilder),
//				new BasicOptimizedTspSolver(moderateBuilder),
//				new MultiTspSolver(moderateBuilder),
//				new MultiOptimizedTspSolver(moderateBuilder),
//				new LambdaSolver(moderateBuilder),
//				new OptimizedLambdaSolver(moderateBuilder),
//				new ForkJoinTspSolver(moderateBuilder),
//				new NearestNeighborSolver(3, moderateBuilder),
				new MultiOptimizedNearestNeighborTspSolver(3, moderateBuilder)
//				new SimulatedAnnealingTspSolver(moderateBuilder)
				)
			.collect(Collectors.toMap(classNameMapper, solveTimer));
		
		logger.info(results);
	}
	
	@Test
	public void test38NewBetaQuadrantMulti(){
		long multi, start;
		start = System.nanoTime();
		TspSolver mw = new MultiOptimizedTspSolver(betaBuilder);
		List<Sector> routeTspMulti = mw.solve();
		multi = System.nanoTime() - start;
		logger.info("Beta Quadrant Length times milli (tspMulti):" + multi/1000000);
		assertEquals("Incorrect bound for moderate tspMulti", BETA_MIN_BOUND, mw.getBoundForPath(routeTspMulti));
	}
	
	@Test
	public void test39MultiExtensive(){
		//TODO Test using multiple expensive clickmaps
	}
	
		@Test
	public void test40NewBetaQuadrant(){
		TspSolver mw;
		
		long tsp, tspInt, multi, multiInt, fj, nn, onn, monn;
		long start;
		
		start = System.nanoTime();
		mw = new BasicTspSolver(betaBuilder);
		List<Sector> routeTsp = mw.solve();
		tsp = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new BasicOptimizedTspSolver(betaBuilder);
		List<Sector> routeTspInt = mw.solve();
		tspInt = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MultiTspSolver(betaBuilder);
		List<Sector> routeTspMulti = mw.solve();
		multi = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MultiOptimizedTspSolver(betaBuilder);
		List<Sector> routeTspMultiInt = mw.solve();
		multiInt = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new ForkJoinTspSolver(betaBuilder);
		List<Sector> routeTspFJ = mw.solve();
		fj = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new NearestNeighborSolver(3, betaBuilder);
		List<Sector> routeNN = mw.solve();
		nn = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new BasicOptimizedNearestNeighborTspSolver(3, betaBuilder);
		List<Sector> routeOptimizedNN = mw.solve();
		onn = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MultiOptimizedNearestNeighborTspSolver(3, betaBuilder);
		List<Sector> routeMultiOptimizedNN = mw.solve();
		monn = System.nanoTime() - start;
		
		logger.info(String.format("Beta Quadrant solver times milli: tsp(%d) int(%d) multi(%d) multiInt(%d) fj(%d) nn(%d) onn(%d) monn(%d)", tsp/1000000, tspInt/1000000, multi/1000000, multiInt/1000000, fj/1000000, nn/1000000, onn/1000000, monn/1000000));
		
		assertEquals("Incorrect bound for moderate tsp", BETA_MIN_BOUND, mw.getBoundForPath(routeTsp));
		assertEquals("Incorrect bound for moderate tsp", BETA_MIN_BOUND, mw.getBoundForPath(routeTspInt));
		assertEquals("Incorrect bound for moderate tspMulti", BETA_MIN_BOUND, mw.getBoundForPath(routeTspMulti));
		assertEquals("Incorrect bound for moderate tspMulti", BETA_MIN_BOUND, mw.getBoundForPath(routeTspMultiInt));
		assertEquals("Incorrect bound for moderate tspFJ", BETA_MIN_BOUND, mw.getBoundForPath(routeTspFJ));
		assertEquals("Incorrect bound for moderate nn", BETA_MIN_BOUND, mw.getBoundForPath(routeNN));
		assertEquals("Incorrect bound for moderate onn", BETA_MIN_BOUND, mw.getBoundForPath(routeOptimizedNN));
		assertEquals("Incorrect bound for moderate monn", BETA_MIN_BOUND, mw.getBoundForPath(routeMultiOptimizedNN));
	}
	
	/**
	 * Test standard implementation on a long route.
	 */
	@Test
	public void test50LongTsp(){
		TspSolver mw = new BasicOptimizedTspSolver(longSeedsBuilder);
		
		long start, timeCost; 
				
		start = System.nanoTime();
		List<Sector> routeTsp = mw.solve();
		timeCost = System.nanoTime() - start;
		
		logger.info("Long Length times milli (tsp):" + timeCost/1000000);
		
		assertEquals("Incorrect bound for long tsp", LONG_MIN_BOUND, mw.getBoundForPath(routeTsp));
	}
	
	/**
	 * Test multi-threaded implementation on a long route.
	 */
	@Test
	public void test51LongTspMulti(){
		TspSolver mw = new MultiOptimizedTspSolver(longSeedsBuilder);
		
		long start, timeCost; 
		
		start = System.nanoTime();
		List<Sector> route = mw.solve();
		timeCost = System.nanoTime() - start;
		
		logger.info("Long Length times milli (tspMulti):" + timeCost/1000000);
		
		assertEquals("Incorrect bound for long tspMulti", LONG_MIN_BOUND, mw.getBoundForPath(route));
	}
	
	/**
	 * Test performance of depth threshold on fork-join solution to moderate route
	 */
	@Test
	public void test70ForkJoinDepth(){
		ForkJoinTspSolver fjSolver;
		
		Map<Integer, Long> times = new HashMap<>();
		long start, total;
		for(int i = 0; i < moderateSectors.size(); i++){
			start = System.nanoTime();
			fjSolver = new ForkJoinTspSolver(moderateBuilder);
			List<Sector> routeTsp = fjSolver.solve(i);
			total = System.nanoTime() - start;
			times.put(i, total);
			assertEquals("Incorrect bound for forkJoin with depth of " + i, MODERATE_MIN_BOUND, fjSolver.getBoundForPath(routeTsp));
		}
		
		StringBuilder sb = new StringBuilder();
		for(Entry<Integer, Long> entry : times.entrySet()){
			sb.append("(").append(entry.getKey()).append(", ").append(entry.getValue()/1000000).append("ms)");
		}
		logger.info(sb.toString());
	}
	
	@Test
	public void test80NearestNeighborCompare(){
		int MAX_NEIGHBORS = 5;
		long start, time;
		TspSolver solver;
		List<Sector> route;
		StringBuilder sb = new StringBuilder();
		
		for(int i = 1; i <= MAX_NEIGHBORS; i++){
			sb.append("Neighbors(").append(i).append(")").append(System.getProperty("line.separator"));
			start = System.nanoTime();			
			solver = new MultiOptimizedNearestNeighborTspSolver(i, moderateBuilder);
			route = solver.solve();
			time = (System.nanoTime() - start)/1000000;
			sb.append("moderate(").append(time).append(")(").append(solver.getBoundForPath(route)).append(") ");
			start = System.nanoTime();
			solver = new MultiOptimizedNearestNeighborTspSolver(i, longBuilder);
			route = solver.solve();
			time = (System.nanoTime() - start)/1000000;
			sb.append("long(").append(time).append(")(").append(solver.getBoundForPath(route)).append(") ");
			start = System.nanoTime();
			solver = new MultiOptimizedNearestNeighborTspSolver(i, betaBuilder);
			route = solver.solve();
			time = (System.nanoTime() - start)/1000000;
			sb.append("beta(").append(time).append(")(").append(solver.getBoundForPath(route)).append(") ").append(System.getProperty("line.separator"));
		}
		sb.append("Moderate actual: ").append(MODERATE_MIN_BOUND).append(" Long actual: ").append(LONG_MIN_BOUND).append(" Beta actual: ").append(BETA_MIN_BOUND);
		logger.info(sb.toString());
	}

	@Test
	public void test90MultiOptimizedPerformanceMetric(){
		TspSolver longSolver = new MultiOptimizedTspSolver(longBuilder);
		TspSolver betaSolver = new MultiOptimizedTspSolver(betaBuilder);

		long start, total;
		List<Long> times;
		int numRuns = 10;

		times = new ArrayList<>();
		total = 0;
		for(int i = 0; i < numRuns; i++){
			start = System.currentTimeMillis();
			List<Sector> path = betaSolver.solve();
			assertEquals("Incorrect bound for beta optimized", BETA_MIN_BOUND, betaSolver.getBoundForPath(path));
			start = System.currentTimeMillis() - start;
			total += start;
			times.add(start);
		}
		long average = total / numRuns;
		logger.info(String.format("Total(%d) Avg(%d) %s", total, average, Arrays.toString(times.toArray())));

	}
	
}
