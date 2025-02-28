/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.Builder;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import ca.bc.gov.ols.enums.TrafficImpactor;
import ca.bc.gov.ols.router.RoutingEngine;
import ca.bc.gov.ols.router.api.GeometryReprojector;
import ca.bc.gov.ols.router.api.IsochroneResponse;
import ca.bc.gov.ols.router.api.NavInfoParameters;
import ca.bc.gov.ols.router.api.NavInfoResponse;
import ca.bc.gov.ols.router.api.RouterDirectionsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceBetweenPairsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceResponse;
import ca.bc.gov.ols.router.api.RouterOptimalDirectionsResponse;
import ca.bc.gov.ols.router.api.RouterOptimalRouteResponse;
import ca.bc.gov.ols.router.api.RouterRouteResponse;
import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.enums.NavInfoType;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.data.vis.VisFeature;
import ca.bc.gov.ols.router.datasource.DataUpdateManager;
import ca.bc.gov.ols.router.restrictions.Constraint;
import ca.bc.gov.ols.router.restrictions.RestrictionLookupBuilder;
import ca.bc.gov.ols.router.restrictions.rdm.Restriction;
import ca.bc.gov.ols.router.status.StatusMessage;
import ca.bc.gov.ols.router.status.StatusMessage.Type;
import ca.bc.gov.ols.router.status.SystemStatus;
import ca.bc.gov.ols.router.util.TimeHelper;
import ca.bc.gov.ols.util.MapList;
import ca.bc.gov.ols.util.StopWatch;

public class BasicGraphRoutingEngine implements RoutingEngine {
	private static final Logger logger = LoggerFactory.getLogger(BasicGraphRoutingEngine.class.getCanonicalName());

	private RouterConfig config;
	private GeometryFactory gf;
	
	BasicGraph graph;
	
	public BasicGraphRoutingEngine(RouterConfig config, BasicGraph graph,
			GeometryFactory geometryFactory, GeometryReprojector reprojector) throws IOException {
		logger.trace("{}() constructor called", getClass().getName());
		this.config = config;
		this.gf = geometryFactory;
		this.graph = graph;
	}
	
	/**
	 * copy constructor with new graph
	 * @param engine
	 */
	public BasicGraphRoutingEngine(BasicGraphRoutingEngine engine, BasicGraph newGraph) {
		this.config = engine.config;
		this.gf = engine.gf;
		this.graph = newGraph;
	}
	
	@Override
	public RouterDistanceResponse distance(RoutingParameters params) {
		try {
			StopWatch sw = new StopWatch();
			sw.start();
			EdgeMerger em = doRoute(params);
			em.calcDistance(gf);
			RouterDistanceResponse response = new RouterDistanceResponse(params, graph.getDates(), em.getDist(), em.getTime());
			sw.stop();
			response.setExecutionTime(sw.getElapsedTime());
			return response;
		} catch(IllegalArgumentException iae) {
			return new RouterDistanceResponse(params, graph.getDates());
		} catch(Throwable t) {
			logger.warn("Exception thrown: ", t);
			return new RouterDistanceResponse(params, graph.getDates());
		}
	}
	
	@Override
	public RouterRouteResponse route(RoutingParameters params) {
		try {
			StopWatch sw = new StopWatch();
			sw.start();
			EdgeMerger em = doRoute(params);
			em.calcRoute(gf);
			RouterRouteResponse response = new RouterRouteResponse(params, graph.getDates(), em.getDist(), em.getTime(), em.getRoute(), em.getPartitions(), em.getTlids(), em.getRestrictions());
			sw.stop();
			response.setExecutionTime(sw.getElapsedTime());
			return response;
		} catch(IllegalArgumentException iae) {
			return new RouterRouteResponse(params, graph.getDates());
		} catch(Throwable t) {
			logger.warn("Exception thrown: ", t);
			return new RouterRouteResponse(params, graph.getDates());
		}
	}

	@Override
	public RouterDirectionsResponse directions(RoutingParameters params) {
		try {
			StopWatch sw = new StopWatch();
			sw.start();
			EdgeMerger em = doRoute(params);
			em.calcDirections(gf);
			RouterDirectionsResponse response = new RouterDirectionsResponse(params, graph.getDates(), em.getDist(), em.getTime(), em.getRoute(), em.getPartitions(), em.getTlids(), em.getRestrictions(), em.getDirections(), em.getNotifications());
			sw.stop();
			response.setExecutionTime(sw.getElapsedTime());
			return response;
		} catch(IllegalArgumentException iae) {
			return new RouterDirectionsResponse(params, graph.getDates());
		} catch(Throwable t) {
			logger.warn("Exception thrown: ", t);
			return new RouterDirectionsResponse(params, graph.getDates());
		}
	}

