/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.data.enums.DistanceUnit;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.data.enums.RoutingCriteria;
import ca.bc.gov.ols.router.engine.basic.Attribute;

public class ApiResponse {

	private LocalDateTime timeStamp;
	private long executionTime;
	private int srsCode;
	private String callback;
	
	protected DistanceUnit distanceUnit;
	protected RoutingCriteria criteria;
	protected Set<RouteOption> enabledOptions;
	protected String routeDescription;
	protected EnumSet<Attribute> partition;

	public ApiResponse(RoutingParameters params) {
		timeStamp = LocalDateTime.now();
		callback = params.getCallback();
		srsCode = params.getOutputSRS();
		distanceUnit = params.getDistanceUnit();
		criteria = params.getCriteria();
		enabledOptions = params.getEnabledOptions();
		routeDescription = params.getRouteDescription();
		partition = params.getPartition();
	}
	
	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(LocalDateTime timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public long getExecutionTime() {
		return executionTime;
	}
	
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	
	public int getSrsCode() {
		return srsCode;
	}
	
	public void setSrsCode(int srsCode) {
		this.srsCode = srsCode;
	}
	
	public String getCallback() {
		return callback;
	}
	
	public void setCallback(String callback) {
		this.callback = callback;
	}

	public RoutingCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(RoutingCriteria criteria) {
		this.criteria = criteria;
	}

	public Set<RouteOption> getEnabledOptions() {
		return enabledOptions;
	}
	
	public DistanceUnit getDistanceUnit() {
		return distanceUnit;
	}

	public String getRouteDescription() {
		return routeDescription;
	}

	public void setRouteDescription(String routeDescription) {
		this.routeDescription = routeDescription;
	}
	
	public EnumSet<Attribute> getPartition() {
		return partition;
	}

	public void reproject(GeometryReprojector gr) {
		// reproject is a no-op unless you have geometry data to reproject.
	}
	
	protected List<Point> reprojectPoints(List<Point> points, GeometryReprojector gr) {
		ArrayList<Point> reprojPoints = new ArrayList<Point>(points.size());
		for(Point p : points) {
			reprojPoints.add(gr.reproject(p, srsCode));
		}
		return reprojPoints;
	}

}
