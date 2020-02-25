/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.util.List;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.enums.DistanceUnit;

public class RouterDistanceBetweenPairsResponse extends ApiResponse {

	protected List<Point> fromPoints;
	protected List<Point> toPoints;
	private int maxPairs;
	protected double[] distance;
	protected double[] time;
	protected String[] errors;
	private int curResult = 0;

	public RouterDistanceBetweenPairsResponse(RoutingParameters params) {
		super(params);
		fromPoints = params.getFromPoints();
		toPoints = params.getToPoints();
		maxPairs = params.getMaxPairs();
		int numResults = fromPoints.size() * toPoints.size();
		distance = new double[numResults];
		time = new double[numResults];
		errors = new String[numResults];
	}

	public void addResult(double distance, double time) {
		if(distance < 0 || time < 0) {
			errors[curResult] = "No Route Found.";
		} else {
			errors[curResult] = null;
		}
		this.distance[curResult] = DistanceUnit.METRE.convertTo(distance, distanceUnit);
		this.time[curResult] = time;
		curResult++;
	}

	public void addResult(String error) {
		errors[curResult] = error;
		distance[curResult] = RouterConfig.ERROR_DISTANCE;
		time[curResult] = RouterConfig.ERROR_TIME;
		curResult++;
	}
	
	public List<Point> getFromPoints() {
		return fromPoints;
	}

	public void setFromPoints(List<Point> fromPoints) {
		this.fromPoints = fromPoints;
	}

	public List<Point> getToPoints() {
		return toPoints;
	}

	public void setToPoints(List<Point> toPoints) {
		this.toPoints = toPoints;
	}

	public int getMaxPairs() {
		return maxPairs;
	}

	public void setMaxPairs(int maxPairs) {
		this.maxPairs = maxPairs;
	}

	public String getDistanceStr(int result) {
		return distanceUnit.formatForDisplay(distance[result]);
	}

	public double getTime(int result) {
		return time[result];
	}

	public String getError(int result) {
		return errors[result];
	}

	@Override
	public void reproject(GeometryReprojector gr) {
		super.reproject(gr);
		fromPoints = reprojectPoints(fromPoints, gr);
		toPoints = reprojectPoints(toPoints, gr);
	}

}
