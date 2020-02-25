/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.vis;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.data.enums.NavInfoType;

public class VisTurnRestriction extends VisFeature {

	private LineString fromFragment;
	private List<LineString> toFragments;

	public VisTurnRestriction(Geometry geom, NavInfoType type, String subType, String detail,
			int angle, LineString fromFragment, List<LineString> toFragments) {
		super(geom, type, subType, detail, angle);
		this.fromFragment = fromFragment;
		this.toFragments = toFragments;
	}

	public LineString getFromFragment() {
		return fromFragment;
	}

	public List<LineString> getToFragments() {
		return toFragments;
	}
	
}