	@Override
	public RouterOptimalRouteResponse optimalRoute(RoutingParameters params) {
		StopWatch sw = new StopWatch();
		sw.start();
		StopWatch routingTimer = new StopWatch();
		StopWatch optimizationTimer = new StopWatch();
		int[] visitOrder = new int[params.getPoints().size()];
		RouterOptimalRouteResponse response;
		try {
			EdgeMerger em = doOptimizedRoute(params, routingTimer, optimizationTimer, visitOrder);
			em.calcRoute(gf);
			response = new RouterOptimalRouteResponse(params, graph.getDates(), em.getDist(), em.getTime(), em.getRoute(), em.getPartitions(), em.getTlids(), em.getRestrictions(), visitOrder);
		} catch(IllegalArgumentException iae) {
			response = new RouterOptimalRouteResponse(params, graph.getDates());
		} catch(Throwable t) {
			logger.warn("Exception thrown: ", t);
			response = new RouterOptimalRouteResponse(params, graph.getDates());
		}
		sw.stop();
		response.setExecutionTime(sw.getElapsedTime());
		response.setRoutingExecutionTime(routingTimer.getElapsedTime());
		response.setOptimizationExecutionTime(optimizationTimer.getElapsedTime());
		return response;
	}

	@Override
	public RouterOptimalDirectionsResponse optimalDirections(RoutingParameters params) {
		StopWatch sw = new StopWatch();
		sw.start();
		StopWatch routingTimer = new StopWatch();
		StopWatch optimizationTimer = new StopWatch();
		int[] visitOrder = new int[params.getPoints().size()];
		RouterOptimalDirectionsResponse response;
		try {
			EdgeMerger em = doOptimizedRoute(params, routingTimer, optimizationTimer, visitOrder);
			em.calcDirections(gf);
			response = new RouterOptimalDirectionsResponse(params, graph.getDates(), em.getDist(),em.getTime(), 
					em.getRoute(), em.getPartitions(), em.getTlids(), em.getRestrictions(), em.getDirections(), em.getNotifications(), visitOrder);
		} catch(IllegalArgumentException iae) {
			response = new RouterOptimalDirectionsResponse(params, graph.getDates());
		} catch(Throwable t) {
			logger.warn("Exception thrown: ", t);
			response = new RouterOptimalDirectionsResponse(params, graph.getDates());
		}			
		sw.stop();
		response.setExecutionTime(sw.getElapsedTime());
		response.setRoutingExecutionTime(routingTimer.getElapsedTime());
		response.setOptimizationExecutionTime(optimizationTimer.getElapsedTime());
		return response;
	}

	private EdgeMerger doRoute(RoutingParameters params) {
		QueryGraph queryGraph = new QueryGraph(graph, params);
		WayPoint[] wayPoints = getWayPoints(queryGraph, params.getFullPoints(), params.isCorrectSide(), false);		
		return doCoreRoute(params, queryGraph, wayPoints);
	}

	private EdgeMerger doOptimizedRoute(RoutingParameters params, StopWatch routingTimer, StopWatch optimizationTimer,
			int[] visitOrder) throws Throwable {
		QueryGraph queryGraph = new QueryGraph(graph, params);
		WayPoint[] wayPoints = optimizeRoute(params, queryGraph, visitOrder, routingTimer, optimizationTimer);
		// do a final route on the resulting optimally-ordered points
		return doCoreRoute(params, queryGraph, wayPoints);
	}

	private EdgeMerger doCoreRoute(RoutingParameters params, QueryGraph queryGraph, WayPoint[] wayPoints) {
		EdgeList[] edgeLists = new EdgeList[wayPoints.length-1];
		double timeOffset = 0;
		for(int i = 1; i < wayPoints.length; i++) {
			DijkstraShortestPath dsp = new DijkstraShortestPath(queryGraph, params);
			edgeLists[i-1] = dsp.findShortestPath(wayPoints[i-1], wayPoints[i], timeOffset);
			timeOffset += edgeLists[i-1].time(0);
		}
		return new EdgeMerger(edgeLists, queryGraph, params);
	}


