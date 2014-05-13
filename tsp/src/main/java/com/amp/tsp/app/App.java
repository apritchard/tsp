package com.amp.tsp.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.amp.tsp.mapping.MapWrapper;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.parse.MapParser;

public class App {
	
	private static final Logger logger = Logger.getLogger(App.class.getName());

	public static void main(String[] args) {
		
		try{
			InputStream is = App.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(is);
		} catch (IOException ioe){
			logger.warning("Failed to find logger.properties");
		}
		
//		URL mapFile = App.class.getClassLoader().getResource("boundaries.yaml");
		URL mapFile = App.class.getClassLoader().getResource("federation-space-boundaries.yaml");
		Set<Sector> sectors = MapParser.parseMapFile(mapFile);
		
//		URL seedFile = App.class.getClassLoader().getResource("seeds-boundaries.yaml");
//		List<List<Sector>> seeds = MapParser.parseSeedFile(seedFile, sectors);
		
//		List<List<Sector>> seeds = new ArrayList<>();
		
//		MapWrapper mw = new MapWrapper(sectors, seeds, true);
		MapWrapper mw = new MapWrapper(sectors);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Sectors:").append(System.lineSeparator()).append(mw);
		logger.info(sb.toString());
		
		sb = new StringBuilder();
		sb.append("Edges:").append(System.lineSeparator());
		for(Sector s1 : sectors){
			sb.append(s1);
			for(Sector s2 : sectors){
				sb.append(System.lineSeparator()).append("\t");
				sb.append(mw.getDistance(s1, s2)).append("\t").append(s2);
			}
			sb.append("*****").append(System.lineSeparator());
		}
		logger.finer(sb.toString());
		
		
//		List<Sector> route = mw.calcTsp();
//		List<Sector> route = mw.calcTspMulti();
		List<Sector> route = mw.calcTspForkJoin();
		
		sb = new StringBuilder();
		sb.append("Best route: ").append(System.lineSeparator());
		for(Sector s : route){
			sb.append(s).append(", ");
		}
		sb.append("Cost: ").append(mw.getBoundForPath(route));
		logger.info(sb.toString());
	}
}
