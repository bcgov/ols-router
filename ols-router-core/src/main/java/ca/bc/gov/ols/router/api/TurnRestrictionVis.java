/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LengthIndexedLine;

import ca.bc.gov.ols.router.data.WeeklyTimeRange;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.engine.basic.BasicGraph;

public class TurnRestrictionVis {
	private final WeeklyTimeRange restriction;
	private final TurnDirection direction;
	private final LineString fromFragment;
	private final LineString toFragment;
	private final int angle;
	
	public TurnRestrictionVis(BasicGraph graph, int[] ids, WeeklyTimeRange restriction) {
		this.restriction = restriction;
		LineString fromLine = graph.getLineString(ids[0]);
		LengthIndexedLine fromLil = new LengthIndexedLine(fromLine);
		if(graph.getReversed(ids[0])) {
			fromFragment = (LineString) fromLil.extractLine(10, 0);			
		} else {
			double end = fromLil.getEndIndex();
			fromFragment = (LineString) fromLil.extractLine(end-10, end);
		}
		LineString endFragment;
		LineString toLine = graph.getLineString(ids[ids.length-1]);
		LengthIndexedLine toLil = new LengthIndexedLine(toLine);
		if(!graph.getReversed(ids[ids.length-1])) {
			endFragment = (LineString) toLil.extractLine(0,10);
		} else {
			double end = toLil.getEndIndex();
			endFragment = (LineString) toLil.extractLine(end, end-10);
		}
		
		if(ids.length == 3) {
			toFragment = endFragment;
			double angleBetween = Angle.angleBetweenOriented(fromFragment.getCoordinateN(fromFragment.getNumPoints()-2), 
					toFragment.getCoordinateN(0), toFragment.getCoordinateN(1));
			if(Math.abs(angleBetween) > Math.PI * 3 / 4) {
				direction = TurnDirection.CENTER;
			} else if(angleBetween < 0) {
				direction = TurnDirection.LEFT;
			} else {
				direction = TurnDirection.RIGHT;
			}
		} else {
			// TODO include all of the internal u-turn segments in the toFragment
			toFragment = endFragment;
			direction = TurnDirection.UTURN;			
		}
		
		angle = (int) Math.round(Angle.toDegrees(Angle.normalizePositive(Angle.angle(
				fromFragment.getCoordinateN(fromFragment.getNumPoints()-2), 
				fromFragment.getCoordinateN(fromFragment.getNumPoints()-1)))));
	}

	public WeeklyTimeRange getRestriction() {
		return restriction;
	}

	public TurnDirection getDirection() {
		return direction;
	}

	public LineString getFromFragment() {
		return fromFragment;
	}

	public LineString getToFragment() {
		return toFragment;
	}

	public int getAngle() {
		return angle;
	}
	
	public String toString() {
		return direction.name().charAt(0) + ": " + (restriction.isAlways() ? "ALWAYS" : restriction.getDaySet() + " " + restriction.getTimeRangeString());
	}
	
}
