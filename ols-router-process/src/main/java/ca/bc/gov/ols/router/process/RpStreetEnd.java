/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.process;

import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.router.data.WeeklyTimeRange;
import ca.bc.gov.ols.router.data.enums.End;
import ca.bc.gov.ols.router.data.enums.TrafficImpactor;
import ca.bc.gov.ols.router.data.enums.TurnTimeCode;
import ca.bc.gov.ols.router.data.enums.XingClass;

public class RpStreetEnd implements Comparable<RpStreetEnd> {
	
	private final RpStreetSegment segment;
	private final int angle;
	private final End end;


	public RpStreetEnd(RpStreetSegment segment, int angle, End end) {
		this.segment = segment;
		this.angle = angle;
		this.end = end;
	}

	public RpStreetSegment getSegment() {
		return segment;
	}

	public int getAngle() {
		return angle;
	}

	public End getEnd() {
		return end;
	}
	
	public int getIntersectionId() {
		if(End.FROM.equals(end)) {
			return segment.getStartIntersectionId();
		}
		return segment.getEndIntersectionId();
	}

	public TrafficImpactor getTrafficImpactor() {
		if(end == End.FROM) {
			return segment.getStartTrafficImpactor();
		} 
		return segment.getEndTrafficImpactor();
	}

	public TravelDirection getTravelDir() {
		if(end == End.FROM) {
			return segment.getTravelDirection();
		} 
		return segment.getTravelDirection().flip();
	}

	public void setXingClass(XingClass xingClass) {
		segment.setXingClass(end, xingClass);
	}

	public TurnTimeCode getLeftTR() {
		return segment.getLeftTR(end);
	}

	public WeeklyTimeRange getLeftTRTimeRange() {
		if(segment.getLeftTR(end) != null) {
			return segment.getLeftTR(end).getTimeRange();
		}
		return null;
	}

	public TurnTimeCode getCentreTR() {
		return segment.getCentreTR(end);
	}

	public WeeklyTimeRange getCentreTRTimeRange() {
		if(segment.getCentreTR(end) != null) {
			return segment.getCentreTR(end).getTimeRange();
		}
		return null;
	}

	public TurnTimeCode getRightTR() {
		return segment.getRightTR(end);
	}

	public WeeklyTimeRange getRightTRTimeRange() {
		if(segment.getRightTR(end) != null) {
			return segment.getRightTR(end).getTimeRange();
		}
		return null;
	}

	public void clearLeftTR() {
		if(segment.getLeftTR(end) != null) {
			RouterProcess.droppedTRs++;
			segment.clearLeftTR(end);
		}
	}
	
	public void clearCentreTR() {
		if(segment.getCentreTR(end) != null) {
			RouterProcess.droppedTRs++;
			segment.clearCentreTR(end);
		}
	}
	
	public void clearRightTR() {
		if(segment.getRightTR(end) != null) {
			RouterProcess.droppedTRs++;
			segment.clearRightTR(end);
		}
	}

	public RpStreetEnd getOtherEnd() {
		if(End.FROM.equals(end)) {
			return segment.getEndEnd();
		}
		return segment.getStartEnd();
	}
	
	@Override
	public int compareTo(RpStreetEnd otherEnd) {
		return angle - otherEnd.angle;
	}

	@Override
	public String toString() {
		return "Stub:" + angle;
	}
}
