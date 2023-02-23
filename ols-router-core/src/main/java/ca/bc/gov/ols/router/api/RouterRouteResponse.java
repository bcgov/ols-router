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
	private List<Integer> tlids;

	public RouterRouteResponse(RoutingParameters params) {
		super(params);
	}

	public RouterRouteResponse(RoutingParameters params, double distance, double time, LineString path, List<Partition> partitions, List<Integer> tlids) {
		super(params, distance, time);
		this.path = path;
		this.partitions = partitions;
		this.tlids = tlids;
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

	@Override
	public void reproject(GeometryReprojector gr) {
		super.reproject(gr);
		path = gr.reproject(path, getSrsCode());
	}

}
