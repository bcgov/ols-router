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

import io.swagger.v3.oas.annotations.Parameter;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.enums.NavInfoType;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;

public class NavInfoParameters {

	@Parameter(description = "The EPSG code of the spatial reference system (SRS) to use for output geometries.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer", defaultValue = "4326"),
			example = "4326")
	private int outputSRS = 4326;
	@Parameter(description = "A bounding box (xmin,ymin,xmax,ymax) that limits the area of the request, "
			+ "in the format \"xmin,ymin,xmax,ymax\". Must be in the same SRS as the outputSRS parameter.",
			example = "-123.13,49.28,-123.11,49.29")
	private double[] bbox;
	@Parameter(hidden = true)
	private Envelope envelope;
	@Parameter(description = "The date and time of departure, used to evaluate time-dependent "
			+ "navigation information.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "date-time"))
	private Instant departure = Instant.now();
	@Parameter(description = "The source of the restriction data to use.")
	private RestrictionSource restrictionSource = null;
	@Parameter(description = "A comma-separated list of the types of navigation information to return.")
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

	public double[] getBbox() {
		return bbox;
	}

	@Parameter(hidden = true)
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

	public RestrictionSource getRestrictionSource() {
		return restrictionSource;
	}
	
	public void setRestrictionSource(RestrictionSource restrictionSource) {
		this.restrictionSource = restrictionSource;
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
