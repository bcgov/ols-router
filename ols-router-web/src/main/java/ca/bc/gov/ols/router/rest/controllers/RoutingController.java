/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
import ca.bc.gov.ols.router.rest.GeotoolsGeometryReprojector;
import ca.bc.gov.ols.router.rest.exceptions.InvalidParameterException;
import ca.bc.gov.ols.router.status.StatusMessage;
import ca.bc.gov.ols.router.status.SystemStatus;
import ca.bc.gov.ols.util.StopWatch;

@RestController
@CrossOrigin
public class RoutingController {
	final static Logger logger = LoggerFactory.getLogger(RoutingController.class.getCanonicalName());
	
	@Autowired
	private Router router;

	@Operation(
		summary = "Service root endpoint",
		description = "Returns a sample distance calculation between two points in downtown Victoria "
				+ "to verify the service is operational.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "A sample distance response confirming the service is operational.")
	})
	@RequestMapping(value = "/", method = {RequestMethod.GET})
	public RouterDistanceResponse routerDefault() {
		RoutingParameters params = new RoutingParameters();
		params.setPoints(new double[] {-123.36487770080568, 48.42547002823357, -123.37015628814699, 48.41812208203614});
		RouterConfig config = router.getConfig();
		params.resolve(config,
				new GeometryFactory(new PrecisionModel(), params.getOutputSRS()),
				new GeotoolsGeometryReprojector());
		return router.distance(params);
	}

	@Operation(
		summary = "Ping endpoint",
		description = "Verifies the service is operational by performing a distance calculation. "
				+ "Returns HTTP 200 when the router can produce a result, otherwise HTTP 503.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The service is operational."),
		@ApiResponse(responseCode = "503", description = "The service is not operational.")
	})
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
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@Operation(
		summary = "Distance between points",
		description = "Returns the length and duration of the shortest or fastest route "
				+ "between the given points. The vehicle type can optionally be specified in the path.",
		tags = {"Distance"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The route length and travel time between the given points.")
	})
	@RequestMapping(value = {"/distance","/{vehicleType}/distance"}, method = {RequestMethod.GET, RequestMethod.POST})
	public RouterDistanceResponse distance(@ParameterObject RoutingParameters params, BindingResult bindingResult) {
		validateRouteRequest(params, bindingResult);
		return router.distance(params);
	}

	@Operation(
		summary = "Route between points",
		description = "Returns the geometry of the shortest or fastest route between the given points, "
				+ "along with its length and duration. The vehicle type can optionally be specified in the path.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The route geometry, length, and travel time between the given points.")
	})
	@RequestMapping(value = {"/route","/{vehicleType}/route"}, method = {RequestMethod.GET, RequestMethod.POST})
	public RouterRouteResponse route(@ParameterObject RoutingParameters params, BindingResult bindingResult) {
		validateRouteRequest(params, bindingResult);
		return router.route(params);
	}

	@Operation(
		summary = "Turn-by-turn directions",
		description = "Returns turn-by-turn directions for the shortest or fastest route between the given points, "
				+ "including the route geometry, length, and duration. "
				+ "The vehicle type can optionally be specified in the path.",
		tags = {"Directions"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Turn-by-turn directions for the route between the given points.")
	})
	@RequestMapping(value = {"/directions","/{vehicleType}/directions"}, method = {RequestMethod.GET, RequestMethod.POST})
	public RouterDirectionsResponse directions(@ParameterObject RoutingParameters params, BindingResult bindingResult) {
		validateRouteRequest(params, bindingResult);
		return router.directions(params);
	}

	@Operation(
		summary = "Optimal route through multiple points",
		description = "Returns the optimal route that visits all of the given points in the most efficient order, "
				+ "along with the route geometry, length, and duration. "
				+ "The vehicle type can optionally be specified in the path.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The optimal route visiting all of the given points.")
	})
	@RequestMapping(value = {"/optimalRoute","/{vehicleType}/optimalRoute"}, method = {RequestMethod.GET, RequestMethod.POST})
	public RouterOptimalRouteResponse optimalRouteGet(@ParameterObject RoutingParameters params, BindingResult bindingResult) {
		validateOptimalRouteRequest(params, bindingResult);
		
		StopWatch sw = new StopWatch();
		sw.start();
		RouterOptimalRouteResponse response = router.optimalRoute(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@Operation(
		summary = "Optimal route directions",
		description = "Returns turn-by-turn directions for the optimal route that visits all of the given points "
				+ "in the most efficient order, including the route geometry, length, and duration. "
				+ "The vehicle type can optionally be specified in the path.",
		tags = {"Directions"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Turn-by-turn directions for the optimal route visiting all of the given points.")
	})
	@RequestMapping(value = {"/optimalDirections","/{vehicleType}/optimalDirections"}, method = {RequestMethod.GET, RequestMethod.POST})
	public RouterOptimalDirectionsResponse optimalDirections(@ParameterObject RoutingParameters params, BindingResult bindingResult) {
		validateOptimalRouteRequest(params, bindingResult);
		StopWatch sw = new StopWatch();
		sw.start();
		RouterOptimalDirectionsResponse response = router.optimalDirections(params);
		sw.stop();
		
		response.setExecutionTime(sw.getElapsedTime());
		return response;
	}

	@Operation(
		summary = "Distance for all pairs of points",
		description = "Returns the length and duration of the shortest or fastest route between each "
				+ "pair of the given from and to points.",
		tags = {"Distance"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The route lengths and travel times between each pair of from and to points.")
	})
	@RequestMapping(value = "/distance/betweenPairs", method = {RequestMethod.GET, RequestMethod.POST})
	public RouterDistanceBetweenPairsResponse distanceBetweenPairs(@ParameterObject RoutingParameters params, BindingResult bindingResult) {
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

	@Operation(
		summary = "Default routing parameters",
		description = "Returns the default values of the routing parameters configured for the service.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The default routing parameter values.")
	})
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

	@Operation(
		summary = "Isochrone zones",
		description = "Returns one or more isochrone zones showing the area reachable from the given point "
				+ "within the specified time or distance.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "One or more isochrone zones as polygons.")
	})
	@RequestMapping(value = "/isochrones", method = {RequestMethod.GET, RequestMethod.POST})
	public IsochroneResponse isochrones(@ParameterObject RoutingParameters params, BindingResult bindingResult) {
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

	@Operation(
		summary = "Loop around a point",
		description = "Returns a loop route highlighting the boundary of the area reachable from the given point "
				+ "within the specified time or distance.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The loop route geometry as a polygon.")
	})
	@RequestMapping(value = "/loop", method = {RequestMethod.GET})
	public IsochroneResponse loop(@ParameterObject RoutingParameters params, BindingResult bindingResult) {
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
	
	@Operation(
		summary = "Navigation information",
		description = "Returns navigation information for the road network within the given bounding box, "
				+ "such as turn restrictions and speed limits.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The navigation information for the requested bounding box.")
	})
	@RequestMapping(value = "/navInfo", method = {RequestMethod.GET})
	public NavInfoResponse navInfo(@ParameterObject NavInfoParameters params, BindingResult bindingResult) {
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

	@Operation(
		summary = "System status",
		description = "Returns the current status of the routing service, including its version "
				+ "and the status of its datasets.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The current system status.")
	})
	@RequestMapping(value = "/status", method = {RequestMethod.GET})
	public SystemStatus status() {
		return router.getStatus();
	}

	@Operation(
		summary = "Status messages by type",
		description = "Returns the status messages of the given type.",
		tags = {"Route"}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "The status messages of the requested type.")
	})
	@RequestMapping(value = "/status/{type}", method = {RequestMethod.GET})
	public List<StatusMessage> statusByType(
			@Parameter(description = "The type of status messages to return.", required = true,
					example = "RDM")
			@PathVariable StatusMessage.Type type) {
		return router.getMessages(type);
	}
}