	@Override
	public RouterDistanceBetweenPairsResponse distanceBetweenPairs(RoutingParameters params) {
		try {
			StopWatch sw = new StopWatch();
			sw.start();
			RouterDistanceBetweenPairsResponse response = new RouterDistanceBetweenPairsResponse(params, graph.getDates());
			List<Point> fromPoints = params.getFromPoints();
			List<Point> toPoints = params.getToPoints();
			QueryGraph queryGraph = new QueryGraph(graph, params); // TODO doesn't handle to/from points
			WayPoint[] fromEdgeSplits = getWayPoints(queryGraph, fromPoints, params.isCorrectSide(), true);
			WayPoint[] toEdgeSplits = getWayPoints(queryGraph, toPoints, params.isCorrectSide(), true);
			for(int i = 0; i < params.getFromPoints().size(); i++) {
				DijkstraShortestPath dsp = new DijkstraShortestPath(queryGraph, params);
				EdgeList[] edgeLists = dsp.findShortestPaths(fromEdgeSplits[i], toEdgeSplits, 0);
				for(EdgeList edgeList : edgeLists) {
					if(edgeList == null) {
						response.addResult("No Route Found.");
					} else {
						EdgeMerger em = new EdgeMerger(new EdgeList[] {edgeList}, queryGraph, params);
						em.calcDistance(gf);
						response.addResult(em.getDist(), em.getTime());
					}
				}
			}
			sw.stop();
			response.setExecutionTime(sw.getElapsedTime());
			return response;
		} catch(IllegalArgumentException iae) {
			return new RouterDistanceBetweenPairsResponse(params, graph.getDates());
		} catch(Throwable t) {
			logger.warn("Exception thrown: ", t);
			return new RouterDistanceBetweenPairsResponse(params, graph.getDates());
		}
	}

	@Override
	public IsochroneResponse isochrones(RoutingParameters params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IsochroneResponse loop(RoutingParameters params) {
		// TODO Auto-generated method stub
		return null;
	}

	private WayPoint[] optimizeRoute(RoutingParameters params, QueryGraph queryGraph, int[] visitOrder, StopWatch routingTimer, StopWatch optimizationTimer) {
		params.disableOption(RouteOption.TIME_DEPENDENCY);
		WayPoint[] edgeSplits = getWayPoints(queryGraph, params.getPoints(), params.isCorrectSide(), false);

		// shortcut the 2-point case
		if(params.getPoints().size() == 2) {
			visitOrder[0] = 0;
			visitOrder[1] = 1;
			return edgeSplits;
		}
		
		// initialize jsprit cost matrix, non-symmetric
		VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
		routingTimer.start();
		for(int fromIndex = 0; fromIndex < params.getPoints().size(); fromIndex++) {
			DijkstraShortestPath dsp = new DijkstraShortestPath(queryGraph, params);
			EdgeList[] edgeLists = dsp.findShortestPaths(edgeSplits[fromIndex], edgeSplits, 0);
			// add the route costs into the cost matrix
			for(int toIndex = 0; toIndex < edgeLists.length; toIndex++) {
				if(fromIndex == toIndex) {
					// don't add costs from/to the same place (should be 0 anyway)
					continue;
				}
				EdgeMerger em = new EdgeMerger(new EdgeList[] {edgeLists[toIndex]}, queryGraph, params);
				em.calcDistance(gf);
				costMatrixBuilder.addTransportDistance(""+fromIndex, ""+toIndex, em.getDist());
				costMatrixBuilder.addTransportTime(""+fromIndex, ""+toIndex, em.getTime());
			}
		}

		routingTimer.stop();
		optimizationTimer.start();

		// define a vehicle, starting at first point
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(1).setCostPerTransportTime(0).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance("0")).setType(type).setReturnToDepot(params.isRoundTrip()).build();
        
        // define the problem
		Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
				.setFleetSize(FleetSize.FINITE)
				.setRoutingCost(costMatrixBuilder.build())
				.addVehicle(vehicle);
		// add a job to stop at ever point other than the first
		for(int i = 1; i < params.getPoints().size(); i++) {
			vrpBuilder.addJob(Service.Builder.newInstance(""+i).setLocation(Location.newInstance(""+i)).build());
		}
		VehicleRoutingProblem vrp = vrpBuilder.build();
		VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		VehicleRoutingProblemSolution sol = Solutions.bestOf(solutions);

		// reorder the edgeSplits into optimal order
		WayPoint[] optimizedEdgeSplits = new WayPoint[edgeSplits.length + (params.isRoundTrip() ? 1 : 0)];
		optimizedEdgeSplits[0] = edgeSplits[0];
		visitOrder[0] = 0;
		int index = 1;
		for(VehicleRoute route : sol.getRoutes()) {
			for(TourActivity activity : route.getActivities()) {
				int pointIndex = Integer.parseInt(activity.getLocation().getId());
				optimizedEdgeSplits[index] = edgeSplits[pointIndex];
				visitOrder[pointIndex] = index++;
			}
		}
		if(params.isRoundTrip()) {
			optimizedEdgeSplits[index] = edgeSplits[0];
		}
		optimizationTimer.stop();
		return optimizedEdgeSplits;
	}
	
