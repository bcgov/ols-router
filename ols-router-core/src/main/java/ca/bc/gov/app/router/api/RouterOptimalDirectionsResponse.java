/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.api;

import java.util.List;

import ca.bc.gov.app.router.directions.Direction;
import ca.bc.gov.app.router.directions.Notification;

import com.vividsolutions.jts.geom.LineString;


public class RouterOptimalDirectionsResponse extends RouterDirectionsResponse implements RouterOptimizedResponse{

	private long routingExecutionTime;
	private long optimizationExecutionTime;
	private int[] visitOrder;
	
	public RouterOptimalDirectionsResponse(RoutingParameters params) {
		super(params);
		visitOrder = new int[0];
	}
	
	public RouterOptimalDirectionsResponse(RoutingParameters params, double distance, double time, LineString path, 
			List<Direction> directions, List<Notification> notifications, int[] visitOrder) {
		super(params, distance, time, path, directions, notifications);
		this.visitOrder = visitOrder;
	}

	public void setRoutingExecutionTime(long routingExecutionTime) {
		this.routingExecutionTime = routingExecutionTime;
	}

	public long getRoutingExecutionTime() {
		return routingExecutionTime;
	}

	public void setOptimizationExecutionTime(long optimizationExecutionTime) {
		this.optimizationExecutionTime = optimizationExecutionTime;
	}

	public long getOptimizationExecutionTime() {
		return optimizationExecutionTime;
	}

	public int[] getVisitOrder() {
		return visitOrder;
	}


}
