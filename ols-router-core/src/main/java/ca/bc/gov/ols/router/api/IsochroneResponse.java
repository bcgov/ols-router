/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

public class IsochroneResponse extends ApiResponse {
	private List<Geometry> polygons;
	private int zoneSize = 0;

	public IsochroneResponse(List<Geometry> polygons, RoutingParameters params) {
		super(params);
		this.polygons = polygons;
		this.zoneSize = params.getZoneSize();
	}

	public List<Geometry> getPolygons() {
		return polygons;
	}

	public int getZoneSize() {
		return zoneSize;
	}

}
