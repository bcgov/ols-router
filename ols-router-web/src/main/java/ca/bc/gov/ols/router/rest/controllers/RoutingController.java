/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.api.DefaultsResponse;
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
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.ols.router.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.util.StopWatch;

@RestController
@CrossOrigin
public class RoutingController {
	final static Logger logger = LoggerFactory.getLogger(RoutingController.class.getCanonicalName());
	
	@Autowired
	private Router router;
	
	@RequestMapping(value = "/ping", method = {RequestMethod.GET})
	public ResponseEntity<String> ping() {
		RoutingParameters params = new RoutingParameters();
		params.setPoints(new double[] {-123.36487770080568, 48.42547002823357, -123.37015628814699, 48.41812208203614});
		RouterConfig config = router.getConfig();
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		RouterDistanceResponse response = router.distance(params);
		if(response.getDistanceStr().equals("")) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
		}
		return ResponseEntity.ok(null);
	}

	@RequestMapping(value = {"/distance","/{vehicleType}/distance"}, method = {RequestMethod.GET})
	public RouterDistanceResponse distanceGet(RoutingParameters params, BindingResult bindingResult) {
		return distance(params, bindingResult);
	}

	@RequestMapping(value = {"/distance","/{vehicleType}/distance"}, method = {RequestMethod.POST})
	public RouterDistanceResponse distancePost(RoutingParameters params, BindingResult bindingResult) {
		return distance(params, bindingResult);
	}

	private RouterDistanceResponse distance(RoutingParameters params, BindingResult bindingResult) {
		validateRouteRequest(params, bindingResult);
		return router.distance(params);
	}

	@RequestMapping(value = {"/route","/{vehicleType}/route"}, method = {RequestMethod.GET})
	public RouterRouteResponse routeGet(RoutingParameters params, BindingResult bindingResult) {
		return route(params, bindingResult);
	}

	@RequestMapping(value = {"/route","/{vehicleType}/route"}, method = {RequestMethod.POST})
	public RouterRouteResponse routePost(RoutingParameters params, BindingResult bindingResult) {
		return route(params, bindingResult);
	}

	private RouterRouteResponse route(RoutingParameters params, BindingResult bindingResult) {
		validateRouteRequest(params, bindingResult);
		return router.route(params);
	}

	@RequestMapping(value = {"/directions","/{vehicleType}/directions"}, method = {RequestMethod.GET})
	public RouterDirectionsResponse directionsGet(RoutingParameters params, BindingResult bindingResult) {
		return directions(params, bindingResult);
	}

	@RequestMapping(value = {"/directions","/{vehicleType}/directions"}, method = {RequestMethod.POST})
	public RouterDirectionsResponse directionsPost(RoutingParameters params, BindingResult bindingResult) {
		return directions(params, bindingResult);
	}
	
	private RouterDirectionsResponse directions(RoutingParameters params, BindingResult bindingResult) {
		validateRouteRequest(params, bindingResult);
		return router.directions(params);
	}

	@RequestMapping(value = {"/optimalRoute","/{vehicleType}/optimalRoute"}, method = {RequestMethod.GET})
	public RouterOptimalRouteResponse optimalRouteGet(RoutingParameters params, BindingResult bindingResult) {
		return optimalRoute(params, bindingResult);
	}

	@RequestMapping(value = {"/optimalRoute","/{vehicleType}/optimalRoute"}, method = {RequestMethod.POST})
	public RouterOptimalRouteResponse optimalRoutePost(RoutingParameters params, BindingResult bindingResult) {
		return optimalRoute(params, bindingResult);
	}

	private RouterOptimalRouteResponse optimalRoute(RoutingParameters params, BindingResult bindingResult) {
		validateOptimalRouteRequest(params, bindingResult);
		
		StopWatch sw = new StopWatch();
		sw.start();
		RouterOptimalRouteResponse response = router.optimalRoute(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@RequestMapping(value = {"/optimalDirections","/{vehicleType}/optimalDirections"}, method = {RequestMethod.GET})
	public RouterOptimalDirectionsResponse optimalDirectionsGet(RoutingParameters params, BindingResult bindingResult) {
		return optimalDirections(params, bindingResult);
	}
	
	@RequestMapping(value = {"/optimalDirections","/{vehicleType}/optimalDirections"}, method = {RequestMethod.POST})
	public RouterOptimalDirectionsResponse optimalDirectionsPost(RoutingParameters params, BindingResult bindingResult) {
		return optimalDirections(params, bindingResult);
	}

	private RouterOptimalDirectionsResponse optimalDirections(RoutingParameters params, BindingResult bindingResult) {
		validateOptimalRouteRequest(params, bindingResult);
		StopWatch sw = new StopWatch();
		sw.start();
		RouterOptimalDirectionsResponse response = router.optimalDirections(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@RequestMapping(value = "/distance/betweenPairs", method = {RequestMethod.GET})
	public RouterDistanceBetweenPairsResponse distanceBetweenPairsGet(RoutingParameters params, BindingResult bindingResult) {
		return distanceBetweenPairs(params, bindingResult);
	}

	@RequestMapping(value = "/distance/betweenPairs", method = {RequestMethod.POST})
	public RouterDistanceBetweenPairsResponse distanceBetweenPairsPost(RoutingParameters params, BindingResult bindingResult) {
		return distanceBetweenPairs(params, bindingResult);
	}

	private RouterDistanceBetweenPairsResponse distanceBetweenPairs(RoutingParameters params, BindingResult bindingResult) {
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

	@RequestMapping(value = "/defaults", method = {RequestMethod.GET})
	public DefaultsResponse defaults() {
		return new DefaultsResponse(router.getConfig());
	}
	
	private void validateRouteRequest(RoutingParameters params, BindingResult bindingResult) {
		validateRouteRequest(params, bindingResult, router.getConfig().getMaxRoutePoints());		
	}

	private void validateOptimalRouteRequest(RoutingParameters params, BindingResult bindingResult) {
		validateRouteRequest(params, bindingResult, router.getConfig().getMaxOptimalRoutePoints());		
	}

	private void validateRouteRequest(RoutingParameters params, BindingResult bindingResult, int maxPoints) {
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
	}

	@RequestMapping(value = "/isochrones", method = {RequestMethod.GET})
	public IsochroneResponse isochronesGet(RoutingParameters params, BindingResult bindingResult) {
		return isochrones(params, bindingResult);
	}

	@RequestMapping(value = "/isochrones", method = {RequestMethod.POST})
	public IsochroneResponse isochronesPost(RoutingParameters params, BindingResult bindingResult) {
		return isochrones(params, bindingResult);
	}

	private IsochroneResponse isochrones(RoutingParameters params, BindingResult bindingResult) {
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

	@RequestMapping(value = "/loop", method = {RequestMethod.GET})
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
	
	@RequestMapping(value = "/navInfo", method = {RequestMethod.GET})
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
