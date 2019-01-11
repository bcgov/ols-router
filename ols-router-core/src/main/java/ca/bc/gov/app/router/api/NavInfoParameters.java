/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.api;

import java.time.Instant;
import java.util.EnumSet;

import ca.bc.gov.app.router.RouterConfig;
import ca.bc.gov.app.router.data.enumTypes.NavInfoType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class NavInfoParameters {

	private int outputSRS = 4326;
	private double[] bbox;
	private Envelope envelope;
	private Instant departure = Instant.now();
	private EnumSet<NavInfoType> types = EnumSet.allOf(NavInfoType.class);
	
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

	public EnumSet<NavInfoType> getTypes() {
		return types;
	}

	public void setTypes(EnumSet<NavInfoType> types) {
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
