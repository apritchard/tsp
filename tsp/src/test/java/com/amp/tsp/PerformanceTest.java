package com.amp.tsp;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.amp.tsp.mapping.MapWrapper;
import com.amp.tsp.mapping.Sector;
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

	private static final Logger logger = Logger.getLogger(PerformanceTest.class.getName());
	
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
		MapWrapper mw;
		
		long tsp, tspInt, multi, multiInt, fj;
		long start; 
				
		start = System.nanoTime();
		mw = new MapWrapper(moderateSectors);
		List<Sector> routeTsp = mw.calcTsp();
		tsp = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MapWrapper(moderateSectors);
		List<Sector> routeTspInt = mw.calcTspInt();
		tspInt = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MapWrapper(moderateSectors);
		List<Sector> routeTspMulti = mw.calcTspMulti();
		multi = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MapWrapper(moderateSectors);
		List<Sector> routeTspMultiInt = mw.calcTspMultiInt();
		multiInt = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MapWrapper(moderateSectors);
		List<Sector> routeTspFJ = mw.calcTspForkJoin();
		fj = System.nanoTime() - start;
		
		logger.info(String.format("Moderate Length times milli: tsp(%d) tspInt(%d) multi(%d) multiInt(%d) fj(%d)", tsp/1000000, tspInt/1000000, multi/1000000, multiInt/1000000, fj/1000000));
		
		assertEquals("Incorrect bound for moderate tsp", MODERATE_MIN_BOUND, mw.getBoundForPath(routeTsp));
		assertEquals("Incorrect bound for moderate tspInt", MODERATE_MIN_BOUND, mw.getBoundForPath(routeTspInt));
		assertEquals("Incorrect bound for moderate tspMulti", MODERATE_MIN_BOUND, mw.getBoundForPath(routeTspMulti));
		assertEquals("Incorrect bound for moderate tspMulti", MODERATE_MIN_BOUND, mw.getBoundForPath(routeTspMultiInt));
		assertEquals("Incorrect bound for moderate tspFJ", MODERATE_MIN_BOUND, mw.getBoundForPath(routeTspFJ));
	}
	
	@Test
	public void test38NewBetaQuadrantMulti(){
		long multi, start;
		start = System.nanoTime();
		MapWrapper mw = new MapWrapper(betaSectors);
		List<Sector> routeTspMulti = mw.calcTspMultiInt();
		multi = System.nanoTime() - start;
		logger.info("Beta Quadrant Length times milli (tspMulti):" + multi/1000000);
		assertEquals("Incorrect bound for moderate tspMulti", BETA_MIN_BOUND, mw.getBoundForPath(routeTspMulti));
	}
	
	@Test
	public void test40NewBetaQuadrant(){
		MapWrapper mw;
		
		long tsp, tspInt, multi, multiInt, fj;
		long start;
		
		start = System.nanoTime();
		mw = new MapWrapper(betaSectors);
		List<Sector> routeTsp = mw.calcTsp();
		tsp = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MapWrapper(betaSectors);
		List<Sector> routeTspInt = mw.calcTspInt();
		tspInt = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MapWrapper(betaSectors);
		List<Sector> routeTspMulti = mw.calcTspMulti();
		multi = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MapWrapper(betaSectors);
		List<Sector> routeTspMultiInt = mw.calcTspMultiInt();
		multiInt = System.nanoTime() - start;
		
		start = System.nanoTime();
		mw = new MapWrapper(betaSectors);
		List<Sector> routeTspFJ = mw.calcTspForkJoin();
		fj = System.nanoTime() - start;
		
		logger.info(String.format("Beta Quadrant times milli: tsp(%d) int(%d) multi(%d) multiInt(%d) fj(%d)", tsp/1000000, tspInt/1000000, multi/1000000, multiInt/1000000, fj/1000000));
		
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
		MapWrapper mw = new MapWrapper(longSectors, longSeeds, true);
		
		long start, timeCost; 
				
		start = System.nanoTime();
		List<Sector> routeTsp = mw.calcTsp();
		timeCost = System.nanoTime() - start;
		
		logger.info("Long Length times milli (tsp):" + timeCost/1000000);
		
		assertEquals("Incorrect bound for long tsp", LONG_MIN_BOUND, mw.getBoundForPath(routeTsp));
	}
	
	/**
	 * Test multi-threaded implementation on a long route.
	 */
	@Test
	public void test51LongTspMulti(){
		MapWrapper mw = new MapWrapper(longSectors, longSeeds, true);
		
		long start, timeCost; 
		
		start = System.nanoTime();
		List<Sector> route = mw.calcTspMulti();
		timeCost = System.nanoTime() - start;
		
		logger.info("Long Length times milli (tspMulti):" + timeCost/1000000);
		
		assertEquals("Incorrect bound for long tspMulti", LONG_MIN_BOUND, mw.getBoundForPath(route));
	}
	
	/*
	 * Test fork-join implementation on a long route
	 */
	@Test
	public void test52LongTspForkJoin(){
		MapWrapper mw = new MapWrapper(longSectors, longSeeds, true);
		
		long start, timeCost;
		
		start = System.nanoTime();
		List<Sector> route = mw.calcTspForkJoin(18);
		timeCost = System.nanoTime() - start;
		
		logger.info("Long Length times milli (tspFj):" + timeCost/1000000);
		
		assertEquals("Incorrect bound for long tspForkJoin", LONG_MIN_BOUND, mw.getBoundForPath(route));		
	}
	
	/**
	 * Test performance of depth threshold on fork-join solution to moderate route
	 */
	@Test
	public void test70ForkJoinDepth(){
		MapWrapper mw;
		
		Map<Integer, Long> times = new HashMap<>();
		long start, total;
		for(int i = 0; i < moderateSectors.size(); i++){
			start = System.nanoTime();
			mw = new MapWrapper(moderateSectors);
			List<Sector> routeTsp = mw.calcTspForkJoin(i);
			total = System.nanoTime() - start;
			times.put(i, total);
			assertEquals("Incorrect bound for forkJoin with depth of " + i, MODERATE_MIN_BOUND, mw.getBoundForPath(routeTsp));
		}
		
		StringBuilder sb = new StringBuilder();
		for(Entry<Integer, Long> entry : times.entrySet()){
			sb.append("(").append(entry.getKey()).append(", ").append(entry.getValue()/1000000).append("ms)");
		}
		logger.info(sb.toString());
	}
	
	/**
	 * Test performance of depth threshold on fork-join solution to long route.
	 * Conclusion: it doesn't really make a difference as long as it's greater than 1.
	 */
	@Test
	public void test71ForkJoinDepthLong(){
		MapWrapper mw;
		
		Map<Integer, Long> times = new HashMap<>();
		long start, total;
		for(int i = 1; i < 23; i++){
			start = System.nanoTime();
			mw = new MapWrapper(longSectors, longSeeds, true);
			List<Sector> routeTsp = mw.calcTspForkJoin(i);
			total = System.nanoTime() - start;
			times.put(i, total);
			assertEquals("Incorrect bound for forkJoin with depth of " + i, LONG_MIN_BOUND, mw.getBoundForPath(routeTsp));
		}
		
		StringBuilder sb = new StringBuilder();
		for(Entry<Integer, Long> entry : times.entrySet()){
			sb.append("(").append(entry.getKey()).append(", ").append(entry.getValue()/1000000).append("ms)");
		}
		logger.info(sb.toString());
	}
}
