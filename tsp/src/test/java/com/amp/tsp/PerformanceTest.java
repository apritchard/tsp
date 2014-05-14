package com.amp.tsp;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;
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
 * in addition to correctness.  It fixes the method order to enforce
 * increasing time cost in case you want to bail early for partial results.
 * @author alex
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PerformanceTest {

	private static final Logger logger = Logger.getLogger(PerformanceTest.class.getName());
	
	private Set<Sector> moderateSectors, longSectors;
	private List<List<Sector>> longSeeds;
	
	@Before
	public void initialize(){
		URL moderateFile = PerformanceTest.class.getClassLoader().getResource("federation-space-boundaries.yaml");
		URL longFile = PerformanceTest.class.getClassLoader().getResource("boundaries.yaml");
		moderateSectors = MapParser.parseMapFile(moderateFile);
		longSectors = MapParser.parseMapFile(longFile);
		
		URL longSeedFile = PerformanceTest.class.getClassLoader().getResource("seeds-boundaries.yaml");
		longSeeds = MapParser.parseSeedFile(longSeedFile, longSectors);
	}
	
	/**
	 * Looks like the simplest way to control sort order is by method name. If this class
	 * (heaven forbid) has more than 100 methods, naming convention should be adjusted.
	 */
	@Test
	public void test00Moderate(){
		MapWrapper mw = new MapWrapper(moderateSectors);
		
		long tsp, multi, fj;
		long start; 
				
		start = System.nanoTime();
		List<Sector> routeTsp = mw.calcTsp();
		tsp = System.nanoTime() - start;
		
		start = System.nanoTime();
		List<Sector> routeTspMulti = mw.calcTspMulti();
		multi = System.nanoTime() - start;
		
		start = System.nanoTime();
		List<Sector> routeTspFJ = mw.calcTspForkJoin();
		fj = System.nanoTime() - start;
		
		logger.info(String.format("Moderate Length times milli: tsp(%d) multi(%d) fj(%d)", tsp/1000000, multi/1000000, fj/1000000));
		
		final int MIN_BOUND = 23;
		
		assertEquals("Incorrect bound for moderate tsp", mw.getBoundForPath(routeTsp), MIN_BOUND);
		assertEquals("Incorrect bound for moderate tspMulti", mw.getBoundForPath(routeTspMulti), MIN_BOUND);
		assertEquals("Incorrect bound for moderate tspFJ", mw.getBoundForPath(routeTspFJ), MIN_BOUND);
	}
	
	@Test
	public void test50LongTsp(){
		MapWrapper mw = new MapWrapper(longSectors, longSeeds, true);
		
		long start, timeCost; 
				
		start = System.nanoTime();
		List<Sector> routeTsp = mw.calcTsp();
		timeCost = System.nanoTime() - start;
		
		logger.info("Long Length times milli (tsp):" + timeCost/1000000);
		
		final int MIN_BOUND = 38;
		
		assertEquals("Incorrect bound for long tsp", mw.getBoundForPath(routeTsp), MIN_BOUND);
	}
	
	@Test
	public void test51LongTspMulti(){
		MapWrapper mw = new MapWrapper(longSectors, longSeeds, true);
		
		long start, timeCost; 
		
		start = System.nanoTime();
		List<Sector> route = mw.calcTspMulti();
		timeCost = System.nanoTime() - start;
		
		logger.info("Long Length times milli (tspMulti):" + timeCost/1000000);
		
		final int MIN_BOUND = 38;
		
		assertEquals("Incorrect bound for long tspMulti", mw.getBoundForPath(route), MIN_BOUND);
	}
	
	@Test
	public void test52LongTspForkJoin(){
		MapWrapper mw = new MapWrapper(longSectors, longSeeds, true);
		
		long start, timeCost; 
		
		start = System.nanoTime();
		List<Sector> route = mw.calcTspForkJoin();
		timeCost = System.nanoTime() - start;
		
		logger.info("Long Length times milli (tspFj):" + timeCost/1000000);
		
		final int MIN_BOUND = 38;
		
		assertEquals("Incorrect bound for long tspForkJoin", mw.getBoundForPath(route), MIN_BOUND);		
	}
}