	private WayPoint[] getWayPoints(QueryGraph queryGraph, List<Point> points, boolean correctSide, boolean allowNullEdges) {
		WayPoint[] wayPoints = new WayPoint[points.size()];
		int i = 0;
		for(Point p : points) {
			int nodeId = queryGraph.findNodeId(p);
			if(nodeId == BasicGraphInternal.NO_NODE) {
				if(!allowNullEdges) {
					throw new IllegalArgumentException("Point (" + p.getX() + "," + p.getY() + ") is too far from any edge.");
				}
				wayPoints[i++] = null;
				continue;
			}
			ArrayList<Integer> outgoingEdgeIds = new ArrayList<>();
			ArrayList<Integer> incomingEdgeIds = new ArrayList<>();
			for(int outgoingEdgeId = queryGraph.nextEdge(nodeId, BasicGraphInternal.NO_EDGE); 
					outgoingEdgeId != BasicGraphInternal.NO_EDGE;
					outgoingEdgeId = queryGraph.nextEdge(nodeId, outgoingEdgeId)) {
				// the incoming Edge is the reverse edge between the same nodes as the outgoing Edge
				int incomingEdgeId = queryGraph.getOtherEdgeId(outgoingEdgeId);
				// the incoming splitEdge is the other half of the outgoing edge that was split at this node
				int incomingSplitEdgeId = queryGraph.otherSplitEdge(nodeId, outgoingEdgeId);
				if(incomingEdgeId == BasicGraphInternal.NO_EDGE) {
					// this is a 1-way segment, doesn't matter which side the point is on
					outgoingEdgeIds.add(outgoingEdgeId);
					if(incomingSplitEdgeId != BasicGraphInternal.NO_EDGE) {
						incomingEdgeIds.add(incomingSplitEdgeId);
					}
				} else {
					// this is a 2-way segment 
					if(correctSide && !queryGraph.isDeadEnded(outgoingEdgeId)) {
						// need to check which one is the right side
						int orientation = computeSide(queryGraph.getBaseLineString(outgoingEdgeId), p);
						if(orientation == -1 ^ queryGraph.getReversed(outgoingEdgeId)) {
							// the point is on the right of the forward linestring, or the left of the reversed seg
							outgoingEdgeIds.add(outgoingEdgeId);
							if(incomingSplitEdgeId != BasicGraphInternal.NO_EDGE) {
								incomingEdgeIds.add(incomingSplitEdgeId);
							}
						} else {
							// the point is on the left of the forward seg, or the right of reversed seg
							incomingEdgeIds.add(incomingEdgeId);
							if(incomingSplitEdgeId != BasicGraphInternal.NO_EDGE) {
								outgoingEdgeIds.add(queryGraph.getOtherEdgeId(incomingSplitEdgeId));
							}
						}
					} else {
						// we're not correct-side routing, so include both direction edges
						outgoingEdgeIds.add(outgoingEdgeId);
//						if(incomingSplitEdgeId != BasicGraphInternal.NO_EDGE) {
//							outgoingEdgeIds.add(queryGraph.getOtherEdgeId(incomingSplitEdgeId));
//						}
						incomingEdgeIds.add(incomingEdgeId);
//						if(incomingSplitEdgeId != BasicGraphInternal.NO_EDGE) {
//							incomingEdgeIds.add(incomingSplitEdgeId);
//						}
					}
				}
			}
			wayPoints[i++] = new WayPoint(outgoingEdgeIds, incomingEdgeIds, p);
		}
		return wayPoints;
	}

