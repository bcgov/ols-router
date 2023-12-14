/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.vis;

import org.locationtech.jts.geom.Geometry;

import ca.bc.gov.ols.router.data.enums.NavInfoType;

public class VisFeature {
	private Geometry geom;
	private final NavInfoType type;
	private final String subType;
	private final String source;
	private final String detail;
	private final int angle;
	
	public VisFeature(Geometry geom, NavInfoType type) {
		this.geom = geom;
		this.type = type;
		this.subType = "";
		this.source = "";
		this.detail = "";
		this.angle = 0;
	}

	public VisFeature(Geometry geom, NavInfoType type, String detail) {
		this.geom = geom;
		this.type = type;
		this.subType = null;
		this.source = "";
		this.detail = detail;
		this.angle = 0;
	}

	public VisFeature(Geometry geom, NavInfoType type, String subType, String detail) {
		this.geom = geom;
		this.type = type;
		this.subType = subType;
		this.source = "";
		this.detail = detail;
		this.angle = 0;
	}

	public VisFeature(Geometry geom, NavInfoType type, String subType, String source, String detail) {
		this.geom = geom;
		this.type = type;
		this.subType = subType;
		this.source = source;
		this.detail = detail;
		this.angle = 0;
	}

	public VisFeature(Geometry geom, NavInfoType type, String subType, String detail, int angle) {
		this.geom = geom;
		this.type = type;
		this.subType = subType;
		this.source = "";
		this.detail = detail;
		this.angle = angle;
	}

	public Geometry getGeometry() {
		return geom;
	}

	public void setGeometry(Geometry geom) {
		this.geom = geom;
	}
	
	public NavInfoType getType() {
		return type;
	}

	public String getSubType() {
		return subType;
	}

	public String getSource() {
		return source;
	}
	
	public String getDetail() {
		return detail;
	}

	public int getAngle() {
		return angle;
	}

}
