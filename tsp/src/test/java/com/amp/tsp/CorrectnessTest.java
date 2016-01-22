package com.amp.tsp;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.amp.tsp.mapping.BasicOptimizedTspSolver;
import com.amp.tsp.mapping.BasicTspSolver;
import com.amp.tsp.mapping.ForkJoinTspSolver;
import com.amp.tsp.mapping.LambdaSolver;
import com.amp.tsp.mapping.MultiOptimizedTspSolver;
import com.amp.tsp.mapping.MultiTspSolver;
import com.amp.tsp.mapping.NearestNeighborSolver;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspSolver;
import com.amp.tsp.mapping.TspUtilities;
import com.amp.tsp.parse.MapParser;

/**
 * Test class that checks for basic correctness of the algorithms used by this project.
 * 
 * @author alex
 *
 */
public class CorrectnessTest {
	
	private static final Logger logger = Logger.getLogger(CorrectnessTest.class);

	private final int SIMPLE_BOUND = 6;
	private final int ASYM_BOUND = 6;
	private final int SEEDS_BOUND = 6;
	private final int SEEDS_ONLY_BOUND = 6;
	private final int PARTIAL_BOUND = 8;
	private final int PARTIAL_SEEDS_ONLY_BOUND = 9;
	
	private Set<Sector> simpleSectors, simplePartialSectors, simpleAsymSectors;
	private List<List<Sector>> simpleSeeds, simplePartialSeeds;

	@Before
	public void initialize(){
		URL simple = CorrectnessTest.class.getClassLoader().getResource("simple.yaml");
		URL simpleAsym = CorrectnessTest.class.getClassLoader().getResource("asymmetric.yaml");
		URL simplePartial = CorrectnessTest.class.getClassLoader().getResource("simple-partial.yaml");
		
		URL seedsSimple = CorrectnessTest.class.getClassLoader().getResource("seeds-simple.yaml");
		
		simpleSectors = MapParser.parseMapFile(simple);
		simpleAsymSectors = MapParser.parseMapFile(simpleAsym);
		simplePartialSectors = MapParser.parseMapFile(simplePartial);
		
		simpleSeeds = MapParser.parseSeedFile(seedsSimple, simpleSectors);
		simplePartialSeeds = MapParser.parseSeedFile(seedsSimple, simpleSectors);
	}
	
	@Test
	public void testSimpleShortestPaths(){
		Map<Sector, Map<Sector,Integer>> m = TspUtilities.calculateShortestPaths(simpleSectors);
		Map<Sector,Integer> aEdges = m.get(new Sector("A"));
		assertEquals(aEdges.get(new Sector("B")), new Integer(2));
		assertEquals(aEdges.get(new Sector("C")), new Integer(3));
		assertEquals(aEdges.get(new Sector("D")), new Integer(3));
	}
	
	@Test
	public void testIncompleteShortestPaths(){
		Map<Sector, Map<Sector,Integer>> m = TspUtilities.calculateShortestPaths(simplePartialSectors);
		Map<Sector,Integer> aEdges = m.get(new Sector("A"));
		assertEquals(aEdges.get(new Sector("B")), new Integer(5));
		assertEquals(aEdges.get(new Sector("C")), new Integer(4));
		assertEquals(aEdges.get(new Sector("D")), new Integer(3));
	}
	@Test
	public void testSimpleShortestPathsStatic(){
		Map<Sector, Map<Sector,Integer>> m = TspUtilities.calculateShortestPathsStatic(simpleSectors);
		Map<Sector,Integer> aEdges = m.get(new Sector("A"));
		assertEquals(aEdges.get(new Sector("B")), new Integer(2));
		assertEquals(aEdges.get(new Sector("C")), new Integer(4));
		assertEquals(aEdges.get(new Sector("D")), new Integer(3));
	}
	
	@Test
	public void testIncompleteShortestPathsStatic(){
		Map<Sector, Map<Sector,Integer>> m = TspUtilities.calculateShortestPathsStatic(simplePartialSectors);
		Map<Sector,Integer> aEdges = m.get(new Sector("A"));
		assertEquals(aEdges.get(new Sector("B")), new Integer(6));
		assertEquals(aEdges.get(new Sector("C")), new Integer(4));
		assertEquals(aEdges.get(new Sector("D")), new Integer(3));
	}
	