	@Override
	public NavInfoResponse navInfo(NavInfoParameters params) {
		List<VisFeature> geoms = new ArrayList<VisFeature>();
		List<Integer> edgeIds = graph.findEdgesWithin(params.getEnvelope());
		LocalDateTime dateTime = LocalDateTime.ofInstant(params.getDeparture(), RouterConfig.DEFAULT_TIME_ZONE);
		for(int edgeId : edgeIds) {
			LineString ls = graph.getLineString(edgeId);
			LengthIndexedLine lil = new LengthIndexedLine(ls);
			double offset = 10;
			double maxOffset = lil.getEndIndex() / 3;
			if(offset > maxOffset) {
				offset = maxOffset; 
			}
			// Segment Ids (don't want to duplicate for each edge so only show the forward direction edges)
			if(params.getTypes().contains(NavInfoType.ID) && !graph.getReversed(edgeId)) {
				double proportion = 0.4;
				Coordinate c = lil.extractPoint(lil.getEndIndex()*proportion);
				geoms.add(new VisFeature(gf.createPoint(c), NavInfoType.ID, null, "" + graph.getSegmentId(edgeId), 90));
			}
			// one-way markers
			if(params.getTypes().contains(NavInfoType.DIR)) {
				if(graph.getOtherEdgeId(edgeId) == BasicGraphInternal.NO_EDGE) {
					Coordinate c = lil.extractPoint(lil.getEndIndex()/2);
					Coordinate c2 = lil.extractPoint(1 + lil.getEndIndex()/2);
					int angle = (int) Math.round(Angle.toDegrees(Angle.normalizePositive(Angle.angle(c, c2))));
					geoms.add(new VisFeature(gf.createPoint(c), NavInfoType.DIR, null, null, angle));
				}
			}
			// impactors
			if(params.getTypes().contains(NavInfoType.IMP) && !graph.getReversed(edgeId)) {
				EnumSet<TrafficImpactor> visImps = EnumSet.of(TrafficImpactor.YIELD, TrafficImpactor.STOPSIGN, TrafficImpactor.LIGHT, TrafficImpactor.BARRICADE, TrafficImpactor.ROUNDABOUT);
				TrafficImpactor fromImp = graph.getFromImpactor(edgeId);
				if(visImps.contains(fromImp)) {
					Coordinate c = lil.extractPoint(offset);
					geoms.add(new VisFeature(gf.createPoint(c), NavInfoType.IMP, fromImp.name(), null, 90));
				}
				TrafficImpactor toImp = graph.getToImpactor(edgeId);
				if(visImps.contains(toImp)) {
					Coordinate c = lil.extractPoint(-offset);
					geoms.add(new VisFeature(gf.createPoint(c), NavInfoType.IMP, toImp.name(), null, 90));
				}
			}
			// Hard Restrictions 
			if(params.getTypes().contains(NavInfoType.HR)) {
				List<Constraint> constraints = graph.getRestrictionLookup(params.getRestrictionSource()).lookup(edgeId);
				MapList<Point,Constraint> constraintMap = new MapList<>();
				for(Constraint c : constraints) {
					constraintMap.add(c.getLocation(), c);
				}
				if(!constraintMap.isEmpty()) {
					for(Entry<Point, List<Constraint>> entry: constraintMap.entrySet()) {
						String type = "";
						String source = "";
						StringBuilder sb = new StringBuilder();
						for(Constraint c : entry.getValue()) {
							type = c.getType().name;
							source = c.getSource().toString();
							if(sb.length() > 0) {
								sb.append("\n");
							}
							sb.append(c.getVisDescriptor());
						}
						String hardList = sb.toString();
						geoms.add(new VisFeature(entry.getKey(), NavInfoType.HR, type, source, hardList));
					}
				}
			}
			// Truck Routes
			if(params.getTypes().contains(NavInfoType.TRK) && graph.isTruckRoute(edgeId)) {
				geoms.add(new VisFeature(ls, NavInfoType.TRK, "" + edgeId));
			}
			// Dead Ends
			if(params.getTypes().contains(NavInfoType.DE) && graph.isDeadEnded(edgeId)) {
				geoms.add(new VisFeature(ls, NavInfoType.DE, "" + edgeId));
			}
			// Traffic
			if(params.getTypes().contains(NavInfoType.TF) && !graph.getReversed(edgeId)) {
				short effectiveSpeed = graph.getEffectiveSpeed(edgeId, dateTime);
				short speedLimit = graph.getSpeedLimit(edgeId);
				if(effectiveSpeed > 0) {
					float ratio = (float)effectiveSpeed / (float)speedLimit;
					String speedClass = null;
					if(ratio <= 0.5) {
						speedClass = "SLOWEST";
					} else if(ratio <= 0.7) {
						speedClass = "SLOWER";
					} else if(ratio <= 0.9) {
						speedClass = "SLOW";
					}
					if(speedClass != null) {
						geoms.add(new VisFeature(ls, NavInfoType.TF, speedClass, "" + edgeId, 90));
					}
				}
			}
		}
		// Events and all
		ScheduleLookup scheduleLookup = graph.getScheduleLookup();
		for(NavInfoType type : params.getTypes()) {
			List<VisFeature> features = graph.getVisLayers().featuresWithin(type, params.getEnvelope());
			if(NavInfoType.SC.equals(type)) {
				// add ferry schedule details
				for(VisFeature feature : features) {
					if(feature.getGeometry() instanceof Point) {
						StringBuilder detail = new StringBuilder();
						detail.append("<b>" + feature.getSubType() + "</b><br/>");
						for (String edgeIdStr : feature.getDetail().split(",")) {
							int edgeId = Integer.parseInt(edgeIdStr);
							List<LocalDateTime> departures = scheduleLookup.lookupRange(edgeId, dateTime, dateTime.plusDays(1));
							FerryInfo info = scheduleLookup.getFerryInfo(edgeId);
							String routeName = graph.getName(edgeId);
							detail.append("<b>" + routeName + "</b><br/>Crossing time: " + (info == null ? "unknown" : TimeHelper.formatTime(info.getTravelTime())) + "<br/>");
							if(departures == null) {
								detail.append("Schedule Unknown<br><br>");
							} else if(departures.size() == 0) {
								detail.append("Runs On Demand<br><br>");
							} else {
								LocalDate date = null;
								detail.append("Schedule for next 24 hours:");
								detail.append("<table class=\"schedule\">");
								for(LocalDateTime departure : departures) {
									if(date == null || !date.equals(LocalDate.from(departure))) {
										if(date != null) {
											detail.append("</td></tr>");
										}
										date = LocalDate.from(departure);
										detail.append("<tr><td>" + date + "</td><td>");
									}
									detail.append(LocalTime.from(departure) + "<br/>");
								}
								detail.append("</td></tr></table><br/>");
							}
						}
						geoms.add(new VisFeature(feature.getGeometry(), feature.getType(), feature.getSubType(), detail.toString(), 90));
					} else {
						geoms.add(feature);
					}
				}
			} else {
				geoms.addAll(features);
			}
		}
		return new NavInfoResponse(params, geoms);
	}
	
