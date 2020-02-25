/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.util.List;

import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.directions.Partition;

public class RouterRouteResponse extends RouterDistanceResponse {

	private LineString path;
	private List<Partition> partitions;

	public RouterRouteResponse(RoutingParameters params) {
		super(params);
	}

	public RouterRouteResponse(RoutingParameters params, double distance, double time, LineString path, List<Partition> partitions) {
		super(params, distance, time);
		this.path = path;
		this.partitions = partitions;
	}

	public LineString getPath() {
		return path;
	}
	
	public List<Partition> getPartitions() {
		return partitions;
	}

	@Override
	public void reproject(GeometryReprojector gr) {
		super.reproject(gr);
		path = gr.reproject(path, getSrsCode());
	}

}
