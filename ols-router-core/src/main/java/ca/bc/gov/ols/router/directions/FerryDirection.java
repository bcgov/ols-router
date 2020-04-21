/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.directions;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.util.TimeHelper;

public class FerryDirection extends AbstractTravelDirection {
	
	public FerryDirection(Point point, String streetName) {
		super(point, streetName);
	}

	@Override
	public String getType() {
		return "FERRY";
	}

	@Override
	public String format(ApiResponse response) {
		String distStr = response.getDistanceUnit().formatForDirections(distance);
		return "Board " + streetName + " and travel " + distStr + " (" + TimeHelper.formatTime(time) + ")";
	}

}
