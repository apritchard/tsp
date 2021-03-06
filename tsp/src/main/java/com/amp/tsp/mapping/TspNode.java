package com.amp.tsp.mapping;

import java.util.List;

/**
 * Wrapper for a path that includes its bound so that it can be sorted by bound.
 */
class TspNode implements Comparable<TspNode>{

	private int bound;
	private List<Sector> path;
	private List<Sector> ending;
	
	/**
	 * @param path A list of sectors traveled so far, with the 
	 * 		  path[0] representing the first sector to visit.
	 * @param bound The lower bound of the cost for this path.
	 */
	public TspNode(List<Sector> path, int bound){
		this.path = path;
		this.bound = bound;
	}
	
	public TspNode(List<Sector> path, int bound, List<Sector> ending){
		this.path = path;
		this.bound = bound;
		this.ending = ending;
	}
	
	/**
	 * @return The bound of the path defined by this node
	 */
	public int getBound(){
		return bound;
	}
	
	/**
	 * @return The sectors traveled, in order from first to last.
	 */
	public List<Sector> getPath(){
		return path;
	}
	
	public List<Sector> getEnding(){
		return ending;
	}
	
	@Override
	public int compareTo(TspNode other) {
		//lower bound first
		if (this.bound != other.bound){
			return this.bound - other.bound;
		}
		//for ties, longer path first
		return other.path.size() - this.path.size();
	}
}
