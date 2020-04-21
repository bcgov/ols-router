/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.util.List;

import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.directions.Partition;

public class RouterOptimalRouteResponse extends RouterRouteResponse implements RouterOptimizedResponse {

	private long routingExecutionTime;
	private long optimizationExecutionTime;
	private int[] visitOrder;
	
	public RouterOptimalRouteResponse(RoutingParameters params) {
		super(params);
	}
	
	public RouterOptimalRouteResponse(RoutingParameters params, double distance, double time, LineString path, 
			List<Partition> partitions, int[] visitOrder) {
		super(params, distance, time, path, partitions);
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
