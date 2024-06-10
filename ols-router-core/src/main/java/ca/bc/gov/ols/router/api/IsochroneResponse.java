/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

import ca.bc.gov.ols.rowreader.DateType;

public class IsochroneResponse extends ApiResponse {
	private List<Geometry> polygons;
	private int zoneSize = 0;

	public IsochroneResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates, List<Geometry> polygons) {
		super(params, dates);
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
