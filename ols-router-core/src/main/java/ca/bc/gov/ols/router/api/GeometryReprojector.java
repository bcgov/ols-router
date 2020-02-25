/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import org.locationtech.jts.geom.Geometry;

public interface GeometryReprojector {
	
	<T extends Geometry> T reproject(T geom, int toSRSCode);
	
}