	@Test
	public void testSimpleTsp() {
		testSolver(new BasicTspSolver(simpleSectors), SIMPLE_BOUND);
		testSolver(new BasicTspSolver(simplePartialSectors), PARTIAL_BOUND);
		testSolver(new BasicTspSolver(simpleAsymSectors), ASYM_BOUND);
		testSolver(new BasicTspSolver(simpleSectors, simpleSeeds, false), SEEDS_BOUND);
		testSolver(new BasicTspSolver(simpleSectors, simpleSeeds, true), SEEDS_ONLY_BOUND);
		testSolver(new BasicTspSolver(simplePartialSectors, simplePartialSeeds, false), PARTIAL_BOUND);
		testSolver(new BasicTspSolver(simplePartialSectors, simplePartialSeeds, true), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testSimpleOptimizedTsp() {
		testSolver(new BasicOptimizedTspSolver(simpleSectors), SIMPLE_BOUND);
		testSolver(new BasicOptimizedTspSolver(simplePartialSectors), PARTIAL_BOUND);
		testSolver(new BasicOptimizedTspSolver(simpleAsymSectors), ASYM_BOUND);
		testSolver(new BasicOptimizedTspSolver(simpleSectors, simpleSeeds, false), SEEDS_BOUND);
		testSolver(new BasicOptimizedTspSolver(simpleSectors, simpleSeeds, true), SEEDS_ONLY_BOUND);
		testSolver(new BasicOptimizedTspSolver(simplePartialSectors, simplePartialSeeds, false), PARTIAL_BOUND);
		testSolver(new BasicOptimizedTspSolver(simplePartialSectors, simplePartialSeeds, true), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testSimpleTspMulti(){
		testSolver(new MultiTspSolver(simpleSectors), SIMPLE_BOUND);
		testSolver(new MultiTspSolver(simplePartialSectors), PARTIAL_BOUND);
		testSolver(new MultiTspSolver(simpleAsymSectors), ASYM_BOUND);
		testSolver(new MultiTspSolver(simpleSectors, simpleSeeds, false), SEEDS_BOUND);
		testSolver(new MultiTspSolver(simpleSectors, simpleSeeds, true), SEEDS_ONLY_BOUND);
		testSolver(new MultiTspSolver(simplePartialSectors, simplePartialSeeds, false), PARTIAL_BOUND);
		testSolver(new MultiTspSolver(simplePartialSectors, simplePartialSeeds, true), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testSimpleTspMultiOptimized(){
		testSolver(new MultiOptimizedTspSolver(simpleSectors), SIMPLE_BOUND);
		testSolver(new MultiOptimizedTspSolver(simplePartialSectors), PARTIAL_BOUND);
		testSolver(new MultiOptimizedTspSolver(simpleAsymSectors), ASYM_BOUND);
		testSolver(new MultiOptimizedTspSolver(simpleSectors, simpleSeeds, false), SEEDS_BOUND);
		testSolver(new MultiOptimizedTspSolver(simpleSectors, simpleSeeds, true), SEEDS_ONLY_BOUND);
		testSolver(new MultiOptimizedTspSolver(simplePartialSectors, simplePartialSeeds, false), PARTIAL_BOUND);
		testSolver(new MultiOptimizedTspSolver(simplePartialSectors, simplePartialSeeds, true), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testNearestNeighbor(){
		testSolver(new NearestNeighborSolver(3, simpleSectors), SIMPLE_BOUND);
		testSolver(new NearestNeighborSolver(3, simplePartialSectors), PARTIAL_BOUND);
		testSolver(new NearestNeighborSolver(3, simpleAsymSectors), ASYM_BOUND);
		testSolver(new NearestNeighborSolver(3, simpleSectors, simpleSeeds, false), SEEDS_BOUND);
		testSolver(new NearestNeighborSolver(3, simpleSectors, simpleSeeds, true), SEEDS_ONLY_BOUND);
		testSolver(new NearestNeighborSolver(3, simplePartialSectors, simplePartialSeeds, false), PARTIAL_BOUND);
		testSolver(new NearestNeighborSolver(3, simplePartialSectors, simplePartialSeeds, true), PARTIAL_SEEDS_ONLY_BOUND);
	}	
	
	@Test
	public void testSimpleTspForkJoin(){
		testSolver(new ForkJoinTspSolver(simpleSectors), SIMPLE_BOUND);
		testSolver(new ForkJoinTspSolver(simplePartialSectors), PARTIAL_BOUND);
		testSolver(new ForkJoinTspSolver(simpleAsymSectors), ASYM_BOUND);
		testSolver(new ForkJoinTspSolver(simpleSectors, simpleSeeds, false), SEEDS_BOUND);
		testSolver(new ForkJoinTspSolver(simpleSectors, simpleSeeds, true), SEEDS_ONLY_BOUND);
		testSolver(new ForkJoinTspSolver(simplePartialSectors, simplePartialSeeds, false), PARTIAL_BOUND);
		testSolver(new ForkJoinTspSolver(simplePartialSectors, simplePartialSeeds, true), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testLambda(){
//		testSolver(new LambdaSolver(simpleSectors), SIMPLE_BOUND);
//		testSolver(new LambdaSolver(simplePartialSectors), PARTIAL_BOUND);
//		testSolver(new LambdaSolver(simpleAsymSectors), ASYM_BOUND);
//		testSolver(new LambdaSolver(simpleSectors, simpleSeeds, false), SEEDS_BOUND);
//		testSolver(new LambdaSolver(simpleSectors, simpleSeeds, true), SEEDS_ONLY_BOUND);
		testSolver(new LambdaSolver(simplePartialSectors, simplePartialSeeds, false), PARTIAL_BOUND);
		testSolver(new LambdaSolver(simplePartialSectors, simplePartialSeeds, true), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	private void testSolver(TspSolver solver, int expectedBound){
		List<Sector> route = solver.solve();
		logger.info("Bound for " + route + " " + solver.getBoundForPath(route));
		assertEquals("Incorrect bound for simple", solver.getBoundForPath(route), expectedBound);
	}
}
