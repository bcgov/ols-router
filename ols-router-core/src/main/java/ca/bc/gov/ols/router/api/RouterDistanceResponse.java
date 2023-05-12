/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.data.enums.DistanceUnit;
import ca.bc.gov.ols.router.engine.basic.BasicGraph;
import ca.bc.gov.ols.rowreader.DateType;

public class RouterDistanceResponse extends ApiResponse {

	protected List<Point> points;
	protected double distance;
	protected double time;

	public RouterDistanceResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates) {
		super(params, dates);
		points = params.getPoints();
		distance = -1;
		time = -1;
	}

	public RouterDistanceResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates, double distance, double time) {
		super(params, dates);
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
