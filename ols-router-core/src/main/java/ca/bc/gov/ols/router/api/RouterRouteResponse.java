/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import com.vividsolutions.jts.geom.LineString;

public class RouterRouteResponse extends RouterDistanceResponse {

	private LineString path;

	public RouterRouteResponse(RoutingParameters params) {
		super(params);
	}

	public RouterRouteResponse(RoutingParameters params, double distance, double time, LineString path) {
		super(params, distance, time);
		this.path = path;
	}

	public LineString getPath() {
		return path;
	}

	@Override
	public void reproject(GeometryReprojector gr) {
		super.reproject(gr);
		path = gr.reproject(path, getSrsCode());
	}

}
