/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.directions;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.ApiResponse;

public class StopoverDirection extends Direction {
	private final int stopNum;
	
	public StopoverDirection(Point point, int stopNum) {
		super(point);
		this.stopNum = stopNum;
	}

	@Override
	public String getType() {
		return "STOPOVER";
	}

	@Override
	public String format(ApiResponse response) {
		return "Stopover " + stopNum;
	}
	
}
