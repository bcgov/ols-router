/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.enums.NavInfoType;

public class NavInfoParameters {

	private int outputSRS = 4326;
	private double[] bbox;
	private Envelope envelope;
	private Instant departure = Instant.now();
	private Set<NavInfoType> types = EnumSet.allOf(NavInfoType.class);
	
	public int getOutputSRS() {
		return outputSRS;
	}
	
	public void setOutputSRS(int outputSRS) {
		this.outputSRS = outputSRS;
	}

	public void setBbox(double[] bbox) {
		this.bbox = bbox;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public Instant getDeparture() {
		return departure;
	}

	public void setDeparture(Instant departure) {
		if(departure != null) { 
			this.departure = departure;
		}
	}

	public Set<NavInfoType> getTypes() {
		return types;
	}

	public void setTypes(Set<NavInfoType> types) {
		this.types = types;
	}

	public void resolve(RouterConfig config, GeometryFactory gf, GeometryReprojector gr) {
		if(bbox != null && bbox.length == 4) {
			Point p1 = gr.reproject(gf.createPoint(new Coordinate(bbox[0], bbox[1])), config.getBaseSrsCode());
			Point p2 = gr.reproject(gf.createPoint(new Coordinate(bbox[2], bbox[3])), config.getBaseSrsCode());
			envelope = new Envelope(p1.getCoordinate(), p2.getCoordinate());
		}
	}

}