	// determines which side of a linestring a point is on
	// 1 is left side, -1 is right side, 0 is co-linear 
	private int computeSide(LineString line, Point pt) {
		double minDistance = Double.MAX_VALUE;
		int nearestPointIdx = 0;
		Coordinate[] coord0 = line.getCoordinates();
		Coordinate coord = pt.getCoordinate();
		// brute force approach!
		for (int i = 0; i < coord0.length - 1; i++) {
			double dist = Distance.pointToSegment(coord, coord0[i], coord0[i + 1]);
			if (dist < minDistance) {
				minDistance = dist;
				nearestPointIdx = i;
			}
		}
		return Orientation.index(coord0[nearestPointIdx], coord0[nearestPointIdx+1], pt.getCoordinate());
	  }

	/**
	 * To perform an update we clone the current graph, make changes to it, 
	 * then create a new engine with the new graph and other bits from the old engine.
	 */
	@Override
	public synchronized RoutingEngine getUpdatedEngine(DataUpdateManager dum, SystemStatus status) {
		try {
			BasicGraph newGraph = new BasicGraph(graph);
			RestrictionLookupBuilder rlb = new RestrictionLookupBuilder(graph, graph.getInternalGraph());
			List<Restriction> newRestrictions = dum.fetchRdmRestrictions();
			rlb.addRestrictions(newRestrictions);
			newGraph.setRestrictionLookup(RestrictionSource.RDM, rlb.build());
			status.setDates(newGraph.getDates());
			status.rdmLastSuccessfulUpdate = ZonedDateTime.now().toString();
			status.rdmSuccessfulUpdateCount++;
			status.rdmLastRecordCount = newRestrictions.size();
			return new BasicGraphRoutingEngine(this, newGraph);
		} catch(IOException ioe) {
			status.rdmFailedUpdateCount++;
			status.rdmLastFailedUpdate = ZonedDateTime.now().toString();
			logger.warn("IO Error trying to update router data: {}", ioe.getMessage());
		}
		return this;
	}

	@Override
	public List<StatusMessage> getMessages(Type type) {
		switch(type) {
		case RDM: return graph.getRestrictionLookup(RestrictionSource.RDM).getMessages();
		}
		return Collections.emptyList();
	}
	
}
