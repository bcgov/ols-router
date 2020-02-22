/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

public class EdgeList {
	private static final int DEFAULT_INITIAL_CAPACITY = 16;
	private TIntArrayList edges;
	//private BitSet forward;
	private TDoubleArrayList times;
	private TDoubleArrayList dists;
	private TIntArrayList waitTimes;
	private SplitEdge startEdge;
	private SplitEdge endEdge;
	
	public EdgeList() {
		this(DEFAULT_INITIAL_CAPACITY);
	}
	
	public EdgeList(int initialCapacity) {
		edges = new TIntArrayList(initialCapacity);
		//forward = new BitSet(initialCapacity);
		times = new TDoubleArrayList(initialCapacity);
		dists = new TDoubleArrayList(initialCapacity);
		waitTimes = new TIntArrayList(initialCapacity);
	}
	
	public void add(int edgeId, double time, double dist, int waitTime) {
		edges.add(edgeId);
		//forward.set(edges.size()-1, isForward);
		times.add(time);
		dists.add(dist);
		waitTimes.add(waitTime);
	}
	
	public int edgeId(int index) {
		return edges.get(index);
	}

//	public boolean forward(int index) {
//		return forward.get(index);
//	}

	public double time(int index) {
		return times.get(index);
	}

	public double dist(int index) {
		return dists.get(index);
	}

	public int waitTime(int index) {
		return waitTimes.get(index);
	}

	public SplitEdge getStartEdge() {
		return startEdge;
	}

	public SplitEdge getEndEdge() {
		return endEdge;
	}

	public void setStartEdge(SplitEdge startEdge) {
		this.startEdge = startEdge;
	}

	public void setEndEdge(SplitEdge endEdge) {
		this.endEdge = endEdge;
	}


	public int size() {
		return edges.size();
	}

}
