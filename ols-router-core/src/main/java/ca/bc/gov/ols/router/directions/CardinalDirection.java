/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.directions;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;

public enum CardinalDirection {
	NORTH,
	NORTHEAST,
	EAST,
	SOUTHEAST,
	SOUTH,
	SOUTHWEST,
	WEST,
	NORTHWEST;

	public static CardinalDirection getHeading(Coordinate c1, Coordinate c2) {
		double angle = Angle.angle(c1, c2);
		if(Math.abs(angle) > Math.PI * 7/8) {
			return WEST;
		} else if(angle < Math.PI * - 5/8) {
			return SOUTHWEST;
		} else if(angle < Math.PI * - 3/8) {
			return SOUTH;
		} else if(angle < Math.PI * - 1/8) {
			return SOUTHEAST;
		} else if(angle < Math.PI * 1/8) {
			return EAST;
		} else if(angle < Math.PI * 3/8) {
			return NORTHEAST;
		} else if(angle < Math.PI * 5/8) {
			return NORTH;
		} else {
			return NORTHWEST;
		}
	}

}
