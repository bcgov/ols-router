/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.data.vis;

import ca.bc.gov.app.router.data.enumTypes.NavInfoType;

import com.vividsolutions.jts.geom.Geometry;

public class VisFeature {
	private Geometry geom;
	private final NavInfoType type;
	private final String subType;
	private final String detail;
	private final int angle;
	
	public VisFeature(Geometry geom, NavInfoType type) {
		this.geom = geom;
		this.type = type;
		this.subType = "";
		this.detail = "";
		this.angle = 0;
	}

	public VisFeature(Geometry geom, NavInfoType type, String detail) {
		this.geom = geom;
		this.type = type;
		this.subType = null;
		this.detail = detail;
		this.angle = 0;
	}

	public VisFeature(Geometry geom, NavInfoType type, String subType, String detail, int angle) {
		this.geom = geom;
		this.type = type;
		this.subType = subType;
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

	public String getDetail() {
		return detail;
	}

	public int getAngle() {
		return angle;
	}

}
