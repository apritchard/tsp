package com.amp.tsp;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.amp.tsp.mapping.BasicOptimizedTspSolver;
import com.amp.tsp.mapping.BasicTspSolver;
import com.amp.tsp.mapping.ForkJoinTspSolver;
import com.amp.tsp.mapping.LambdaSolver;
import com.amp.tsp.mapping.MultiOptimizedTspSolver;
import com.amp.tsp.mapping.MultiTspSolver;
import com.amp.tsp.mapping.OptimizedLambdaSolver;
import com.amp.tsp.mapping.Sector;
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
public class PerformanceTest {

	private static final Logger logger = Logger.getLogger(PerformanceTest.class);
	
	private Set<Sector> moderateSectors, longSectors, betaSectors;
	private List<List<Sector>> longSeeds;
	
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
//				new BasicTspSolver(moderateSectors),
//				new BasicOptimizedTspSolver(moderateSectors),
//				new MultiTspSolver(moderateSectors),
//				new MultiOptimizedTspSolver(moderateSectors),
				new LambdaSolver(moderateSectors)
//				new OptimizedLambdaSolver(moderateSectors)
//				new ForkJoinTspSolver(moderateSectors)
				)
			.collect(Collectors.toMap(classNameMapper, solveTimer));
		
		logger.info(results);
	}
	
	@Test
	public void test38NewBetaQuadrantMulti(){
		long multi, start;
		start = System.nanoTime();
		TspSolver mw = new MultiOptimizedTspSolver(betaSectors);
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
		
		long tsp, tspInt, multi, multiInt, fj;
		long start;
		
		start = System.nanoTime();
		mw = new BasicTspSolver(betaSectors);
		List<Sector> routeTsp = mw.solve();
		tsp = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new BasicOptimizedTspSolver(betaSectors);
		List<Sector> routeTspInt = mw.solve();
		tspInt = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MultiTspSolver(betaSectors);
		List<Sector> routeTspMulti = mw.solve();
		multi = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MultiOptimizedTspSolver(betaSectors);
		List<Sector> routeTspMultiInt = mw.solve();
		multiInt = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new ForkJoinTspSolver(betaSectors);
		List<Sector> routeTspFJ = mw.solve();
		fj = System.nanoTime() - start;
		
		logger.info(String.format("Beta Quadrant solver times milli: tsp(%d) int(%d) multi(%d) multiInt(%d) fj(%d)", tsp/1000000, tspInt/1000000, multi/1000000, multiInt/1000000, fj/1000000));
		
		assertEquals("Incorrect bound for moderate tsp", BETA_MIN_BOUND, mw.getBoundForPath(routeTsp));
		assertEquals("Incorrect bound for moderate tsp", BETA_MIN_BOUND, mw.getBoundForPath(routeTspInt));
		assertEquals("Incorrect bound for moderate tspMulti", BETA_MIN_BOUND, mw.getBoundForPath(routeTspMulti));
		assertEquals("Incorrect bound for moderate tspMulti", BETA_MIN_BOUND, mw.getBoundForPath(routeTspMultiInt));
		assertEquals("Incorrect bound for moderate tspFJ", BETA_MIN_BOUND, mw.getBoundForPath(routeTspFJ));
	}
	
	/**
	 * Test standard implementation on a long route.
	 */
	@Test
	public void test50LongTsp(){
		TspSolver mw = new BasicOptimizedTspSolver(longSectors, longSeeds, true);
		
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
		TspSolver mw = new MultiOptimizedTspSolver(longSectors, longSeeds, true);
		
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
			fjSolver = new ForkJoinTspSolver(moderateSectors);
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
	
}
