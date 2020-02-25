/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.util.List;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.data.enums.DistanceUnit;

public class RouterDistanceResponse extends ApiResponse {

	protected List<Point> points;
	protected double distance;
	protected double time;

	public RouterDistanceResponse(RoutingParameters params) {
		super(params);
		points = params.getPoints();
		distance = -1;
		time = -1;
	}

	public RouterDistanceResponse(RoutingParameters params, double distance, double time) {
		super(params);
		points = params.getPoints();
		this.distance = DistanceUnit.METRE.convertTo(distance, params.getDistanceUnit());
		this.time = time;
	}

	public List<Point> getPoints() {
		return points;
	}

	public String getDistanceStr() {
		return distanceUnit.formatForDisplay(distance);
	}

	public double getTime() {
		return time;
	}

	public boolean isRouteFound() {
		return distance >= 0;
	}
	
	@Override
	public void reproject(GeometryReprojector gr) {
		super.reproject(gr);
		points = reprojectPoints(points, gr);
	}
	
}
