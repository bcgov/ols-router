/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.graphhopper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
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
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

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
import ca.bc.gov.ols.router.data.enums.DistanceUnit;
import ca.bc.gov.ols.router.data.enums.RoutingCriteria;
import ca.bc.gov.ols.router.datasource.RouterDataLoader;
import ca.bc.gov.ols.router.datasource.RouterDataSource;
import ca.bc.gov.ols.router.directions.Direction;
import ca.bc.gov.ols.router.directions.FinishDirection;
import ca.bc.gov.ols.router.directions.StreetDirection;
import ca.bc.gov.ols.router.directions.StreetDirectionType;
import ca.bc.gov.ols.util.StopWatch;

public class GraphHopperRoutingEngine implements RoutingEngine {
	private static final Logger logger = LoggerFactory.getLogger(GraphHopperRoutingEngine.class.getCanonicalName());
	
	public static final int GH_SRS = 4326;
			
	private RouterConfig config;
	
	/* This is the global geometry factory, all geometries are created using it */
	private GeometryFactory geometryFactory;
	
	GeometryReprojector reprojector;
	
	private RouterGraphHopper graphHopper;
		
	public GraphHopperRoutingEngine(RouterConfig config, RouterDataSource dataSource,
			GeometryFactory geometryFactory, GeometryReprojector reprojector) throws IOException {
		logger.trace("{}() constructor called",getClass().getName());
		// were are just going to make our own factory because it has to produce geometries with SRID=GH_SRS 
		//this.geometryFactory = geometryFactory;
		this.geometryFactory = new GeometryFactory(geometryFactory.getPrecisionModel(), GH_SRS);
		this.reprojector = reprojector;
		this.config = config;
		
		GraphHopperGraphBuilder graphBuilder = new GraphHopperGraphBuilder(reprojector);

		RouterDataLoader loader = new RouterDataLoader(config, dataSource, graphBuilder);
		loader.loadData();
		graphHopper = new RouterGraphHopper(graphBuilder);
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.RoutingEngine#distance(ca.bc.gov.app.router.api.RoutingParameters)
	 */
	@Override
	public RouterDistanceResponse distance(RoutingParameters params) {
		List<GHPoint> ghPoints = toGHPointList(params.getPoints());
		if(params.isRoundTrip()) {
			ghPoints.add((GHPoint)(ghPoints.get(0)));
		}
		GHRequest ghRequest = new GHRequest(ghPoints);
		ghRequest.setWeighting(params.getCriteria().toString());
		GHResponse ghResponse;
		ghResponse = graphHopper.route(ghRequest);
		RouterDistanceResponse response;
		if(ghResponse.hasErrors()) {
			response = new RouterDistanceResponse(params);
		} else {
			response = new RouterDistanceResponse(params, 
					DistanceUnit.METRE.convertTo(ghResponse.getDistance(), params.getDistanceUnit()), 
					ghResponse.getTime()/1000);
		}

		return response;
	}
	
	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.RoutingEngine#route(ca.bc.gov.app.router.api.RoutingParameters)
	 */
	@Override
	public RouterRouteResponse route(RoutingParameters params) {
		List<GHPoint> ghPoints = toGHPointList(params.getPoints());
		if(params.isRoundTrip()) {
			ghPoints.add((GHPoint)(ghPoints.get(0)));
		}
		GHRequest ghRequest = new GHRequest(ghPoints);
		ghRequest.setWeighting(params.getCriteria().toString());
		GHResponse ghResponse;
		ghResponse = graphHopper.route(ghRequest);
		RouterRouteResponse response;
		if(ghResponse.hasErrors()) {
			response = new RouterRouteResponse(params);
		} else {
			response = new RouterRouteResponse(params, 
					DistanceUnit.METRE.convertTo(ghResponse.getDistance(), params.getDistanceUnit()), 
					ghResponse.getTime()/1000, pointListToLineString(ghResponse.getPoints()), null);
		}
		return response;
	}
	
	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.RoutingEngine#directions(ca.bc.gov.app.router.api.RoutingParameters)
	 */
	@Override
	public RouterDirectionsResponse directions(RoutingParameters params) {
		List<GHPoint> ghPoints = toGHPointList(params.getPoints());
		if(params.isRoundTrip()) {
			ghPoints.add((GHPoint)(ghPoints.get(0)));
		}
		GHRequest ghRequest = new GHRequest(ghPoints);		
		ghRequest.setWeighting(params.getCriteria().toString());
		GHResponse ghResponse;
		ghResponse = graphHopper.route(ghRequest);
		RouterDirectionsResponse response;
		if(ghResponse.hasErrors()) {
			response = new RouterDirectionsResponse(params);
		} else {
			response = new RouterDirectionsResponse(params,  
					DistanceUnit.METRE.convertTo(ghResponse.getDistance(), params.getDistanceUnit()), 
					ghResponse.getTime()/1000, pointListToLineString(ghResponse.getPoints()), null,
					directionsFromInstructionList(ghResponse.getInstructions(), params), Collections.emptyList());
		}
		return response;
	}

//  Old Slow way
//	public RouterDistanceBetweenPairsResponse distanceBetweenPairs(RoutingParameters params) {
//		RouterDistanceBetweenPairsResponse response = new RouterDistanceBetweenPairsResponse(params);
//		response.setFromPoints(params.getFromPoints());
//		response.setToPoints(params.getToPoints());
//		for(Point from : params.getFromPoints()) {
//			for(Point to : params.getToPoints()) {
//				GHRequest ghRequest = new GHRequest(toGHPointList(from, to));			
//				ghRequest.setWeighting(params.getCriteria().toString());
//				GHResponse ghResponse = graphHopper.route(ghRequest);
//				response.add(ghResponse);
//			}
//		}
//		return response;
//	}

	// new hotness
	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.RoutingEngine#distanceBetweenPairs(ca.bc.gov.app.router.api.RoutingParameters)
	 */
	@Override
	public RouterDistanceBetweenPairsResponse distanceBetweenPairs(RoutingParameters params) {
		RouterDistanceBetweenPairsResponse response = new RouterDistanceBetweenPairsResponse(params);
		for(Point from : params.getFromPoints()) {
			GHRequest ghRequest = new GHRequest(toGHPointList(from, params.getToPoints()));			
			ghRequest.setWeighting(params.getCriteria().toString());
			ghRequest.getHints().put("maxPairs", params.getMaxPairs());
			List<GHResponse> ghResponses = graphHopper.route1ToMany(ghRequest);
			ghResponses.forEach(ghResponse->{
				if(ghResponse.hasErrors()) {
					StringBuilder sb = new StringBuilder();
					for(Throwable error : ghResponse.getErrors()) {
						sb.append(" " + error.getMessage() + ";");
					}
					response.addResult(sb.toString());
				} else {
					response.addResult(DistanceUnit.METRE.convertTo((float)ghResponse.getDistance(), response.getDistanceUnit()), 
							ghResponse.getTime()/1000.0d);
				}
			});
		}
		return response;
	}
		
	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.RoutingEngine#optimalRoute(ca.bc.gov.app.router.api.RoutingParameters)
	 */
	@Override
	public RouterOptimalRouteResponse optimalRoute(RoutingParameters params) {
		StopWatch routingTimer = new StopWatch();
		StopWatch optimizationTimer = new StopWatch();
		int[] visitOrder = new int[params.getPoints().size()];
		RouterOptimalRouteResponse response;
		try {
			List<GHPoint> optimizedGHPoints = optimizeRoute(params, visitOrder, routingTimer, optimizationTimer);
			// do a final route on the resulting optimally-ordered points
			GHRequest ghRequest = new GHRequest(optimizedGHPoints);
			ghRequest.setWeighting(params.getCriteria().toString());
			GHResponse ghResponse = graphHopper.route(ghRequest);
			if(ghResponse.hasErrors()) {
				response = new RouterOptimalRouteResponse(params);
			} else {
				response = new RouterOptimalRouteResponse(params,  
						DistanceUnit.METRE.convertTo(ghResponse.getDistance(), params.getDistanceUnit()), 
						ghResponse.getTime()/1000, pointListToLineString(ghResponse.getPoints()), null, visitOrder);
			}
		} catch(Throwable t) {
			response = new RouterOptimalRouteResponse(params);
		}			
		response.setRoutingExecutionTime(routingTimer.getElapsedTime());
		response.setOptimizationExecutionTime(optimizationTimer.getElapsedTime());
		return response;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.RoutingEngine#optimalDirections(ca.bc.gov.app.router.api.RoutingParameters)
	 */
	@Override
	public RouterOptimalDirectionsResponse optimalDirections(RoutingParameters params) {
		StopWatch routingTimer = new StopWatch();
		StopWatch optimizationTimer = new StopWatch();
		int[] visitOrder = new int[params.getPoints().size()];
		RouterOptimalDirectionsResponse response;
		try {
			List<GHPoint> optimizedGHPoints = optimizeRoute(params, visitOrder, routingTimer, optimizationTimer);
			// do a final route on the resulting optimally-ordered points
			GHRequest ghRequest = new GHRequest(optimizedGHPoints);
			ghRequest.setWeighting(params.getCriteria().toString());
			GHResponse ghResponse = graphHopper.route(ghRequest);
			if(ghResponse.hasErrors()) {
				response = new RouterOptimalDirectionsResponse(params);
			} else {
				response = new RouterOptimalDirectionsResponse(params,  
						DistanceUnit.METRE.convertTo(ghResponse.getDistance(), params.getDistanceUnit()), 
						ghResponse.getTime()/1000, pointListToLineString(ghResponse.getPoints()), null,
						directionsFromInstructionList(ghResponse.getInstructions(), params), Collections.emptyList(), visitOrder);
			}
		} catch(Throwable t) {
			response = new RouterOptimalDirectionsResponse(params);
		}
		response.setRoutingExecutionTime(routingTimer.getElapsedTime());
		response.setOptimizationExecutionTime(optimizationTimer.getElapsedTime());
		return response;
	}
	
	private List<GHPoint> optimizeRoute(RoutingParameters params, int[] visitOrder, StopWatch routingTimer, StopWatch optimizationTimer) throws Throwable {
		if(params.getPoints().size() == 2) {
			visitOrder[0] = 0;
			visitOrder[1] = 1;
			return toGHPointList(params.getPoints());
		}
		// initialize jsprit cost matrix, non-symmetric
		VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
		routingTimer.start();
		List<GHPoint> ghPoints = toGHPointList(params.getPoints());
		List<GHPoint> queryGHPoints = new ArrayList<GHPoint>(ghPoints.size() + 1);
		queryGHPoints.add(ghPoints.get(0));
		queryGHPoints.addAll(ghPoints);
		for(int fromIndex = 0; fromIndex < params.getPoints().size(); fromIndex++) {
			queryGHPoints.set(0,ghPoints.get(fromIndex));
			GHRequest ghRequest = new GHRequest(queryGHPoints);			
			ghRequest.setWeighting(params.getCriteria().toString());
			List<GHResponse> ghResponses = graphHopper.route1ToMany(ghRequest);
			// add the route costs into the cost matrix
			for(int toIndex = 0; toIndex < ghResponses.size(); toIndex++) {
				if(fromIndex == toIndex) {
					// don't add costs from/to the same place (should be 0 anyway)
					continue;
				}
				GHResponse ghr = ghResponses.get(toIndex);
				if(ghr.hasErrors()) {
					throw ghr.getErrors().get(1);
				}
				costMatrixBuilder.addTransportDistance(""+fromIndex, ""+toIndex, ghr.getDistance());
				costMatrixBuilder.addTransportTime(""+fromIndex, ""+toIndex, ghr.getTime());
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

		// reorder the points into optimal order
		List<GHPoint> optimizedGHPoints = new ArrayList<GHPoint>(ghPoints.size() + (params.isRoundTrip() ? 1 : 0));
		optimizedGHPoints.add(ghPoints.get(0));
		visitOrder[0] = 0 ;
		int index = 1;
		for(VehicleRoute route : sol.getRoutes()) {
			for(TourActivity activity : route.getActivities()) {
				int pointIndex = Integer.parseInt(activity.getLocation().getId());
				optimizedGHPoints.add(ghPoints.get(pointIndex));
				visitOrder[pointIndex] = index++;
			}
		}
		if(params.isRoundTrip()) {
			optimizedGHPoints.add(ghPoints.get(0));
		}
		optimizationTimer.stop();
		return optimizedGHPoints;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.RoutingEngine#isochrones(ca.bc.gov.app.router.api.RoutingParameters)
	 */
	@Override
	public IsochroneResponse isochrones(RoutingParameters params) {
		List<GHPoint> ghPoints = toGHPointList(params.getPoint());
		
		// determine which segments are contained in each contour
		GHRequest ghRequest = new GHRequest(ghPoints);
		ghRequest.setWeighting(RoutingCriteria.FASTEST.toString());
		ghRequest.getHints().put("zoneSize", params.getZoneSize() * 60); // convert minutes into seconds
		ghRequest.getHints().put("zoneCount", params.getZoneCount());
		ghRequest.getHints().put("reverseFlow", params.isInbound());
		List<Geometry> polygons = graphHopper.isoline(ghRequest);
		
		// TODO in progress
		IsochroneResponse response = new IsochroneResponse(polygons, params);
		return response;
	}

	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.RoutingEngine#loop(ca.bc.gov.app.router.api.RoutingParameters)
	 */
	@Override
	public IsochroneResponse loop(RoutingParameters params) {
		List<GHPoint> ghPoints = toGHPointList(params.getPoint());
		
		// determine which segments are contained in each contour
		GHRequest ghRequest = new GHRequest(ghPoints);
		ghRequest.setWeighting(RoutingCriteria.FASTEST.toString());
		List<Geometry> polygons = graphHopper.loop(ghRequest);
		
		// TODO in progress
		IsochroneResponse response = new IsochroneResponse(polygons, params);
		return response;
	}

	private static List<GHPoint> toGHPointList(List<Point> points) {
		List<GHPoint> ghPoints = new ArrayList<GHPoint>(points.size());
		for(Point p : points) {
			ghPoints.add(new GHPoint(p.getY(), p.getX()));
		}
		return ghPoints;
	}

	private static List<GHPoint> toGHPointList(Point from, List<Point> toPoints) {
		List<GHPoint> ghPoints = new ArrayList<GHPoint>(toPoints.size() + 1);
		ghPoints.add(new GHPoint(from.getY(), from.getX()));
		for(Point p : toPoints) {
			ghPoints.add(new GHPoint(p.getY(), p.getX()));
		}
		return ghPoints;
	}

	private static List<GHPoint> toGHPointList(Point p) {
		List<GHPoint> ghPoints = new ArrayList<GHPoint>(2);
		ghPoints.add(new GHPoint(p.getY(),p.getX()));
		return ghPoints;
	}

	private static List<GHPoint> toGHPointList(Point from, Point to) {
		List<GHPoint> ghPoints = new ArrayList<GHPoint>(2);
		ghPoints.add(new GHPoint(from.getY(), from.getX()));
		ghPoints.add(new GHPoint(to.getY(), to.getX()));
		return ghPoints;
	}

	private LineString pointListToLineString(PointList ghPoints) {
		CoordinateSequence coords = null;
		if(ghPoints.size() == 1) {
			Coordinate[] coordArray = new Coordinate[2];
			coordArray[0] = new Coordinate(ghPoints.getLon(0), ghPoints.getLat(0));
			coordArray[1] = new Coordinate(ghPoints.getLon(0), ghPoints.getLat(0));
			coords = new CoordinateArraySequence(coordArray);
		} else if(ghPoints.size() > 1) {
			coords = new GraphHopperCoordinateSequence(ghPoints);
		}
		return new LineString(coords, geometryFactory);
	}
	
	private List<Direction> directionsFromInstructionList(InstructionList il, RoutingParameters params) {
		//List<Map<String, Object>> instrs = il.createJson();
		List<Direction> directions = new ArrayList<Direction>(il.size());
		for(int i = 0; i < il.size(); i++) {
			directions.add(DirectionFromInstruction(il.get(i), params));			
		}
		return directions;
	}
	
	private Direction DirectionFromInstruction(Instruction ins, RoutingParameters params) {
		int sign = ins.getSign();
		Point p = geometryFactory.createPoint(new Coordinate(ins.getPoints().getLon(0), ins.getPoints().getLat(0)));
		StreetDirectionType type = null;
		switch(sign) {
		case Instruction.FINISH: 
			return new FinishDirection(p);
		case Instruction.TURN_SLIGHT_LEFT: 
			type = StreetDirectionType.TURN_SLIGHT_LEFT;
			break;
		case Instruction.TURN_LEFT: 
			type = StreetDirectionType.TURN_LEFT;
			break;
		case Instruction.TURN_SHARP_LEFT:
			type = StreetDirectionType.TURN_SHARP_LEFT;
			break;
		case Instruction.TURN_SLIGHT_RIGHT: 
			type = StreetDirectionType.TURN_SLIGHT_RIGHT;
			break;
		case Instruction.TURN_RIGHT: 
			type = StreetDirectionType.TURN_RIGHT;
			break;
		case Instruction.TURN_SHARP_RIGHT: 
			type = StreetDirectionType.TURN_SHARP_RIGHT;
			break;
		case Instruction.CONTINUE_ON_STREET:
		default: 
			type = StreetDirectionType.CONTINUE;
		}
		return new StreetDirection(p, type, ins.getName(), 
				DistanceUnit.METRE.convertTo(ins.getDistance(), params.getDistanceUnit()), ins.getTime()/1000);
	}

	@Override
	public NavInfoResponse navInfo(NavInfoParameters params) {
		return new NavInfoResponse(params, Collections.emptyList());
	}
	
//	private List<Direction> directionsFromInstructionList(InstructionList il, RoutingParameters params) {
//		List<Map<String, Object>> instrs = il.createJson();
//		List<String> directions = new ArrayList<String>(instrs.size());
//		for(int i = 0; i < instrs.size(); i++) {
//			Instruction in = il.get(i);
//	        Map<String, Object> instr = instrs.get(i);
//			String distStr = params.getDistanceUnit().formatForDirections(
//					DistanceUnit.METRE.convertTo(in.getDistance(), params.getDistanceUnit()));
//			String dir = (String)instr.get("text") + (distStr.isEmpty() ? "" : (" for " + distStr))
//					+ (in.getTime() == 0 ? "" : (" (" + TimeHelper.formatTime(in.getTime()/1000) + ")"));
//			directions.add(dir);			
//		}
//		return directions;
//	}
	
}
