package com.amp.app;

import java.util.List;

public class App {
	private static int COST = 1;
	private static int ZONE_COST = 2;
	
	public static void main(String[] args) {
		
		MapWrapper mw = new MapWrapper();
		
		String[] sectors = {
				"Z-6", "Tarod", "Bolarus", "Devron",
				"Teneebia", "Sierra", "Vendor",
				"Hyralan", "Onias", "Mylasa",
				"Azure", "Nimbus", "Narendra",
				"Vulcan", "Orion", "Risa",
				"Argelius", "Celes", "Kassae",
				"Xarantine", "Hromi", "Mempa",
				"Aldebaren", "Donatu", "Archanis",
				"Cardassia", "Bajor", "Kalandra",
				"Algira", "Almatha", "Dorvan",
				"Arawath", "Kora", "Vanden", "Orias",
				"Raveh", "Deferi",
				"Galdonterre", "Cestus", "Mutara", "Bellatrix",
				"Pelia"
		};
		
		String[][] links = {
				{"Z-6", "Tarod"},
				{"Z-6", "Bolarus"},
				{"Bolarus", "Devron"},
				{"Tarod", "Devron"},
				{"Teneebia", "Sierra"},
				{"Sierra", "Vendor"},
				{"Hyralan", "Onias"},
				{"Onias", "Mylasa"},
				{"Azure", "Nimbus"},
				{"Nimbus", "Narendra"},
				{"Xarantine", "Hromi"},
				{"Hromi", "Mempa"},
				{"Vulcan", "Orion"},
				{"Orion", "Risa"},
				{"Argelius", "Celes"},
				{"Celes", "Kassae"},
				{"Aldebaren", "Donatu"},
				{"Donatu", "Archanis"},
				{"Cardassia", "Bajor"},
				{"Bajor", "Kalandra"},
				{"Algira", "Almatha"},
				{"Almatha", "Dorvan"},
				{"Arawath", "Kora"},
				{"Arawath", "Vanden"},
				{"Kora", "Orias"},
				{"Vanden", "Orias"},
				{"Raveh", "Deferi"},
				{"Galdonterre", "Cestus"},
				{"Galdonterre", "Mutara"},
				{"Cestus", "Bellatrix"},
				{"Mutara", "Bellatrix"}
		};
		
		String[][] zoneLinks = {
				{"Bolarus", "Teneebia"},
				{"Devron", "Sierra"},
				{"Teneebia", "Vulcan"},
				{"Sierra", "Argelius"},
				{"Vendor", "Hyralan"},
				{"Hyralan", "Azure"},
				{"Hyralan", "Argelius"},
				{"Onias", "Nimbus"},
				{"Mylasa", "Narendra"},
				{"Azure", "Celes"},
				{"Azure", "Xarantine"},
				{"Nimbus", "Hromi"},
				{"Narendra", "Mempa"},
				{"Xarantine", "Kassae"},
				{"Xarantine", "Archanis"},
				{"Argelius", "Vulcan"},
				{"Celes", "Orion"},
				{"Kassae", "Risa"},
				{"Kassae", "Donatu"},
				{"Orion", "Kalandra"},
				{"Risa", "Dorvan"},
				{"Risa", "Cestus"},
				{"Risa", "Aldebaren"},
				{"Aldebaren", "Deferi"},
				{"Kalandra", "Dorvan"},
				{"Bajor", "Almatha"},
				{"Cardassia", "Algira"},
				{"Cardassia", "Deferi"},
				{"Cardassia", "Kora"},
				{"Orias", "Algira"},
				{"Mutara", "Pelia"}
		};
		
		//populate the sectors set
		for (String sector : sectors){
			mw.addSector(sector);
		}
		
		//link adjacent sectors
		for (String[] pair : links){
			mw.linkSectors(pair[0], pair[1], COST);
		}
		
		//link sectors by zone borders
		for (String[] pair : zoneLinks){
			mw.linkSectors(pair[0], pair[1], ZONE_COST);
		}
		
//		System.out.println("Map:");
//		System.out.println(mw);
		
		//calc shortest path from each sector to each other sector
		mw.calcShortestPaths();
		
//		StringBuilder sb = new StringBuilder();
//		for(String s1 : sectors){
//			sb.append(s1);
//			for(String s2 : sectors){
//				sb.append(System.lineSeparator()).append("\t");
//				sb.append(mw.getDistance(s1, s2)).append("\t").append(s2);
//			}
//			sb.append("*****").append(System.lineSeparator());
//		}
//		System.out.println(sb.toString());
		
		//branch and bound traveling salesman problem
		String[] seed1 = {
				"Teneebia", "Bolarus", "Z-6", "Tarod", "Devron",
				"Sierra", "Vendor", "Hyralan", "Onias", "Mylasa",
				"Narendra", "Nimbus", "Azure", "Hromi", "Mempa", "Xarantine",
				"Archanis", "Donatu", "Aldebaren", "Kassae", "Celes","Argelius",
				"Vulcan", "Orion", "Kalandra", "Bajor", "Cardassia", "Deferi", "Raveh",
				"Kora", "Arawath", "Vanden", "Orias", "Algira", "Almatha", "Dorvan",
				"Risa", "Cestus", "Bellatrix", "Mutara", "Galdonterre", "Pelia"
		};
		mw.seedPath(seed1);
		
		String[] seed2 = {
				"Teneebia", "Bolarus", "Z-6", "Tarod", "Devron",
				"Sierra", "Vendor", "Hyralan", "Onias", "Mylasa"};
		mw.seedPath(seed2);
		
		List<Sector> route = mw.calcTsp();
		StringBuilder sb = new StringBuilder();
		sb.append("Best route: ").append(System.lineSeparator());
		for(Sector s : route){
			sb.append(s).append(", ");
		}
		System.out.println(sb.toString());
	}
}
