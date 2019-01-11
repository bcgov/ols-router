/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.rest.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.app.router.Router;
import ca.bc.gov.app.router.RouterConfig;
import ca.bc.gov.app.router.api.IsochroneResponse;
import ca.bc.gov.app.router.api.NavInfoParameters;
import ca.bc.gov.app.router.api.NavInfoResponse;
import ca.bc.gov.app.router.api.RouterDirectionsResponse;
import ca.bc.gov.app.router.api.RouterDistanceBetweenPairsResponse;
import ca.bc.gov.app.router.api.RouterDistanceResponse;
import ca.bc.gov.app.router.api.RouterOptimalDirectionsResponse;
import ca.bc.gov.app.router.api.RouterOptimalRouteResponse;
import ca.bc.gov.app.router.api.RouterRouteResponse;
import ca.bc.gov.app.router.api.RoutingParameters;
import ca.bc.gov.app.router.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.app.router.rest.exceptions.InvalidParameterException;
import ca.bc.gov.app.router.util.StopWatch;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

@RestController
@CrossOrigin
public class RoutingController {
	final static Logger logger = LoggerFactory.getLogger(RoutingController.class.getCanonicalName());
	
	@Autowired
	private Router router;
	
	@RequestMapping(value = "/distance", method = {RequestMethod.GET,RequestMethod.POST})
	public RouterDistanceResponse distance(RoutingParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		if(params.getPoints() == null) {
			throw new IllegalArgumentException(
					"Parameter \"points\" is required and must be in the format \"x,y,x,y...\".");
		}

		List<Point> points = params.getPoints();
		if(points.size() < 2) {
			throw new IllegalArgumentException(
					"Exactly two parameters named \"point\" are required in the format \"x,y\".");
		}
		
		if(config.getMaxRoutePoints() >= 0 && points.size() > config.getMaxRoutePoints()) {
			throw new IllegalArgumentException(
					"There may not be more than " + config.getMaxRoutePoints() + " points provided.");
		}
		
		StopWatch sw = new StopWatch();
		sw.start();
		RouterDistanceResponse response = router.distance(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}
	
	@RequestMapping(value = "/distance/betweenPairs", method = {RequestMethod.GET,RequestMethod.POST})
	public RouterDistanceBetweenPairsResponse distanceBetweenPairs(RoutingParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		if(params.getFromPoints() == null) {
			throw new IllegalArgumentException(
					"Parameter \"fromPoints\" is required and must be in the format \"x,y,x,y...\".");
		}
		if(params.getToPoints() == null) {
			throw new IllegalArgumentException(
					"Parameter \"toPoints\" is required and must be in the format \"x,y,x,y...\".");
		}
		if(config.getMaxPairs() >= 0 
				&& params.getFromPoints().size() * Math.min(params.getMaxPairs(), params.getToPoints().size()) > config.getMaxPairs()) {
			StringBuilder sb = new StringBuilder();
			sb.append("There may not be more than " + config.getMaxPairs() + " combinations of from and to points; "
					+ "\"fromPoints\" length is " + params.getFromPoints().size() + " and ");
			if(params.getMaxPairs() < params.getToPoints().size()) {
				sb.append("\"maxPairs\" is " + params.getMaxPairs()
						+ " which is a total of " + (params.getFromPoints().size() * params.getMaxPairs()) + " pairs");
			} else {
				sb.append("\"toPoints\" length is " + params.getToPoints().size() 
					+ " which is a total of " + (params.getFromPoints().size() * params.getToPoints().size()) + " pairs");
			}
			throw new IllegalArgumentException(sb.toString());
		}

		StopWatch sw = new StopWatch();
		sw.start();
		RouterDistanceBetweenPairsResponse response = router.distanceBetweenPairs(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@RequestMapping(value = "/optimalRoute", method = {RequestMethod.GET,RequestMethod.POST})
	public RouterOptimalRouteResponse optimalRoute(RoutingParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		List<Point> points = params.getPoints();
		if(points == null || points.size() < 2) {
			throw new IllegalArgumentException(
					"Parameter \"points\" is required and must be in the format \"x,y,x,y...\".");
		}
		if(config.getMaxOptimalRoutePoints() >= 0 && points.size() > config.getMaxOptimalRoutePoints()) {
			throw new IllegalArgumentException(
					"There may not be more than " + config.getMaxOptimalRoutePoints() + " points provided.");
		}
		
		StopWatch sw = new StopWatch();
		sw.start();
		RouterOptimalRouteResponse response = router.optimalRoute(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@RequestMapping(value = "/route", method = {RequestMethod.GET,RequestMethod.POST})
	public RouterRouteResponse route(RoutingParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		List<Point> points = params.getPoints();
		if(points == null || points.size() < 2) {
			throw new IllegalArgumentException(
					"Parameter \"points\" is required and must be in the format \"x,y,x,y...\".");
		}
		if(config.getMaxRoutePoints() >= 0 && points.size() > config.getMaxRoutePoints()) {
			throw new IllegalArgumentException(
					"There may not be more than " + config.getMaxRoutePoints() + " points provided.");
		}
		
		StopWatch sw = new StopWatch();
		sw.start();
		RouterRouteResponse response = router.route(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@RequestMapping(value = "/optimalDirections", method = {RequestMethod.GET,RequestMethod.POST})
	public RouterOptimalDirectionsResponse optimalDirections(RoutingParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		List<Point> points = params.getPoints();
		if(points == null || points.size() < 2) {
			throw new IllegalArgumentException(
					"Parameter \"points\" is required and must be in the format \"x,y,x,y...\".");
		}
		if(config.getMaxOptimalRoutePoints() >= 0 && points.size() > config.getMaxOptimalRoutePoints()) {
			throw new IllegalArgumentException(
					"There may not be more than " + config.getMaxOptimalRoutePoints() + " points provided.");
		}
		StopWatch sw = new StopWatch();
		sw.start();
		RouterOptimalDirectionsResponse response = router.optimalDirections(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@RequestMapping(value = "/directions", method = {RequestMethod.GET,RequestMethod.POST})
	public RouterDirectionsResponse directions(RoutingParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		List<Point> points = params.getPoints();
		if(points == null || points.size() < 2) {
			throw new IllegalArgumentException(
					"Parameter \"points\" is required and must be in the format \"x,y,x,y...\".");
		}
		if(config.getMaxRoutePoints() >= 0 && points.size() > config.getMaxRoutePoints()) {
			throw new IllegalArgumentException(
					"There may not be more than " + config.getMaxRoutePoints() + " points provided.");
		}
		
		StopWatch sw = new StopWatch();
		sw.start();
		RouterDirectionsResponse response = router.directions(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@RequestMapping(value = "/isochrones", method = {RequestMethod.GET,RequestMethod.POST})
	public IsochroneResponse isochrones(RoutingParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		Point point = params.getPoint();
		if(point == null) {
			throw new IllegalArgumentException(
					"Parameter \"point\" is required to contain a single point in the format \"x,y\".");
		}
		if(params.getZoneCount() < 1 || params.getZoneCount() > 10) {
			throw new IllegalArgumentException(
					"Invalid number of zones/contours: " + params.getZoneCount() + ", must be between 1 and 10.");
		}
		if(params.getZoneSize() < 1 ) {
			throw new IllegalArgumentException(
					"Invalid zoneSize: " + params.getZoneSize() + ", must be greater than 1 minute/meter.");
		}

		StopWatch sw = new StopWatch();
		sw.start();
		IsochroneResponse response = router.isochrones(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@RequestMapping(value = "/loop", method = {RequestMethod.GET,RequestMethod.POST})
	public IsochroneResponse loop(RoutingParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		Point point = params.getPoint();
		if(point == null) {
			throw new IllegalArgumentException(
					"Parameter \"point\" is required to contain a single point in the format \"x,y\".");
		}

		StopWatch sw = new StopWatch();
		sw.start();
		IsochroneResponse response = router.loop(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}
	
	@RequestMapping(value = "/navInfo", method = {RequestMethod.GET,RequestMethod.POST})
	public NavInfoResponse navInfo(NavInfoParameters params, BindingResult bindingResult) {
		RouterConfig config = router.getConfig();
		if(bindingResult.hasErrors()) {
			throw new InvalidParameterException(bindingResult);
		}
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		Envelope env = params.getEnvelope();
		if(env == null) {
			throw new IllegalArgumentException(
					"Parameter \"bbox\" is required in the format \"minx,miny,maxx,maxy\".");
		}

		NavInfoResponse response = router.navInfo(params);
		return response;
	}
}
