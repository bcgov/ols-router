/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import ca.bc.gov.ols.router.data.vis.VisFeature;

@Schema(description = "A response containing navigation information for the road network within a bounding box.")
public class NavInfoResponse {

	private final int srsCode;
	private final List<VisFeature> geoms;
	
	public NavInfoResponse(NavInfoParameters params, List<VisFeature> geoms) {
		srsCode = params.getOutputSRS();
		this.geoms = geoms;
	}

	public int getSrsCode() {
		return srsCode;
	}

	public List<VisFeature> getMapGeoms() {
		return geoms;
	}

}
