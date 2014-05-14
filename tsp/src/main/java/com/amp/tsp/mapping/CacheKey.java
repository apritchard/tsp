package com.amp.tsp.mapping;

import java.util.Set;

public class CacheKey {
	private Sector start;
	private Set<Sector> others;
	public CacheKey(Sector start, Set<Sector> others){
		this.start = start;
		this.others = others;
	}
	public Sector getStart() {
		return start;
	}
	public Set<Sector> getOthers() {
		return others;
	}
	
	@Override
	public String toString(){
		return start.toString() + others.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((others == null) ? 0 : others.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
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
		CacheKey other = (CacheKey) obj;
		if (others == null) {
			if (other.others != null)
				return false;
		} else if (!others.equals(other.others))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}
}
