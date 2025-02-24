/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.RoadEvent;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.data.enums.RoutingCriteria;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.restrictions.Constraint;
import ca.bc.gov.ols.util.LineStringSplitter;

public class DijkstraShortestPath {
	private final static Logger logger = LoggerFactory.getLogger(DijkstraShortestPath.class.getCanonicalName());
	
	private iBasicGraph graph;
	private RoutingParameters params;
	
	public DijkstraShortestPath(iBasicGraph graph, RoutingParameters params) {
		this.graph = graph;
		this.params = params;
	}
	
	public EdgeList findShortestPath(WayPoint startEdge, WayPoint endEdge, double timeOffset) {	
		return findShortestPaths(startEdge, new WayPoint[]{endEdge}, timeOffset)[0];
	}
	
	private double getMultiplier(int edgeId) {
		double multiplier = 1;
		if(params.isFollowTruckRoute() && !graph.isTruckRoute(edgeId)) {
			multiplier *= params.getTruckRouteMultiplier();
		}
		if(params.isEnabled(RouteOption.LOCAL_DISTORTION_FIELD)) {
			multiplier *= graph.getLocalDistortion(edgeId, params.getVehicleType());
		}
		if(params.isEnabled(RouteOption.GLOBAL_DISTORTION_FIELD)) {
			multiplier *= params.getGlobalDistortionField().lookup(graph.getRoadClass(edgeId), params.getVehicleType() == VehicleType.TRUCK && graph.isTruckRoute(edgeId));
		}
		return multiplier;
	}
	
