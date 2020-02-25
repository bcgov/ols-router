/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.vis;

import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;

public class VisLayer {

	private STRtree spatialIndex;
	
	public VisLayer() {
		spatialIndex = new STRtree();
	}
	
	public void build() {
		spatialIndex.build();
	}
	
	public void addMapGeom(VisFeature mapGeom) {
		spatialIndex.insert(mapGeom.getGeometry().getEnvelopeInternal(), mapGeom);
	}
	
	@SuppressWarnings("unchecked")
	public List<VisFeature> within(Envelope env) {
		return spatialIndex.query(env);
	}

}
