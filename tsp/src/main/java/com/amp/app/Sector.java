package com.amp.app;

import java.util.HashMap;
import java.util.Map;

public class Sector {
	String name;
	Map<Sector,Integer> links;
	
	@Override
	public String toString(){
		return name;
//		StringBuilder sb = new StringBuilder();
//		sb.append(name).append(":[");
//		for(Map.Entry<Sector, Integer> entry : links.entrySet()){
//			sb.append("(").append(entry.getKey().getName());
//			sb.append(",").append(entry.getValue()).append(")");
//		}
//		sb.append("]");
//		return sb.toString();
	}
	
	public String getName(){
		return name;
	}
	
	public Sector(String name){
		this.name = name;
		links = new HashMap<>();
	}
	
	public Map<Sector, Integer> getLinks(){
		return links;
	}
	
	public void addLink(Sector sector, int cost){
		links.put(sector, cost);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sector other = (Sector) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
}
