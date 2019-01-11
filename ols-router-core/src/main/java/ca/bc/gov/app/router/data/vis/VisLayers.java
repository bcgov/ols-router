/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.data.vis;

import java.util.EnumMap;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

import ca.bc.gov.app.router.data.enumTypes.NavInfoType;

public class VisLayers {

	private EnumMap<NavInfoType, VisLayer> layers;
	
	public VisLayers() {
		layers = new EnumMap<NavInfoType, VisLayer>(NavInfoType.class);
		for(NavInfoType type : NavInfoType.values()) {
			layers.put(type, new VisLayer());
		}
	}

	public void buildIndexes() {
		for(VisLayer layer : layers.values()) {
			layer.build();
		}
	}

	
	public void addFeature(VisFeature mapGeom) {
		layers.get(mapGeom.getType()).addMapGeom(mapGeom);
	}

	public 	List<VisFeature> featuresWithin(NavInfoType type, Envelope env) {
		return layers.get(type).within(env);
	}
	
}
