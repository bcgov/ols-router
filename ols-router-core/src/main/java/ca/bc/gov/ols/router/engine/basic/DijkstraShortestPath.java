/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiFunction;

import org.locationtech.jts.algorithm.Angle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.RoadEvent;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.data.enums.RoutingCriteria;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.restrictions.Constraint;
import gnu.trove.map.hash.TIntObjectHashMap;

public class DijkstraShortestPath {
	private final static Logger logger = LoggerFactory.getLogger(DijkstraShortestPath.class.getCanonicalName());
	
	private QueryGraph graph;
	private RoutingParameters params;
	
	public DijkstraShortestPath(QueryGraph graph, RoutingParameters params) {
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
		DijkstraWalker[] costByEndWpIdx = new DijkstraWalker[endWps.length];
		
		int nullToEdges = 0;
		
		// and populate them
		for(int endWpIdx = 0; endWpIdx < endWps.length; endWpIdx++) {
			WayPoint endWp = endWps[endWpIdx];
			if(endWp == null) {
				nullToEdges++;
			} else {
				for(int edgeId : endWp.incomingEdgeIds()) {
					List<Integer> edges = endEdgesById.get(edgeId);
					if(edges == null) {
						edges = new ArrayList<Integer>();
						endEdgesById.put(edgeId, edges);
					}
					edges.add(endWpIdx);
				}
				costByEndWpIdx[endWpIdx] = new DijkstraWalker(endWp.incomingEdgeIds().get(0), BasicGraphInternal.NO_NODE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, 0, null);
			}
		}
		
		int minPaths = Math.min(params.getMaxPairs(), endWps.length - nullToEdges);
		EdgeList[] paths = new EdgeList[endWps.length];
		int pathsFinished = 0;
		double worstPathCost = 0;
		LocalDateTime startTime = LocalDateTime.ofInstant(params.getDeparture().plusSeconds(Math.round(timeOffset)), RouterConfig.DEFAULT_TIME_ZONE);
				
		// check all of the endWps for the special case of being the same as the startWp, or very close
		for(int endWpIdx = 0; endWpIdx < endWps.length; endWpIdx++) {
			WayPoint endWp = endWps[endWpIdx];
			if(endWp == null) continue;
			int startNodeId = graph.findNodeId(startWp.point());
			int endNodeId = graph.findNodeId(endWp.point());
			double distance = startWp.point().distance(endWp.point());
			for(int startEdgeId : startWp.outgoingEdgeIds()) {
				for(int endEdgeId : endWp.incomingEdgeIds()) {
					// shortcut the case where start and end node are the same
					// or are on the same seg and within minRoutingDistance
					if( startNodeId == endNodeId 
							|| (graph.getBaseEdgeId(startEdgeId) == graph.getBaseEdgeId(endEdgeId)
								&& distance < params.getMinRoutingDistance())) {
						costByEndWpIdx[endWpIdx] = new DijkstraWalker(BasicGraphInternal.NO_EDGE, BasicGraphInternal.NO_NODE, 0, 0, distance, 0, null);
						pathsFinished++;
					}
				}
			}
		}

		// setup the cost data structure and queue
		boolean[] edgeIdVisited = new boolean[graph.numEdges()];
		Queue<DijkstraWalker> queue = new PriorityQueue<DijkstraWalker>();
		int checkedEdgeCount = 0;

		// add all the start edges to the Q and cost map
		for(int startEdgeId : startWp.outgoingEdgeIds()) {
			double length = graph.getLength(startEdgeId);
			double time = length * 3.6 / speedFunction.apply(startEdgeId, startTime);
			double cost = costFunction.apply(startEdgeId, time, length);
//			if(params.isEnabled(RouteOption.XING_COSTS)) {
//				double xingCost = params.getXingCost(graph.getToImpactor(startEdgeId), graph.getXingClass(startEdgeId));
//				time += xingCost;
//				if(params.getCriteria().equals(RoutingCriteria.FASTEST)) {
//					cost += xingCost;
//				}
//			}
			DijkstraWalker startWalker = new DijkstraWalker(startEdgeId, graph.getToNodeId(startEdgeId), 
					cost, time, length, 0, null);
			queue.add(startWalker);
			//costByEdgeId.put(startEdgeId, startWalker);
		}
		
		// traverse network looking for the cheapest paths
		DijkstraWalker walker;
		nextEdge:
		while((walker = queue.poll()) != null) {
			checkedEdgeCount++;
			// escape from infinite loop!
			if(checkedEdgeCount > graph.numEdges() * 2) {
				logger.error("Infinite routing loop encountered, investigation required!");
				break;
			}

			int nodeId = walker.nodeId();
			int edgeId = walker.edgeId();
			// if we've already been to this non-end, non-internal edge, or it is part of a loop
			List<Integer> endEdges = endEdgesById.get(edgeId);
			if(edgeIdVisited[edgeId] 
					&& ((endEdges == null  && (!params.isEnabled(RouteOption.TURN_RESTRICTIONS) 
							|| !graph.isMidRestriction(edgeId))) 
					|| isLoop(walker))) {
				// skip it
				continue nextEdge;
			}

			// TODO possibly allow U-turns in some situations/params
			if(walker.from() != null && edgeId == graph.getOtherEdgeId(walker.from().edgeId())) {
				// this is a U-turn
				//turnDir = TurnDirection.UTURN;
				continue nextEdge;
			}

			// Length/Angle Based Restrictions - not required at this time
			// TODO make the maximum standard vehicle length of 12.5 a config parameter
			// TODO make the minimum angle of 30 degrees a config parameter
			//if(params.getLength() != null && params.getLength() > 12.5
			//		&& getAngle(fromEdgeId, edgeId) < 30) {
			//	continue;
			//}

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
				currentDateTime = LocalDateTime.ofInstant(params.getDeparture().plusSeconds(Math.round(timeOffset + walker.time())), RouterConfig.DEFAULT_TIME_ZONE);
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
				turnDir = graph.lookupTurn(walker, currentDateTime, params.getVehicleType(), params.isEnabled(RouteOption.TURN_RESTRICTIONS));
			}
			if(params.isEnabled(RouteOption.TURN_RESTRICTIONS) && turnDir == null) continue;

			// if there was a previous segment, add the turn and crossing costs
			double turnCost = 0;
			if(walker.from() != null) {
				// use the turnDirection to calculate the turn cost, if turncosts are on
				if(params.isEnabled(RouteOption.TURN_COSTS) ) {
					turnCost = params.getTurnCost(turnDir, graph.getXingClass(walker.from().edgeId()));
				}
				if(params.isEnabled(RouteOption.XING_COSTS)) {
					double xingCost = params.getXingCost(graph.getToImpactor(walker.edgeId()), graph.getXingClass(walker.from().edgeId()));
					turnCost += xingCost;
				}
			}


			// if this is an end edge
			if(endEdges != null) {
				// for each end point on this edge
				for(int endEdgeIdx : endEdges) {
					// if it the best path to the end edge so far
					double prevCost = costByEndWpIdx[endEdgeIdx].cost();
					if (walker.cost() < prevCost) {
						costByEndWpIdx[endEdgeIdx] = walker;
						if(prevCost == Double.MAX_VALUE) {
							pathsFinished++;
						}
						if(walker.cost() > worstPathCost) {
							worstPathCost = walker.cost();
						}
					}
				}
			}
				
			for(int nextEdgeId = graph.nextEdge(nodeId, BasicGraphInternal.NO_EDGE); 
					nextEdgeId != BasicGraphInternal.NO_EDGE; 
					nextEdgeId = graph.nextEdge(nodeId, nextEdgeId)) {
			
				// store the cost to get to the far side of this edge
				double length = graph.getLength(nextEdgeId);
				double edgeTime = waitTime;
				if(overrideTravelSeconds > 0) {
					edgeTime += overrideTravelSeconds;
				} else {
					edgeTime += length * 3.6 / speedFunction.apply(nextEdgeId, currentDateTime);
				}
				edgeTime += turnCost;
				double time = walker.time() + edgeTime;
				double dist = walker.dist() + length;
				double cost = walker.cost() + costFunction.apply(nextEdgeId, edgeTime, length);

				DijkstraWalker newWalker = new DijkstraWalker(nextEdgeId, graph.getOtherNodeId(nextEdgeId, nodeId), cost, time, dist, waitTime, walker);
				// if we haven't found all paths or this path is still shorter than the worst shortest found
				if(pathsFinished < minPaths || cost < worstPathCost) {
					queue.add(newWalker);
				}
			}
			edgeIdVisited[edgeId] = true;

		}
		logger.debug("{} edges checked to find the the shortest path", checkedEdgeCount);
		
