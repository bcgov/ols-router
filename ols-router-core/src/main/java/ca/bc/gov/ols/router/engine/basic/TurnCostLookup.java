/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import gnu.trove.set.hash.TIntHashSet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.data.WeeklyTimeRange;
import ca.bc.gov.ols.router.util.IntObjectArrayMap;

public class TurnCostLookup {
	private static final Logger logger = LoggerFactory.getLogger(TurnCostLookup.class.getCanonicalName());
	
	private BasicGraph graph;
	private IntObjectArrayMap<ArrayList<TurnCostEntry>> turnCostMap;
	private TIntHashSet internalEdges = new TIntHashSet();
	
	public TurnCostLookup(BasicGraph graph) {
		this.graph = graph;
		turnCostMap = new IntObjectArrayMap<ArrayList<TurnCostEntry>>(graph.numEdges());
	}
	
	public boolean addCost(int[] ids, byte cost, WeeklyTimeRange restriction) {
		int toEdgeIdx = ids.length - 1;
		ArrayList<TurnCostEntry> costList = turnCostMap.get(ids[toEdgeIdx]);
		if(costList == null) {
			costList = new ArrayList<TurnCostEntry>(3);
			turnCostMap.put(ids[toEdgeIdx], costList);
		}
		for(TurnCostEntry costEntry : costList) {
			if(Arrays.equals(costEntry.ids, ids)) {
				// probably a lollipop situation
				logger.warn("Duplicate Turn Cost entry (ignored) for {}", ids);
				return false;
			}
		}
		costList.add(new TurnCostEntry(ids, cost, restriction));
		if(ids.length > 3) {
			for(int i = 2; i < ids.length - 1; i += 2) {
				internalEdges.add(ids[i]);
			}
		}
		return true;
	}
	
	public boolean isInternalEdge(int edgeId) {
		return internalEdges.contains(edgeId);
	}
	
	public int lookup(final int toEdge, final DijkstraWalker fromWalker, final LocalDateTime dateTime) {
		ArrayList<TurnCostEntry> costList = turnCostMap.get(toEdge);
		int cost = 0;
		if(costList != null) {
			entry:
			for(TurnCostEntry costEntry : costList) {
				if(costEntry.ids.length == 3 && costEntry.ids[1] == fromWalker.getNodeId() && costEntry.ids[0] == fromWalker.getEdgeId()) {
					// this is the matching basic turn; we will return this cost if don't find any other restrictions (u-turns/complex)
					cost = costEntry.cost;
					if(costEntry.restriction != null && costEntry.restriction.contains(dateTime)) {
						return -1;
					}
				} else if(costEntry.ids.length > 3) {
					// this is a u-turn/complex turn
					// loop through the ids while walking back through the path and confirm the match
					DijkstraWalker walker = fromWalker;
					for(int idx = costEntry.ids.length - 3; idx >= 0 && walker != null; idx -= 2) {
						if(walker.getEdgeId() != costEntry.ids[idx] || walker.getNodeId() != costEntry.ids[idx+1]) {
							// not a match, start on next entry
							continue entry;
						}
						walker = walker.getFrom();
					}
					if(costEntry.restriction != null && costEntry.restriction.contains(dateTime)) {
						return -1;
					}
				}
			}
		}
		return cost;
	}
	
}

class TurnCostEntry {
	final int[] ids;
	final byte cost;
	final WeeklyTimeRange restriction;

	TurnCostEntry(int[] ids, byte cost, WeeklyTimeRange restriction) {
		this.ids = ids;
		this.cost = cost;
		this.restriction = restriction;
	}
	
	public String toString() {
		return Arrays.toString(ids) + "|" + cost + "|" + restriction;
	}
}

