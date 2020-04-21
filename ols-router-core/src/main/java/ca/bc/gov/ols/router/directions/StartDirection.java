/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.directions;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.util.TimeHelper;

public class StartDirection extends AbstractTravelDirection {
	private final CardinalDirection heading;
	
	public StartDirection(Point point, String streetName, CardinalDirection heading) {
		super(point, streetName);
		this.heading = heading;
	}

	@Override
	public String getType() {
		return "START";
	}
	
	public CardinalDirection getHeading() {
		return heading;
	}

	@Override
	public String format(ApiResponse response) {
		String distStr = response.getDistanceUnit().formatForDirections(distance);
		String str = "Head " + heading.toString().toLowerCase() + " on " + streetName + (distStr.isEmpty() ? "" : (" for " + distStr))
				+ (time == 0 ? "" : (" (" + TimeHelper.formatTime(time) + ")"));
		return str;
	}

}
