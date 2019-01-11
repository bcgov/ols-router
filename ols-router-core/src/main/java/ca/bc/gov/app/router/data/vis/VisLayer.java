/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.data.vis;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

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
