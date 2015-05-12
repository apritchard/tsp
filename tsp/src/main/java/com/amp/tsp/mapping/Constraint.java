package com.amp.tsp.mapping;

import java.util.List;

/**
 * This class describes both a set of required starting and required ending nodes for a path.
 * @author alex
 *
 */
public class Constraint {
	private List<Sector> starting;
	private List<Sector> ending;
	
	public Constraint(List<Sector> starting, List<Sector> ending){
		this.starting = starting;
		this.ending = ending;
	}
	
	public List<Sector> getEnding() {
		return ending;
	}
	public void setEnding(List<Sector> ending) {
		this.ending = ending;
	}
	public List<Sector> getStarting() {
		return starting;
	}
	public void setStarting(List<Sector> starting) {
		this.starting = starting;
	}

}
