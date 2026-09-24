/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.LineString;

import io.swagger.v3.oas.annotations.media.Schema;

import ca.bc.gov.ols.router.directions.Partition;
import ca.bc.gov.ols.rowreader.DateType;

@Schema(description = "A response containing the geometry, length, and duration of the route between the given points.")
public class RouterRouteResponse extends RouterDistanceResponse {

	private LineString path;
	private List<Partition> partitions;
	private List<Integer> tlids;
	private List<Integer> restrictions;
	
	public RouterRouteResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates) {
		super(params, dates);
	}

	public RouterRouteResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates, double distance, double time, LineString path, List<Partition> partitions, List<Integer> tlids, List<Integer> restrictions) {
		super(params, dates, distance, time);
		this.path = path;
		this.partitions = partitions;
		this.tlids = tlids;
		this.restrictions = restrictions;
	}

	public LineString getPath() {
		return path;
	}
	
	public List<Partition> getPartitions() {
		return partitions;
	}
	
	public List<Integer> getTlids() {
		return tlids;
	}
	
	public List<Integer> getRestrictions() {
		return restrictions;
	}

	@Override
	public void reproject(GeometryReprojector gr) {
		super.reproject(gr);
		path = gr.reproject(path, getSrsCode());
	}

}
