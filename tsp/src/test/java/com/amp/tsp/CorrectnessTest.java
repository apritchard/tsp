package com.amp.tsp;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amp.tsp.mapping.SimulatedAnnealingTspSolver;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.amp.tsp.mapping.BasicOptimizedTspSolver;
import com.amp.tsp.mapping.BasicTspSolver;
import com.amp.tsp.mapping.ForkJoinTspSolver;
import com.amp.tsp.mapping.LambdaSolver;
import com.amp.tsp.mapping.MultiOptimizedTspSolver;
import com.amp.tsp.mapping.MultiTspSolver;
import com.amp.tsp.mapping.NearestNeighborSolver;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspSolution;
import com.amp.tsp.mapping.TspSolution.TspBuilder;
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
	
	private TspBuilder simpleBuilder, simplePartialBuilder, simpleAsymBuilder,
		simpleSeedBuilder, simpleSeedOptionalBuilder, simplePartialSeedBuilder, simplePartialSeedOptionalBuilder;

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
		
		simpleBuilder = TspSolution.forSectors(simpleSectors).build();
		simplePartialBuilder = TspSolution.forSectors(simplePartialSectors).build();
		simpleAsymBuilder = TspSolution.forSectors(simpleAsymSectors).build();
		simpleSeedBuilder = TspSolution.forSectors(simpleSectors).usingSeeds(simpleSeeds).required().build();
		simpleSeedOptionalBuilder = TspSolution.forSectors(simpleSectors).usingSeeds(simpleSeeds).optional().build();
		simplePartialSeedBuilder = TspSolution.forSectors(simplePartialSectors).usingSeeds(simplePartialSeeds).required().build();
		simplePartialSeedOptionalBuilder = TspSolution.forSectors(simplePartialSectors).usingSeeds(simplePartialSeeds).optional().build();
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
		
		testSolver(new BasicTspSolver(simpleBuilder), SIMPLE_BOUND);
		testSolver(new BasicTspSolver(simplePartialBuilder), PARTIAL_BOUND);
		testSolver(new BasicTspSolver(simpleAsymBuilder), ASYM_BOUND);
		testSolver(new BasicTspSolver(simpleSeedOptionalBuilder), SEEDS_BOUND);
		testSolver(new BasicTspSolver(simpleSeedBuilder), SEEDS_ONLY_BOUND);
		testSolver(new BasicTspSolver(simplePartialSeedOptionalBuilder), PARTIAL_BOUND);
		testSolver(new BasicTspSolver(simplePartialSeedBuilder), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testSimpleOptimizedTsp() {
		testSolver(new BasicOptimizedTspSolver(simpleBuilder), SIMPLE_BOUND);
		testSolver(new BasicOptimizedTspSolver(simplePartialBuilder), PARTIAL_BOUND);
		testSolver(new BasicOptimizedTspSolver(simpleAsymBuilder), ASYM_BOUND);
		testSolver(new BasicOptimizedTspSolver(simpleSeedOptionalBuilder), SEEDS_BOUND);
		testSolver(new BasicOptimizedTspSolver(simpleSeedBuilder), SEEDS_ONLY_BOUND);
		testSolver(new BasicOptimizedTspSolver(simplePartialSeedOptionalBuilder), PARTIAL_BOUND);
		testSolver(new BasicOptimizedTspSolver(simplePartialSeedBuilder), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testSimpleTspMulti(){
		testSolver(new MultiTspSolver(simpleBuilder), SIMPLE_BOUND);
		testSolver(new MultiTspSolver(simplePartialBuilder), PARTIAL_BOUND);
		testSolver(new MultiTspSolver(simpleAsymBuilder), ASYM_BOUND);
		testSolver(new MultiTspSolver(simpleSeedOptionalBuilder), SEEDS_BOUND);
		testSolver(new MultiTspSolver(simpleSeedBuilder), SEEDS_ONLY_BOUND);
		testSolver(new MultiTspSolver(simplePartialSeedOptionalBuilder), PARTIAL_BOUND);
		testSolver(new MultiTspSolver(simplePartialSeedBuilder), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testSimpleTspMultiOptimized(){
		testSolver(new MultiOptimizedTspSolver(simpleBuilder), SIMPLE_BOUND);
		testSolver(new MultiOptimizedTspSolver(simplePartialBuilder), PARTIAL_BOUND);
		testSolver(new MultiOptimizedTspSolver(simpleAsymBuilder), ASYM_BOUND);
		testSolver(new MultiOptimizedTspSolver(simpleSeedOptionalBuilder), SEEDS_BOUND);
		testSolver(new MultiOptimizedTspSolver(simpleSeedBuilder), SEEDS_ONLY_BOUND);
		testSolver(new MultiOptimizedTspSolver(simplePartialSeedOptionalBuilder), PARTIAL_BOUND);
		testSolver(new MultiOptimizedTspSolver(simplePartialSeedBuilder), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testNearestNeighbor(){
		testSolver(new NearestNeighborSolver(simpleBuilder), SIMPLE_BOUND);
		testSolver(new NearestNeighborSolver(simplePartialBuilder), PARTIAL_BOUND);
		testSolver(new NearestNeighborSolver(simpleAsymBuilder), ASYM_BOUND);
		testSolver(new NearestNeighborSolver(simpleSeedOptionalBuilder), SEEDS_BOUND);
		testSolver(new NearestNeighborSolver(simpleSeedBuilder), SEEDS_ONLY_BOUND);
		testSolver(new NearestNeighborSolver(simplePartialSeedOptionalBuilder), PARTIAL_BOUND);
		testSolver(new NearestNeighborSolver(simplePartialSeedBuilder), PARTIAL_SEEDS_ONLY_BOUND);
	}	
	
	@Test
	public void testSimpleTspForkJoin(){
		testSolver(new ForkJoinTspSolver(simpleBuilder), SIMPLE_BOUND);
		testSolver(new ForkJoinTspSolver(simplePartialBuilder), PARTIAL_BOUND);
		testSolver(new ForkJoinTspSolver(simpleAsymBuilder), ASYM_BOUND);
		testSolver(new ForkJoinTspSolver(simpleSeedOptionalBuilder), SEEDS_BOUND);
		testSolver(new ForkJoinTspSolver(simpleSeedBuilder), SEEDS_ONLY_BOUND);
		testSolver(new ForkJoinTspSolver(simplePartialSeedOptionalBuilder), PARTIAL_BOUND);
		testSolver(new ForkJoinTspSolver(simplePartialSeedBuilder), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	@Test
	public void testLambda(){
		testSolver(new LambdaSolver(simpleBuilder), SIMPLE_BOUND);
		testSolver(new LambdaSolver(simplePartialBuilder), PARTIAL_BOUND);
		testSolver(new LambdaSolver(simpleAsymBuilder), ASYM_BOUND);
		testSolver(new LambdaSolver(simpleSeedOptionalBuilder), SEEDS_BOUND);
		testSolver(new LambdaSolver(simpleSeedBuilder), SEEDS_ONLY_BOUND);
		testSolver(new LambdaSolver(simplePartialSeedOptionalBuilder), PARTIAL_BOUND);
		testSolver(new LambdaSolver(simplePartialSeedBuilder), PARTIAL_SEEDS_ONLY_BOUND);
	}

	@Test
	@Ignore
	public void testSimulatedAnnealing(){
		testSolver(new SimulatedAnnealingTspSolver(simpleBuilder), SIMPLE_BOUND);
		testSolver(new SimulatedAnnealingTspSolver(simplePartialBuilder), PARTIAL_BOUND);
		testSolver(new SimulatedAnnealingTspSolver(simpleAsymBuilder), ASYM_BOUND);
//		testSolver(new SimulatedAnnealingTspSolver(simpleSeedOptionalBuilder), SEEDS_BOUND);
//		testSolver(new SimulatedAnnealingTspSolver(simpleSeedBuilder), SEEDS_ONLY_BOUND);
//		testSolver(new SimulatedAnnealingTspSolver(simplePartialSeedOptionalBuilder), PARTIAL_BOUND);
//		testSolver(new SimulatedAnnealingTspSolver(simplePartialSeedBuilder), PARTIAL_SEEDS_ONLY_BOUND);
	}
	
	private void testSolver(TspSolver solver, int expectedBound){
		List<Sector> route = solver.solve();
		logger.info("Bound for " + route + " " + solver.getBoundForPath(route));
		assertEquals("Incorrect bound for simple", solver.getBoundForPath(route), expectedBound);
	}
}
