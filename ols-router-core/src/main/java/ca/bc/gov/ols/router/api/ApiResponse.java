/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

import ca.bc.gov.ols.router.data.enumTypes.DistanceUnit;
import ca.bc.gov.ols.router.data.enumTypes.RoutingCriteria;

public class ApiResponse {

	private LocalDateTime timeStamp;
	private long executionTime;
	private int srsCode;
	private String callback;
	
	protected DistanceUnit distanceUnit;
	protected RoutingCriteria criteria;
	protected String routeDescription;

	public ApiResponse(RoutingParameters params) {
		timeStamp = LocalDateTime.now();
		callback = params.getCallback();
		srsCode = params.getOutputSRS();
		distanceUnit = params.getDistanceUnit();
		criteria = params.getCriteria();
		routeDescription = params.getRouteDescription();
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

	public DistanceUnit getDistanceUnit() {
		return distanceUnit;
	}

	public String getRouteDescription() {
		return routeDescription;
	}

	public void setRouteDescription(String routeDescription) {
		this.routeDescription = routeDescription;
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
