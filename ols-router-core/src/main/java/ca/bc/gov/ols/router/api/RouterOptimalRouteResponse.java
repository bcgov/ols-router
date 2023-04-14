/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.directions.Partition;
import ca.bc.gov.ols.router.engine.basic.BasicGraph;
import ca.bc.gov.ols.rowreader.DateType;

public class RouterOptimalRouteResponse extends RouterRouteResponse implements RouterOptimizedResponse {

	private long routingExecutionTime;
	private long optimizationExecutionTime;
	private int[] visitOrder;
	
	public RouterOptimalRouteResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates) {
		super(params, dates);
	}
	
	public RouterOptimalRouteResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates, double distance, double time, 
			LineString path, List<Partition> partitions, List<Integer> tlids, int[] visitOrder) {
		super(params, dates, distance, time, path, partitions, tlids);
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
