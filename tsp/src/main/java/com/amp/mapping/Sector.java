package com.amp.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Pojo containing the name a sector and its edge list
 * @author alex
 *
 */
public class Sector {
	String name;
	Map<Sector,Integer> edgeList;
	
	@Override
	public String toString(){
		return name;
	}
		
	public String getName(){
		return name;
	}
	
	public Sector(String name){
		this.name = name;
		edgeList = new HashMap<>();
	}
	
	public Map<Sector, Integer> getEdgeList(){
		return edgeList;
	}
	
	public void addEdge(Sector sector, int cost){
		edgeList.put(sector, cost);
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
