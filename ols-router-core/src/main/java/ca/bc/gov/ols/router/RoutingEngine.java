/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router;

import java.util.List;

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
import ca.bc.gov.ols.router.datasource.DataUpdateManager;
import ca.bc.gov.ols.router.status.StatusMessage;
import ca.bc.gov.ols.router.status.StatusMessage.Type;
import ca.bc.gov.ols.router.status.SystemStatus;

public interface RoutingEngine {

	public abstract RouterDistanceResponse distance(RoutingParameters params);

	public abstract RouterRouteResponse route(RoutingParameters params);

	public abstract RouterDirectionsResponse directions(RoutingParameters params);

	public abstract RouterDistanceBetweenPairsResponse distanceBetweenPairs(
			RoutingParameters params);

	public abstract RouterOptimalRouteResponse optimalRoute(
			RoutingParameters params);

	public abstract RouterOptimalDirectionsResponse optimalDirections(
			RoutingParameters params);

	public abstract IsochroneResponse isochrones(RoutingParameters params);

	public abstract IsochroneResponse loop(RoutingParameters params);

	public abstract NavInfoResponse navInfo(NavInfoParameters params);

	public abstract RoutingEngine getUpdatedEngine(DataUpdateManager dum, SystemStatus status);

	public abstract List<StatusMessage> getMessages(Type type);

}