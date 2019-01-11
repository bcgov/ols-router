/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.api;

import com.vividsolutions.jts.geom.Geometry;

public interface GeometryReprojector {
	
	<T extends Geometry> T reproject(T geom, int toSRSCode);
	
}
