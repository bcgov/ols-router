/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DijkstraWalker implements Comparable<DijkstraWalker>{
	private final int edgeId;
	private final int nodeId;
	private final double cost;
	private final double time;
	private final double dist;
	private final int waitTime;
	private final DijkstraWalker from;
	
	public DijkstraWalker(int edgeId, int nodeId, double cost, double time, double dist, DijkstraWalker from) {
		this.edgeId = edgeId;
		this.nodeId = nodeId;
		this.cost = cost;
		this.time = time;
		this.dist = dist;
		this.from = from;
		this.waitTime = 0;
	}

	public DijkstraWalker(int edgeId, int nodeId, double cost, double time, double dist, DijkstraWalker from, int waitTime) {
		this.edgeId = edgeId;
		this.nodeId = nodeId;
		this.cost = cost;
		this.time = time;
		this.dist = dist;
		this.from = from;
		this.waitTime = waitTime;
	}

	public int getEdgeId() {
		return edgeId;
	}

	public int getNodeId() {
		return nodeId;
	}

	public double getCost() {
		return cost;
	}

	public double getTime() {
		return time;
	}

	public double getDist() {
		return dist;
	}

	public DijkstraWalker getFrom() {
		return from;
	}

	public int getWaitTime() {
		return waitTime;
	}


	public int compareTo(DijkstraWalker other) {
		return Double.compare(cost, other.cost);
	}

	public List<Integer> getEdgeChain() {
		ArrayList<Integer> chain = new ArrayList<Integer>();
		buildEdgeChain(chain);
		return chain;
	}
	
	private void buildEdgeChain(List<Integer> chain) {
		chain.add(edgeId);
		if(from != null) {
			from.buildEdgeChain(chain);
		}
	}
	
	public List<Integer> checkForDuplicateEdges() {
		List<Integer> dups = new ArrayList<Integer>();
		List<Integer> chain = getEdgeChain();
		HashSet<Integer> set = new HashSet<Integer>(chain.size());
		for(Integer edgeId : chain) {
			if(!set.add(edgeId)) {
				dups.add(edgeId);
			}
		}
		return dups;
	}
}