	public EdgeList[] findShortestPaths(WayPoint startWp, WayPoint[] endWps, double timeOffset) {
		
		final CostFunction<Integer,Double,Double,Double> costFunction = params.getCriteria() == RoutingCriteria.SHORTEST 
				? (edgeId, time, dist) -> dist * getMultiplier(edgeId) : (edgeId, time, dist) -> time * getMultiplier(edgeId);
		
		final BiFunction<Integer,LocalDateTime,Short> speedFunction = params.isEnabled(RouteOption.TRAFFIC) ? graph::getEffectiveSpeed : (edgeId,time) -> graph.getSpeedLimit(edgeId);
		
		if(startWp == null) return new EdgeList[endWps.length];
		
		// maps from endEdgeId to a list of indexes into the toEdgesArray
		TIntObjectHashMap<List<Integer>> endEdgesById = new TIntObjectHashMap<List<Integer>>();
		// and a map to best cost found so far
		DijkstraWalker[] costByEndWPIdx = new DijkstraWalker[endWps.length];
		
		int nullToEdges = 0;
		
		// and populate them
		for(int endWpIdx = 0; endWpIdx < endWps.length; endWpIdx++) {
			WayPoint endWp = endWps[endWpIdx];
			if(endWp == null) {
				nullToEdges++;
			} else {
				for(int edgeId : endWp.edgeIds()) {
					List<Integer> edges = endEdgesById.get(edgeId);
					if(edges == null) {
						edges = new ArrayList<Integer>();
						endEdgesById.put(edgeId, edges);
					}
					edges.add(endWpIdx);
				}
				costByEndWPIdx[endWpIdx] = new DijkstraWalker(endWp.edgeIds()[0], BasicGraphInternal.NO_NODE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, null);
			}
		}
		
		int minPaths = Math.min(params.getMaxPairs(), endWps.length-nullToEdges);
		EdgeList[] paths = new EdgeList[endWps.length];
		int pathsFinished = 0;
		double worstPathCost = 0;
		LocalDateTime startTime = LocalDateTime.ofInstant(params.getDeparture().plusSeconds(Math.round(timeOffset)), RouterConfig.DEFAULT_TIME_ZONE);
				
		// TODO check all of the endWps for the special case of being the same as the startWp

		// setup the cost data structure and queue
		boolean[] edgeIdVisisted = new boolean[graph.numEdges()];
		Queue<DijkstraWalker> queue = new PriorityQueue<DijkstraWalker>();
		int checkedEdgeCount = 0;

		// add all the start edges to the Q and cost map
		for(int startEdgeId : startWp.edgeIds()) {
			double length = graph.getLength(startEdgeId);
			double time = length * 3.6 / speedFunction.apply(startEdgeId, startTime);
			double cost = costFunction.apply(startEdgeId, time, length);
			if(params.isEnabled(RouteOption.XING_COSTS)) {
				double xingCost = params.getXingCost(graph.getToImpactor(startEdgeId), graph.getXingClass(startEdgeId));
				time += xingCost;
				if(params.getCriteria().equals(RoutingCriteria.FASTEST)) {
					cost += xingCost;
				}
			}
			DijkstraWalker startWalker = new DijkstraWalker(startEdgeId, graph.getToNodeId(startEdgeId), 
					cost, time, length, null);
			queue.add(startWalker);
			//costByEdgeId.put(startEdgeId, startWalker);
		}
		
		// traverse network looking for the cheapest paths
		DijkstraWalker walker;
		while((walker = queue.poll()) != null) {
			checkedEdgeCount++;
			// escape from infinite loop!
			if(checkedEdgeCount > graph.numEdges() * 2) {
				logger.error("Infinite routing loop encountered, investigation required!");
				break;
			}

			int nodeId = walker.getNodeId();
			int fromEdgeId = walker.getEdgeId();
			nextEdge:
			for(int edgeId = graph.nextEdge(nodeId, BasicGraphInternal.NO_EDGE); edgeId != BasicGraphInternal.NO_EDGE; edgeId = graph.nextEdge(nodeId, edgeId)) {
				// if we've already been to this non-end, non-internal edge, or it is part of a loop
				List<Integer> endEdges = endEdgesById.get(edgeId);
				if(edgeIdVisisted[edgeId] && ((endEdges == null  && (!params.isEnabled(RouteOption.TURN_RESTRICTIONS) || !graph.isMidRestriction(edgeId))) || isLoop(edgeId, walker))) {
					// skip it
					continue nextEdge;
				}
				// filter the edge based on constraints
				// the following comparisons take advantage of the fact that comparisons with NaN always return false  
//				if(params.getHeight() != null && params.getHeight() > graph.getMaxHeight(edgeId)) {
//					continue;
//				}
//				if(params.getWidth() != null && params.getWidth() > graph.getMaxWidth(edgeId)) {
//					continue;
//				}
				// TODO make the maximum standard vehicle length of 12.5 a config parameter
				// TODO make the minimum angle of 30 degrees a config parameter
				//if(params.getLength() != null && params.getLength() > 12.5
				//		&& getAngle(fromEdgeId, edgeId) < 30) {
				//	continue;
				//}
				
//				if(params.getWeight() != null && graph.getFromMaxWeight(edgeId) != null && params.getWeight() > graph.getFromMaxWeight(edgeId)) {
//					continue;
//				}
				
				// filter the edge based on restrictions
				if(!params.getRestrictionValues().isEmpty()) {
					List<? extends Constraint> constraints = graph.lookupRestriction(params.getRestrictionSource(), edgeId);
					for(Constraint c : constraints) {
						if(c.prevents(params) && Collections.disjoint(params.getExcludeRestrictions(), c.getIds())) {
							continue nextEdge;
						}
					}
				}
				
				// TODO Handle alternate time zones
				LocalDateTime currentDateTime = null;
				int overrideTravelSeconds = -1;
				int waitTime = 0;
				if(params.isEnabled(RouteOption.TIME_DEPENDENCY)) {
					currentDateTime = LocalDateTime.ofInstant(params.getDeparture().plusSeconds(Math.round(timeOffset + walker.getTime())), RouterConfig.DEFAULT_TIME_ZONE);
					if(params.isEnabled(RouteOption.EVENTS)) {
						List<RoadEvent> events = graph.lookupEvent(edgeId, currentDateTime);
						for(RoadEvent event : events) {
							int t = event.getDelay(currentDateTime);
							if(t == -1) {
								waitTime = -1;
								break;
							}
							waitTime = Math.max(waitTime, t);
						}
						if(waitTime < 0) {
							// this segment is inaccessible due to an event
							continue nextEdge;
						}
						// TODO handle other types of events (slow-downs due to partial lane closures, etc.)
					}
					if(params.isEnabled(RouteOption.SCHEDULING)) {
						int[] waitAndTravelTime = graph.lookupSchedule(edgeId, currentDateTime);
						
						if(waitAndTravelTime[1] > 0) {
							waitTime = waitAndTravelTime[0]; 
							overrideTravelSeconds = waitAndTravelTime[1];
							//logger.info("Ferry schedule travel time: {}", overrideTravelSeconds);
						}
					}
				}
				if(!params.isEnabled(RouteOption.TIME_DEPENDENCY) || !params.isEnabled(RouteOption.SCHEDULING)) {
					FerryInfo info = graph.getFerryInfo(edgeId);
					if(info != null && info.getMinWaitTime() > 0) {
						waitTime = info.getMinWaitTime();
					}
				}
				
				TurnDirection turnDir = TurnDirection.CENTER;
				if(params.isEnabled(RouteOption.TURN_RESTRICTIONS) || params.isEnabled(RouteOption.TURN_COSTS)) {
					turnDir = graph.lookupTurn(edgeId, walker, currentDateTime, params.getVehicleType(), params.isEnabled(RouteOption.TURN_RESTRICTIONS));
				}
				if(params.isEnabled(RouteOption.TURN_RESTRICTIONS) && turnDir == null) continue;

				// TODO possibly allow U-turns in some situations/params
				if(edgeId == graph.getOtherEdgeId(fromEdgeId)) {
					// this is a U-turn
					//turnDir = TurnDirection.UTURN;
					continue nextEdge;
				}

				// use the turnDirection to calculate the turn cost, if turncosts are on
				double turnCost = 0; 
				if(params.isEnabled(RouteOption.TURN_COSTS)) {
					turnCost = params.getTurnCost(turnDir, graph.getXingClass(walker.getEdgeId()));
				}

				// if this is an end edge
				if(endEdges != null) {
					// for each end point on this edge
					for(int endEdgeIdx : endEdges) {
						WayPoint endEdge = endWps[endEdgeIdx];
						double length = graph.getLength(edgeId);
						// calculate cost
						double edgeTime = waitTime + turnCost + length * 3.6 / speedFunction.apply(edgeId, currentDateTime);
						double time = walker.getTime() + edgeTime;
						double dist = walker.getDist() + length;
						double cost = walker.getCost() + costFunction.apply(edgeId, edgeTime, length);
						DijkstraWalker newWalker = new DijkstraWalker(edgeId, graph.getOtherNodeId(edgeId, nodeId), cost, time, dist, walker, waitTime);
						
						// if it the best path to the end edge so far
						double prevCost = costByEndWPIdx[endEdgeIdx].getCost();
						if (cost < prevCost) {
							costByEndWPIdx[endEdgeIdx] = newWalker;
							if(prevCost == Double.MAX_VALUE) {
								pathsFinished++;
							}
							if(cost > worstPathCost) {
								worstPathCost = cost;
							}
						}
					}
				}
				
				// store the cost to get to the far side of this edge
				double length = graph.getLength(edgeId);
				double edgeTime = waitTime;
				if(overrideTravelSeconds > 0) {
					edgeTime += overrideTravelSeconds;
				} else {
					edgeTime += length * 3.6 / speedFunction.apply(edgeId, currentDateTime);
				}
				edgeTime += turnCost;
				double time = walker.getTime() + edgeTime;
				double dist = walker.getDist() + length;
				double cost = walker.getCost() + costFunction.apply(edgeId, edgeTime, length);
				if(params.isEnabled(RouteOption.XING_COSTS)) {
					double xingCost = params.getXingCost(graph.getToImpactor(edgeId), graph.getXingClass(edgeId));
					time += xingCost;
					if(params.getCriteria().equals(RoutingCriteria.FASTEST)) {
						cost += xingCost;
					}
				}

				DijkstraWalker newWalker = new DijkstraWalker(edgeId, graph.getOtherNodeId(edgeId, nodeId), cost, time, dist, walker, waitTime);
				edgeIdVisisted[edgeId] = true;
				// if we haven't found all paths or this path is still shorter than the worst shortest found
				if(pathsFinished < minPaths || cost < worstPathCost) {
					queue.add(newWalker);
				}
			}
		}
		logger.debug("{} edges checked to find the the shortest path", checkedEdgeCount);
		
		// make a list of all the toEdgeIndexes
		Integer[] toEdgeIdxs = new Integer[endWps.length];
		for(int i = 0; i < toEdgeIdxs.length; i++) {
			toEdgeIdxs[i] = i;
		}
		// if we don't need to calculate all paths, sort them by cost
		if(minPaths < endWps.length-nullToEdges) {
			Arrays.sort(toEdgeIdxs, Comparator.comparingDouble(idx -> costByEndWPIdx[idx].getCost()));
		}
		int calcPaths = 0;
		for(Integer toEdgeIdx : toEdgeIdxs) {
			if(calcPaths < minPaths) {
				if(paths[toEdgeIdx] == null) {
					paths[toEdgeIdx] = walkback(startWp, endWps[toEdgeIdx], costByEndWPIdx[toEdgeIdx]);
				}
				calcPaths++;
			} else {
				paths[toEdgeIdx] = null;
			}
		}
		return paths;
	}
	
	private boolean isLoop(int edgeId, DijkstraWalker walker) {
		for(int count = 1; count < 6 && walker != null; count++) {
			if(walker.getEdgeId() == edgeId) {
				return true;
			}
			walker = walker.getFrom();
		}
		return false;
	}
	
	private double getAngle(int fromEdgeId, int toEdgeId) {
		return Angle.toDegrees(Angle.diff(graph.getToAngleRads(fromEdgeId), graph.getFromAngleRads(toEdgeId)));			
	}

	private EdgeList walkback(WayPoint startEdge, WayPoint endEdge, DijkstraWalker endWalker) {
		if(startEdge == null || endEdge == null) {
			return null;
		}
		// store the edges in the cheapest path, in reverse order 
		EdgeList edges = new EdgeList(100);
		edges.setStartEdge(startEdge);
		edges.setEndEdge(endEdge);

		for(DijkstraWalker curWalker = endWalker; curWalker != null; curWalker = curWalker.getFrom()) {
			edges.add(curWalker.getEdgeId(), curWalker.getTime(), curWalker.getDist(), curWalker.getWaitTime());
		} 
		return edges;
	}

}
