package com.amp.tsp;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.amp.tsp.mapping.MapWrapper;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspUtilities;
import com.amp.tsp.parse.MapParser;

/**
 * Test class that checks for basic correctness of the algorithms used by this project.
 * 
 * @author alex
 *
 */
public class CorrectnessTest {
	
	private static final Logger logger = Logger.getLogger(CorrectnessTest.class.getName());

	private Set<Sector> simpleSectors, simplePartialSectors;
	private List<List<Sector>> simpleSeeds, simplePartialSeeds;

	@Before
	public void initialize(){
		URL simple = CorrectnessTest.class.getClassLoader().getResource("simple.yaml");
		URL simplePartial = CorrectnessTest.class.getClassLoader().getResource("simple-partial.yaml");
		
		URL seedsSimple = CorrectnessTest.class.getClassLoader().getResource("seeds-simple.yaml");
		
		simpleSectors = MapParser.parseMapFile(simple);
		simplePartialSectors = MapParser.parseMapFile(simplePartial);
		
		simpleSeeds = MapParser.parseSeedFile(seedsSimple, simpleSectors);
		simplePartialSeeds = MapParser.parseSeedFile(seedsSimple, simpleSectors);
	}
	
	@Test
	public void testSimpleShortestPaths(){
		Map<Sector, Map<Sector,Integer>> m = TspUtilities.calculateShorestPaths(simpleSectors);
		Map<Sector,Integer> aEdges = m.get(new Sector("A"));
		assertEquals(aEdges.get(new Sector("B")), new Integer(2));
		assertEquals(aEdges.get(new Sector("C")), new Integer(3));
		assertEquals(aEdges.get(new Sector("D")), new Integer(3));
	}
	
	@Test
	public void testIncompleteShortestPaths(){
		Map<Sector, Map<Sector,Integer>> m = TspUtilities.calculateShorestPaths(simplePartialSectors);
		Map<Sector,Integer> aEdges = m.get(new Sector("A"));
		assertEquals(aEdges.get(new Sector("B")), new Integer(5));
		assertEquals(aEdges.get(new Sector("C")), new Integer(4));
		assertEquals(aEdges.get(new Sector("D")), new Integer(3));
	}
	
	@Test
	public void testSimpleTsp() {
		MapWrapper mw = new MapWrapper(simpleSectors);
		testSimple(mw, mw.calcTsp());
		
		mw = new MapWrapper(simplePartialSectors);
		testSimplePartial(mw, mw.calcTsp());
	}
	
	@Test
	public void testSimpleTspMulti(){
		MapWrapper mw = new MapWrapper(simpleSectors);
		testSimple(mw, mw.calcTspMulti()); 
		
		mw = new MapWrapper(simplePartialSectors);
		testSimplePartial(mw, mw.calcTspMulti());
	}
	
	@Test
	public void testSimpleTspForkJoin(){
		MapWrapper mw = new MapWrapper(simpleSectors);
		testSimple(mw, mw.calcTspForkJoin());
		
		mw = new MapWrapper(simplePartialSectors);
		testSimplePartial(mw, mw.calcTspForkJoin());		
	}
	
	@Test
	public void testSeedTsp(){
		MapWrapper mw = new MapWrapper(simpleSectors, simpleSeeds, false);
		testSimple(mw, mw.calcTsp());
		
		mw = new MapWrapper(simpleSectors, simpleSeeds, true);
		testSimpleSeedsOnly(mw, mw.calcTsp());
		
		mw = new MapWrapper(simplePartialSectors, simpleSeeds, false);
		testSimplePartial(mw, mw.calcTsp());
		
		mw = new MapWrapper(simplePartialSectors, simpleSeeds, true);
		testSimplePartialSeedsOnly(mw, mw.calcTsp());
	}
	
	@Test
	public void testSeedTspMulti(){
		MapWrapper mw = new MapWrapper(simpleSectors, simpleSeeds, false);
		testSimple(mw, mw.calcTspMulti());
		
		mw = new MapWrapper(simpleSectors, simpleSeeds, true);
		testSimpleSeedsOnly(mw, mw.calcTspMulti());
		
		mw = new MapWrapper(simplePartialSectors, simpleSeeds, false);
		testSimplePartial(mw, mw.calcTspMulti());
		
		mw = new MapWrapper(simplePartialSectors, simpleSeeds, true);
		testSimplePartialSeedsOnly(mw, mw.calcTspMulti());
	}
	
	@Test
	public void testSeedTspForkJoin(){
		MapWrapper mw = new MapWrapper(simpleSectors, simpleSeeds, false);
		testSimple(mw, mw.calcTspForkJoin());
		
		mw = new MapWrapper(simpleSectors, simpleSeeds, true);
		testSimpleSeedsOnly(mw, mw.calcTspForkJoin());
		
		mw = new MapWrapper(simplePartialSectors, simpleSeeds, false);
		testSimplePartial(mw, mw.calcTspForkJoin());
		
		mw = new MapWrapper(simplePartialSectors, simpleSeeds, true);
		testSimplePartialSeedsOnly(mw, mw.calcTspForkJoin());
	}
	
	private void testSimple(MapWrapper mw, List<Sector> route){
		final int LENGTH = 6;
		assertEquals("Incorrect bound for simple", mw.getBoundForPath(route), LENGTH);
	}
	
	private void testSimplePartial(MapWrapper mw, List<Sector> route){
		final int LENGTH = 8;
		assertEquals("Incorrect bound for simple incomplete", mw.getBoundForPath(route), LENGTH);
	}
	
	private void testSimpleSeedsOnly(MapWrapper mw, List<Sector> route){
		final int LENGTH = 6;
		assertEquals("Incorrect bound for simple with starting seeds only", mw.getBoundForPath(route), LENGTH);
	}
	
	private void testSimplePartialSeedsOnly(MapWrapper mw, List<Sector> route){
		final int LENGTH = 9;
		assertEquals("Incorrect bound for simple partial with starting seeds only", mw.getBoundForPath(route), LENGTH);
	}
}
