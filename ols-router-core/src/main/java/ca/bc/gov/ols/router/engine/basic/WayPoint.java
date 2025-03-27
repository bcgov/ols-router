/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.util.List;

import org.locationtech.jts.geom.Point;

public record WayPoint ( 
		List<Integer> outgoingEdgeIds,
		List<Integer> incomingEdgeIds,
		Point point
) {}
