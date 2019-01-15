/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.process;

import java.util.EnumMap;

import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.enumTypes.DividerType;
import ca.bc.gov.ols.router.data.enumTypes.End;
import ca.bc.gov.ols.router.data.enumTypes.RoadClass;
import ca.bc.gov.ols.router.data.enumTypes.SurfaceType;
import ca.bc.gov.ols.router.data.enumTypes.TrafficImpactor;
import ca.bc.gov.ols.router.data.enumTypes.TravelDirection;
import ca.bc.gov.ols.router.data.enumTypes.TurnTimeCode;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class RpStreetSegment extends StreetSegment {

	private final boolean virtual;
	private EnumMap<End,TurnTimeCode> leftTR;
	private EnumMap<End,TurnTimeCode> centreTR;
	private EnumMap<End,TurnTimeCode> rightTR;
	
	public RpStreetSegment(int segmentId, LineString centerLine, 
			int startIntersectionId, int endIntersectionId, String name, RoadClass roadClass,
			TravelDirection travelDirection, DividerType dividerType, 
			TrafficImpactor startTrafficImpactor, TrafficImpactor endTrafficImpactor, 
			short speedLimit, SurfaceType surfaceType, 
			double maxHeight, double maxWidth, Integer maxWeight, boolean isTruckRoute,
			String highwayRoute1, String highwayRoute2, String highwayRoute3,
			boolean isDeadEnded, boolean virtual, 
			TurnTimeCode fromLeftTR, TurnTimeCode fromCentreTR, TurnTimeCode fromRightTR, 
			TurnTimeCode toLeftTR, TurnTimeCode toCentreTR, TurnTimeCode toRightTR) {
		super(segmentId, centerLine, 
			startIntersectionId, endIntersectionId, name, roadClass,
			travelDirection, dividerType, 
			startTrafficImpactor, endTrafficImpactor, 
			speedLimit, surfaceType,
			maxHeight, maxWidth, maxWeight, isTruckRoute, 
			highwayRoute1, highwayRoute2, highwayRoute3,
			isDeadEnded);
		
		this.virtual = virtual;
		leftTR = new EnumMap<End,TurnTimeCode>(End.class);
		centreTR = new EnumMap<End,TurnTimeCode>(End.class);
		rightTR = new EnumMap<End,TurnTimeCode>(End.class);
		leftTR.put(End.FROM, fromLeftTR);
		centreTR.put(End.FROM, fromCentreTR);
		rightTR.put(End.FROM, fromRightTR);
		leftTR.put(End.TO, toLeftTR);
		centreTR.put(End.TO, toCentreTR);
		rightTR.put(End.TO, toRightTR);
	}
	
	public RpStreetEnd getStartEnd() {
		return new RpStreetEnd(this, calcAngle(centerLine.getCoordinateN(0), 
				centerLine.getCoordinateN(1)), End.FROM);
	}

	public RpStreetEnd getEndEnd() {
		int lastCoord = centerLine.getNumPoints() - 1;
		return new RpStreetEnd(this, calcAngle(centerLine.getCoordinateN(lastCoord), 
						centerLine.getCoordinateN(lastCoord - 1)), End.TO);
	}

	/*
	 * Angle is measured counterclockwise from directly west (3 o'clock) = 0
	 */
	private int calcAngle(Coordinate c1, Coordinate c2) {
		return (int) Math.round(Angle.toDegrees(Angle.normalizePositive(Angle.angle(c1, c2))));
	}
	
	public void setIsDeadEnded() {
		isDeadEnded = true;
	}
	
	public boolean isVirtual() {
		return virtual;
	}

	public TurnTimeCode getLeftTR(End end) {
		return leftTR.get(end);
	}

	public TurnTimeCode getCentreTR(End end) {
		return centreTR.get(end);
	}

	public TurnTimeCode getRightTR(End end) {
		return rightTR.get(end);
	}

	public void clearLeftTR(End end) {
		leftTR.put(end, null);
	}

	public void clearCentreTR(End end) {
		centreTR.put(end, null);
	}

	public void clearRightTR(End end) {
		rightTR.put(end, null);
	}

}
