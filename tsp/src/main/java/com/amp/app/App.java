package com.amp.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.amp.mapping.MapWrapper;
import com.amp.mapping.Sector;
import com.amp.parse.MapParser;

public class App {
	
	private static final Logger logger = Logger.getLogger(App.class.getName());

	public static void main(String[] args) {
		
		try{
			InputStream is = App.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(is);
		} catch (IOException ioe){
			logger.warning("Failed to find logger.properties");
		}
		
		URL mapFile = App.class.getClassLoader().getResource("boundaries.yaml");
		Set<Sector> sectors = MapParser.parseMapFile(mapFile);
		
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
		
//		//branch and bound traveling salesman problem
//		String[] seed1 = {
//				"Teneebia", "Bolarus", "Z-6", "Tarod", "Devron",
//				"Sierra", "Vendor", "Hyralan", "Onias", "Mylasa",
//				"Narendra", "Nimbus", "Azure", "Hromi", "Mempa", "Xarantine",
//				"Archanis", "Donatu", "Aldebaren", "Kassae", "Celes","Argelius",
//				"Vulcan", "Orion", "Kalandra", "Bajor", "Cardassia", "Deferi", "Raveh",
//				"Kora", "Arawath", "Vanden", "Orias", "Algira", "Almatha", "Dorvan",
//				"Risa", "Cestus", "Bellatrix", "Mutara", "Galdonterre", "Pelia"
//		};
//		mw.seedPath(seed1);
//		
//		String[] seed2 = {
//				"Teneebia", "Bolarus", "Z-6", "Tarod", "Devron",
//				"Sierra", "Vendor", "Hyralan", "Onias", "Mylasa"};
//		mw.seedPath(seed2);
		
//		List<Sector> route = mw.calcTsp();
		List<Sector> route = mw.calcTspMulti();
		sb = new StringBuilder();
		sb.append("Best route: ").append(System.lineSeparator());
		for(Sector s : route){
			sb.append(s).append(", ");
		}
		logger.info(sb.toString());
	}
}
