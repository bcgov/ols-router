/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Point;

public class RpStreetIntersection {

	private int id;
	private Point point;
	private List<RpStreetEnd> streetEnds = null;

	public RpStreetIntersection(int id, Point point) {
		this.id = id;
		this.point = point;
	}

	public int getId() {
		return id;
	}

	public Point getPoint() {
		return point;
	}

	public void addEnd(RpStreetEnd end) {
		if(streetEnds == null) {
			streetEnds = new ArrayList<RpStreetEnd>(4);
		}
		int i;
		for(i = 0; i < streetEnds.size() && streetEnds.get(i).getAngle() < end.getAngle(); i++);
		streetEnds.add(i, end);
	}

	public List<RpStreetEnd> getEnds() {
		return Collections.unmodifiableList(streetEnds);
	}
}
