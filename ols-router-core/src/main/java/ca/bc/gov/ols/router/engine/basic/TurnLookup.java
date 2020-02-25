/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.data.WeeklyTimeRange;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.util.IntObjectArrayMap;
import gnu.trove.set.hash.TIntHashSet;

public class TurnLookup {
	private static final Logger logger = LoggerFactory.getLogger(TurnLookup.class.getCanonicalName());
	
	private BasicGraph graph;
	private IntObjectArrayMap<ArrayList<TurnRestrictionEntry>> turnRestrictionMap;
	private TIntHashSet internalEdges = new TIntHashSet();
	
	public TurnLookup(BasicGraph graph) {
		this.graph = graph;
		turnRestrictionMap = new IntObjectArrayMap<ArrayList<TurnRestrictionEntry>>(graph.numEdges());
	}
	
	public void addRestriction(int[] ids, WeeklyTimeRange restriction, Set<VehicleType> vehicleTypes) {
		int toEdgeIdx = ids.length - 1;
		ArrayList<TurnRestrictionEntry> restrictionList = turnRestrictionMap.get(ids[toEdgeIdx]);
		if(restrictionList == null) {
			restrictionList = new ArrayList<TurnRestrictionEntry>(3);
			turnRestrictionMap.put(ids[toEdgeIdx], restrictionList);
		}
		restrictionList.add(new TurnRestrictionEntry(ids, restriction, vehicleTypes));
		if(ids.length > 3) {
			for(int i = 2; i < ids.length - 1; i += 2) {
				internalEdges.add(ids[i]);
			}
		}
	}
	
	public void addClass(int[] ids, TurnDirection turnDir) {
		ArrayList<TurnRestrictionEntry> restrictionList = turnRestrictionMap.get(ids[2]);
		if(restrictionList == null) {
			restrictionList = new ArrayList<TurnRestrictionEntry>(3);
			turnRestrictionMap.put(ids[2], restrictionList);
		}
		restrictionList.add(new TurnRestrictionEntry(ids, turnDir));
	}
	
	public boolean isInternalEdge(int edgeId) {
		return internalEdges.contains(edgeId);
	}
	
	public TurnDirection lookupTurn(final int toEdge, final DijkstraWalker fromWalker, final LocalDateTime dateTime, final VehicleType vehicleType, boolean useRestrictions) {
		ArrayList<TurnRestrictionEntry> restrictionList = turnRestrictionMap.get(toEdge);
		TurnDirection turnDir = null;
		if(restrictionList != null) {
			entry:
			for(TurnRestrictionEntry restrictionEntry : restrictionList) {
				// loop through the ids while walking back through the path and confirm the match
				DijkstraWalker walker = fromWalker;
				for(int idx = restrictionEntry.ids.length - 3; idx >= 0; idx -= 2) {
					if(walker == null) {
						continue entry;
					}
					if(walker.getEdgeId() != restrictionEntry.ids[idx] || walker.getNodeId() != restrictionEntry.ids[idx+1]) {
						// not a match, start on next entry
						continue entry;
					}
					if(turnDir == null) {
						turnDir = restrictionEntry.turnDir;
						if(!useRestrictions && turnDir != null) return turnDir;
					}
					walker = walker.getFrom();
				}
				if(useRestrictions 
						&& restrictionEntry.vehicleTypes.contains(vehicleType) 
						&& restrictionEntry.restriction != null 
						&& restrictionEntry.restriction.contains(dateTime)) {
					return null;
				}
			}
		}
		if(turnDir == null) {
			// this does happen, eg. reverse seg u-turn
			//logger.debug("No turn class for turn!");
			return TurnDirection.CENTER;
		}
		return turnDir;				
	}
	
//	public TurnDirection lookupTurn(final int toEdge, final DijkstraWalker fromWalker, final LocalDateTime dateTime, final VehicleType vehicleType) {
//		ArrayList<TurnRestrictionEntry> restrictionList = turnRestrictionMap.get(toEdge);
//		TurnDirection turnDir = null;
//		if(restrictionList != null) {
//			entry:
//			for(TurnRestrictionEntry restrictionEntry : restrictionList) {
//				if(!restrictionEntry.vehicleTypes.contains(vehicleType) 
//						|| restrictionEntry.restriction == null 
//						|| !restrictionEntry.restriction.contains(dateTime)) {
//					continue;
//				}
//				if(restrictionEntry.ids.length == 3) {
//					if(restrictionEntry.ids[1] == fromWalker.getNodeId() && restrictionEntry.ids[0] == fromWalker.getEdgeId()) {
//						turnDir = restrictionEntry.turnDir;
//						return null;
//					}
//				} else if(restrictionEntry.ids.length > 3) {
//					// this is a u-turn/complex turn
//					// loop through the ids while walking back through the path and confirm the match
//					DijkstraWalker walker = fromWalker;
//					for(int idx = restrictionEntry.ids.length - 3; idx >= 0; idx -= 2) {
//						if(walker == null) {
//							continue entry;
//						}
//						if(walker.getEdgeId() != restrictionEntry.ids[idx] || walker.getNodeId() != restrictionEntry.ids[idx+1]) {
//							// not a match, start on next entry
//							continue entry;
//						}
//						walker = walker.getFrom();
//					}
//					return null;
//				}
//			}
//		}
//		return turnDir;
//	}
	
}

class TurnRestrictionEntry {
	final int[] ids;
	final WeeklyTimeRange restriction;
	final Set<VehicleType> vehicleTypes;
	final TurnDirection turnDir;

	TurnRestrictionEntry(int[] ids, WeeklyTimeRange restriction, Set<VehicleType> vehicleTypes) {
		this.ids = ids;
		this.restriction = restriction;
		this.vehicleTypes = vehicleTypes;
		turnDir = null;
	}

	TurnRestrictionEntry(int[] ids, TurnDirection turnDir) {
		this.ids = ids;
		this.restriction = null;
		this.vehicleTypes = EnumSet.allOf(VehicleType.class);
		this.turnDir = turnDir;
	}

	public String toString() {
		return Arrays.toString(ids) + "|" + restriction + "|" + vehicleTypes;
	}
}

