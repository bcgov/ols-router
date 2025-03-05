/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/* 
 * Represents one step in a graph traversal.
 */
public record DijkstraWalker(
		int edgeId, // the edge just traversed
		int nodeId, // the node just arrived at
		double cost, // the cumulative cost from start to the nodeId
		double time, // the cumulative time from start to the nodeId
		double dist, // the cumulative distance from start to the nodeId
		int waitTime, // the cumulative waitTime from start to the nodeId
		DijkstraWalker from, // a reference to the previous step
		double turnCost,
		double xingCost
	) implements Comparable<DijkstraWalker> { 
	
	@Override
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
