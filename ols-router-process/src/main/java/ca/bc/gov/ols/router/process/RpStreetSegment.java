/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.process;

import java.util.EnumMap;

import ca.bc.gov.ols.enums.DividerType;
import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.enums.End;
import ca.bc.gov.ols.router.data.enums.SurfaceType;
import ca.bc.gov.ols.router.data.enums.TrafficImpactor;
import ca.bc.gov.ols.router.data.enums.TurnTimeCode;
import ca.bc.gov.ols.router.data.enums.XingClass;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

public class RpStreetSegment extends StreetSegment {

	private final boolean virtual;
	private EnumMap<End,TurnTimeCode> leftTR;
	private EnumMap<End,TurnTimeCode> centreTR;
	private EnumMap<End,TurnTimeCode> rightTR;
	
	public RpStreetSegment(int segmentId, LineString centerLine, 
			int startIntersectionId, int endIntersectionId, 
			String leftLocality, String rightLocality, String name, RoadClass roadClass,
			TravelDirection travelDirection, DividerType dividerType, 
			TrafficImpactor startTrafficImpactor, TrafficImpactor endTrafficImpactor, 
			short speedLimit, SurfaceType surfaceType, 
			double maxHeight, double maxWidth, 
			Integer fromMaxWeight, Integer toMaxWeight, 
			boolean isTruckRoute, 
			String highwayRoute1, String highwayRoute2, String highwayRoute3,
			boolean isDeadEnded, boolean virtual, 
			TurnTimeCode fromLeftTR, TurnTimeCode fromCentreTR, TurnTimeCode fromRightTR, 
			TurnTimeCode toLeftTR, TurnTimeCode toCentreTR, TurnTimeCode toRightTR) {
		super(segmentId, centerLine, 
			startIntersectionId, endIntersectionId, 
			leftLocality, rightLocality, name, roadClass,
			travelDirection, dividerType, 
			startTrafficImpactor, endTrafficImpactor, 
			speedLimit, surfaceType,
			maxHeight, maxWidth, 
			fromMaxWeight, toMaxWeight,
			isTruckRoute, 
			highwayRoute1, highwayRoute2, highwayRoute3,
			XingClass.SAME, XingClass.SAME, isDeadEnded);
		
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
	
	public void setXingClass(End end, XingClass xingClass) {
		switch(end) {
		case FROM:
			this.startXingClass = xingClass;
			break;
		case TO:
			this.endXingClass = xingClass;
			break;
		}
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
