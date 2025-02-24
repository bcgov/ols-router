/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import org.locationtech.jts.geom.Point;

public record WayPoint ( 
		int[] edgeIds,
		Point point
) {}
