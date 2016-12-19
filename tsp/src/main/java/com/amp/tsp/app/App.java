package com.amp.tsp.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;

import com.amp.tsp.mapping.SimulatedAnnealingTspSolver;
import org.apache.log4j.Logger;

import com.amp.tsp.mapping.MultiOptimizedNearestNeighborTspSolver;
import com.amp.tsp.mapping.Sector;
import com.amp.tsp.mapping.TspSolution;
import com.amp.tsp.mapping.TspSolver;
import com.amp.tsp.parse.MapParser;

public class App {
	
	private static final Logger logger = Logger.getLogger(App.class);

	public static void main(String[] args) {
		
		try{
			InputStream is = App.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(is);
		} catch (IOException ioe){
			logger.warn("Failed to find logger.properties");
		}
		
//		URL mapFile = App.class.getClassLoader().getResource("asymmetric.yaml");
//		URL mapFile = App.class.getClassLoader().getResource("boundaries.yaml");
//		URL mapFile = App.class.getClassLoader().getResource("federation-space-boundaries.yaml");
//		URL mapFile = App.class.getClassLoader().getResource("Season10-Alpha.yaml");
//		URL mapFile = App.class.getClassLoader().getResource("season10-alpha-quadrant.yaml");
		URL mapFile = App.class.getClassLoader().getResource("warp-point.yaml");
		
		Set<Sector> sectors = MapParser.parseMapFile(mapFile);
		
//		URL seedFile = App.class.getClassLoader().getResource("seeds-season10-alpha.yaml");
//		List<List<Sector>> seeds = MapParser.parseSeedFile(seedFile, sectors);
		
		TspSolver mw = TspSolution.forSectors(sectors).accuracy(3);

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
		logger.debug(sb.toString());
		
		List<Sector> route = mw.solve();
		
		sb = new StringBuilder();
		sb.append("Best route: ").append(System.lineSeparator());
		for(Sector s : route){
			sb.append(s).append(", ");
		}
		sb.append("Cost: ").append(mw.getBoundForPath(route));
		logger.info(sb.toString());
	}
}