		// make a list of all the toEdgeIndexes
		Integer[] toEdgeIdxs = new Integer[endWps.length];
		for(int i = 0; i < toEdgeIdxs.length; i++) {
			toEdgeIdxs[i] = i;
		}
		// if we don't need to calculate all paths, sort them by cost
		if(minPaths < endWps.length-nullToEdges) {
			Arrays.sort(toEdgeIdxs, Comparator.comparingDouble(idx -> costByEndWpIdx[idx].cost()));
		}
		int calcPaths = 0;
		for(Integer toEdgeIdx : toEdgeIdxs) {
			if(calcPaths < minPaths) {
				if(paths[toEdgeIdx] == null) {
					paths[toEdgeIdx] = walkback(startWp, endWps[toEdgeIdx], costByEndWpIdx[toEdgeIdx]);
				}
				calcPaths++;
			} else {
				paths[toEdgeIdx] = null;
			}
		}
		return paths;
	}
	
	private boolean isLoop(DijkstraWalker walker) {
		int edgeId = walker.edgeId();
		walker = walker.from();
		for(int count = 1; count < 6 && walker != null; count++) {
			if(walker.edgeId() == edgeId) {
				return true;
			}
			walker = walker.from();
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
		edges.setStartWayPoint(startEdge);
		edges.setEndWayPoint(endEdge);

		for(DijkstraWalker curWalker = endWalker; curWalker != null; curWalker = curWalker.from()) {
			edges.add(curWalker.edgeId(), curWalker.time(), curWalker.dist(), curWalker.waitTime());
		} 
		return edges;
	}

}
