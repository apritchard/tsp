package com.amp.mapping;

import java.util.List;

/**
 * Wrapper for a path that calculates its bound on construction
 * and is comparable so that it has a natural ordering for priority queuing
 */
class TspNode implements Comparable<TspNode>{

	private int bound;
	private List<Sector> path;
	
	/**
	 * @param path A list of sectors traveled so far, with the 
	 * 		  path[0] representing the first sector to visit.
	 * @param bound The lower bound of the cost for this path.
	 */
	public TspNode(List<Sector> path, int bound){
		this.path = path;
		this.bound = bound;

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
	
	@Override
	public int compareTo(TspNode other) {
		return this.bound - other.bound;
	}
}
