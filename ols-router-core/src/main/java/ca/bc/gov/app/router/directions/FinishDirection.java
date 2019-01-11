/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.directions;

import com.vividsolutions.jts.geom.Point;

import ca.bc.gov.app.router.api.ApiResponse;

public class FinishDirection extends Direction {

	public FinishDirection(Point point) {
		super(point);
	}

	@Override
	public String getType() {
		return "FINISH";
	}

	@Override
	public String format(ApiResponse response) {
		return "Finish!";
	}

}
