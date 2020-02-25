/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class SplitEdge {
	private final int[] edgeIds;
	private final Point point;
	private final LineString fromSplit;
	private final LineString toSplit;
	
	public SplitEdge(int[] edgeIds, Point point, LineString[] splitString) {
		this.edgeIds = edgeIds;
		this.point = point;
		fromSplit = splitString[0];
		toSplit = splitString[1];
	}

	public int[] getEdgeIds() {
		return edgeIds;
	}

	public Point getPoint() {
		return point;
	}
	
	public LineString getFromSplit() {
		return fromSplit;
	}

	public LineString getToSplit() {
		return toSplit;
	}

	public double getFromSplitLength() {
		 return fromSplit.getLength();
	}

	public double getToSplitLength() {
		 return toSplit.getLength();
	}

}
