package com.amp.app;

import java.net.URL;
import java.util.List;
import java.util.Set;

import com.amp.mapping.MapWrapper;
import com.amp.mapping.Sector;
import com.amp.parse.MapParser;

public class App {

	public static void main(String[] args) {
		
		URL mapFile = App.class.getClassLoader().getResource("federation-space.yaml");
		Set<Sector> sectors = MapParser.parseMapFile(mapFile);
		
		MapWrapper mw = new MapWrapper(sectors);
		
		System.out.println("Map:");
		System.out.println(mw);
		
		StringBuilder sb = new StringBuilder();
		for(Sector s1 : sectors){
			sb.append(s1);
			for(Sector s2 : sectors){
				sb.append(System.lineSeparator()).append("\t");
				sb.append(mw.getDistance(s1, s2)).append("\t").append(s2);
			}
			sb.append("*****").append(System.lineSeparator());
		}
		System.out.println(sb.toString());
		
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
		
		List<Sector> route = mw.calcTsp();
		sb = new StringBuilder();
		sb.append("Best route: ").append(System.lineSeparator());
		for(Sector s : route){
			sb.append(s).append(", ");
		}
		System.out.println(sb.toString());
	}
}
