/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;

import org.locationtech.jts.geom.Point;

public class StreetIntersection {
	
	private final int id;
	private final Point location;
	
	public StreetIntersection(int id, Point location) {
		this.id = id;
		this.location = location;
	}
	
	public int getId() {
		return id;
	}
	
	public Point getLocation() {
		return location;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof StreetIntersection && this.id == ((StreetIntersection)obj).id;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
}
