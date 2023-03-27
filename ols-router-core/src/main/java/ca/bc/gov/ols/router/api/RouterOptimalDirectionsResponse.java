/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.directions.Direction;
import ca.bc.gov.ols.router.directions.Partition;
import ca.bc.gov.ols.router.notifications.Notification;


public class RouterOptimalDirectionsResponse extends RouterDirectionsResponse implements RouterOptimizedResponse{

	private long routingExecutionTime;
	private long optimizationExecutionTime;
	private int[] visitOrder;
	
	public RouterOptimalDirectionsResponse(RoutingParameters params) {
		super(params);
		visitOrder = new int[0];
	}
	
	public RouterOptimalDirectionsResponse(RoutingParameters params, double distance, double time, LineString path, 
			List<Partition> partitions, List<Direction> directions, Collection<Notification> notifications, int[] visitOrder) {
		super(params, distance, time, path, partitions, directions, notifications);
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
