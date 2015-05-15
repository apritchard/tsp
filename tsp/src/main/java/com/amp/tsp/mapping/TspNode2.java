package com.amp.tsp.mapping;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * Wrapper for a path that includes its bound so that it can be sorted by bound.
 */
class TspNode2 implements Comparable<TspNode2>{

	private int bound;
	private int length;
	private byte[] path;
	private byte[] ending;
	
	public TspNode2(int bound, byte[] path, byte[] ending, int length){
		this.bound = bound;
		this.path = path;
		this.ending = ending;
		this.length = length;
	}
	
	public TspNode2(TspNode node, Map<Sector, Byte> sectorMap){
		this.bound = node.getBound();
		
		List<Sector> sectorPath = node.getPath();
		this.path = new byte[sectorMap.size()]; //path needs to be maximum possible length
		for(int i = 0; i < sectorPath.size() ; i++){
			path[i] = sectorMap.get(sectorPath.get(i));
		}
		
		List<Sector> sectorEnding = node.getEnding();
		if(sectorEnding != null){
			this.ending = new byte[sectorEnding.size()];
			for(int i = 0; i < sectorEnding.size(); i++){
				ending[i] = sectorMap.get(sectorEnding.get(i));
			}
		}
		
		this.length = sectorPath.size();
	}
	
	public void addNode(byte node){
		path[length++] = node;
	}
	
	public static Queue<TspNode2> queueFrom(Queue<TspNode> initialNodes,
			Map<Sector, Byte> sectorMap) {
		Queue<TspNode2> queue = new PriorityQueue<>();
		
		for(TspNode node : initialNodes){
			queue.add(new TspNode2(node, sectorMap));
		}
		
		return queue;
	}
		
	@Override
	public int compareTo(TspNode2 other) {
		//lower bound first
		if (this.bound != other.bound){
			return this.bound - other.bound;
		}
		//for ties, longer path first
		return other.length - this.length;
	}

	public int getBound() {
		return bound;
	}

	public void setBound(int bound) {
		this.bound = bound;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getPath() {
		return path;
	}

	public void setPath(byte[] path) {
		this.path = path;
	}

	public byte[] getEnding() {
		return ending;
	}

	public void setEnding(byte[] ending) {
		this.ending = ending;
	}

}
