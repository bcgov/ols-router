/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.directions;

import org.locationtech.jts.geom.Point;

public abstract class AbstractTravelDirection extends Direction {

	protected final String streetName;
	protected double distance = 0;
	protected double time = 0;

	public AbstractTravelDirection(Point point, String streetName) {
		super(point);
		this.streetName = streetName;
	}

	public AbstractTravelDirection(Point point, String streetName, double distance, double time) {
		super(point);
		this.streetName = streetName;
		this.distance = distance;
		this.time = time;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public void addDistance(double distance) {
		this.distance += distance;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void addTime(double time) {
		this.time += time;
	}

	public double getTime() {
		return time;
	}
	

}