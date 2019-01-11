/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.LineString;

import ca.bc.gov.app.router.data.enumTypes.DividerType;
import ca.bc.gov.app.router.data.enumTypes.RoadClass;
import ca.bc.gov.app.router.data.enumTypes.SurfaceType;
import ca.bc.gov.app.router.data.enumTypes.TrafficImpactor;
import ca.bc.gov.app.router.data.enumTypes.TravelDirection;

public class StreetSegment {
	private static final Logger logger = LoggerFactory.getLogger(StreetSegment.class.getCanonicalName());
	
	protected final int segmentId;
	protected final LineString centerLine;
	protected int startIntersectionId;
	protected int endIntersectionId;
	protected final String name;
	protected final RoadClass roadClass;
	protected final TravelDirection travelDirection;
	protected final DividerType dividerType;
	protected final TrafficImpactor startTrafficImpactor;
	protected final TrafficImpactor endTrafficImpactor;
	protected final short speedLimit;
	protected final SurfaceType surfaceType;
	protected final double maxHeight;
	protected final double maxWidth;
	protected final Integer maxWeight;
	protected final String highwayRoute1;
	protected final String highwayRoute2;
	protected final String highwayRoute3;
	protected final boolean isTruckRoute;
	protected boolean isDeadEnded;
//	private final LaneRestriction laneRestriction;
//	private final AccessRestriction accessRestriction;
	
	public StreetSegment(int segmentId, LineString centerLine, 
			int startIntersectionId, int endIntersectionId, String name, RoadClass roadClass,
			TravelDirection travelDirection, DividerType dividerType, 
			TrafficImpactor startTrafficImpactor, TrafficImpactor endTrafficImpactor, 
			short speedLimit, SurfaceType surfaceType,
			double maxHeight, double maxWidth, Integer maxWeight, boolean isTruckRoute,
			String highwayRoute1, String highwayRoute2, String highwayRoute3,
			boolean isDeadEnded
//			LaneRestriction laneRestriction, AccessRestriction accessRestriction 
			) {
		this.segmentId = segmentId;
		this.centerLine = centerLine;
		this.startIntersectionId = startIntersectionId;
		this.endIntersectionId = endIntersectionId;
		this.name = name;
		this.roadClass = roadClass;
		this.travelDirection = travelDirection;
		this.dividerType = dividerType;
		this.startTrafficImpactor = startTrafficImpactor;
		this.endTrafficImpactor = endTrafficImpactor;
		this.speedLimit = speedLimit;
		this.surfaceType = surfaceType;
		this.maxHeight = maxHeight;    
		this.maxWidth = maxWidth;    
		this.maxWeight = maxWeight; 
		this.highwayRoute1 = highwayRoute1;
		this.highwayRoute2 = highwayRoute2;
		this.highwayRoute3 = highwayRoute3;
		this.isTruckRoute = isTruckRoute;
		this.isDeadEnded = isDeadEnded;
//		this.laneRestriction = laneRestriction;
//		this.accessRestriction = accessRestriction;
	}
		
	public int getSegmentId() {
		return segmentId;
	}
	
	public LineString getCenterLine() {
		return centerLine;
	}

	public int getStartIntersectionId() {
		return startIntersectionId;
	}

	public void setStartIntersectionId(int startIntersectionId) {
		this.startIntersectionId = startIntersectionId;
	}

	public int getEndIntersectionId() {
		return endIntersectionId;
	}

	public void setEndIntersectionId(int endIntersectionId) {
		this.endIntersectionId = endIntersectionId;
	}

	public String getName() {
		return name;
	}
	
	public RoadClass getRoadClass() {
		return roadClass;
	}
	
	public boolean isOneWay() {
		return travelDirection.isOneWay();
	}
	
	public TravelDirection getTravelDirection() {
		return travelDirection;
	}
	
	public DividerType getDividerType() {
		return dividerType;
	}

	public TrafficImpactor getStartTrafficImpactor() {
		return startTrafficImpactor;
	}

	public TrafficImpactor getEndTrafficImpactor() {
		return endTrafficImpactor;
	}

	public short getSpeedLimit() {
		return speedLimit;
	}

	public SurfaceType getSurfaceType() {
		return surfaceType;
	}

	public double getMaxHeight() {
		return maxHeight;
	}

	public double getMaxWidth() {
		return maxWidth;
	}

	public Integer getMaxWeight() {
		return maxWeight;
	}

	public String getHighwayRoute1() {
		return highwayRoute1;
	}

	public String getHighwayRoute2() {
		return highwayRoute2;
	}

	public String getHighwayRoute3() {
		return highwayRoute3;
	}

	public boolean isTruckRoute() {
		return isTruckRoute;
	}

	public boolean isDeadEnded() {
		return isDeadEnded;
	}

//	public LaneRestriction getLaneRestriction() {
//	return laneRestriction;
//}
//
//public AccessRestriction getAccessRestriction() {
//	return accessRestriction;
//}

	public boolean isFerry() {
		return roadClass.equals(RoadClass.FERRY);
	}

}
