package com.amp.tsp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.amp.app.App;
import com.amp.mapping.MapWrapper;
import com.amp.mapping.Sector;
import com.amp.mapping.TspUtilities;
import com.amp.parse.MapParser;

public class CorrectnessTest {
	
	Logger logger = Logger.getLogger(CorrectnessTest.class.getName());

	Set<Sector> simpleSectors, simpleIncompleteSectors;

	@Before
	public void initialize(){
		try{
			InputStream is = App.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(is);
		} catch (IOException ioe){
			logger.warning("Failed to find logger.properties");
		}		
		
		URL simple = CorrectnessTest.class.getClassLoader().getResource("simple.yaml");
		URL simpleIncomplete = CorrectnessTest.class.getClassLoader().getResource("simple-incomplete.yaml");
		
		simpleSectors = MapParser.parseMapFile(simple);
		simpleIncompleteSectors = MapParser.parseMapFile(simpleIncomplete);
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
		Map<Sector, Map<Sector,Integer>> m = TspUtilities.calculateShorestPaths(simpleIncompleteSectors);
		Map<Sector,Integer> aEdges = m.get(new Sector("A"));
		assertEquals(aEdges.get(new Sector("B")), new Integer(5));
		assertEquals(aEdges.get(new Sector("C")), new Integer(4));
		assertEquals(aEdges.get(new Sector("D")), new Integer(3));
	}
	
	@Test
	public void testSimpleTsp() {

		MapWrapper mw = new MapWrapper(simpleSectors);
		List<Sector> route = mw.calcTsp();
		
		assertEquals("Incorrect bound for simple", mw.getBoundForPath(route), 6); 
		
		mw = new MapWrapper(simpleIncompleteSectors);
		route = mw.calcTsp();

		assertEquals("Incorrect bound for simple incomplete", mw.getBoundForPath(route), 8);
	}
	
	@Test
	public void testSimpleTspMulti(){
		MapWrapper mw = new MapWrapper(simpleSectors);
		List<Sector> route = mw.calcTspMulti();
		
		assertEquals("Incorrect bound for simple", mw.getBoundForPath(route), 6); 
		
		mw = new MapWrapper(simpleIncompleteSectors);
		route = mw.calcTspMulti();
		
		assertEquals("Incorrect bound for simple incomplete", mw.getBoundForPath(route), 8);
	}
	
	@Test
	public void testSimpleTspForkJoin(){
		MapWrapper mw = new MapWrapper(simpleSectors);
		List<Sector> route = mw.calcTspForkJoin();
		
		assertEquals("Incorrect bound for simple", mw.getBoundForPath(route), 6); 
		
		mw = new MapWrapper(simpleIncompleteSectors);
		route = mw.calcTspForkJoin();
		
		assertEquals("Incorrect bound for simple incomplete", mw.getBoundForPath(route), 8);
	}

}
