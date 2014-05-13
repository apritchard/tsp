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
	
	private Set<Sector> moderateSectors;
	
	@Before
	public void initialize(){
		URL moderate = CorrectnessTest.class.getClassLoader().getResource("federation-space-boundaries.yaml");
		moderateSectors = MapParser.parseMapFile(moderate);
	}
	
	/**
	 * Looks like the only way to control sort order is by method name. If this class
	 * (heaven forbid) has more than 100 methods, naming convention should be adjusted.
	 */
	@Test
	public void testPerformance00(){
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
}
