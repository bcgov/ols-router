/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.directions;

import ca.bc.gov.app.router.api.ApiResponse;
import ca.bc.gov.app.router.util.TimeHelper;

import com.vividsolutions.jts.geom.Point;

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
