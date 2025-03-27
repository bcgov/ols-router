package ca.bc.gov.ols.router.directions;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthLocationMap;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;

import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.enums.RestrictionType;
import ca.bc.gov.ols.router.restrictions.LaneBasedRestriction;

public class LaneRequirement {
	// restriction locations over this tolerance away from the segment will not be considered
	// specifically for handling lane restrictions on partial segments at start/end of route
	private static final double TOLERANCE = 10.0;
	
	// private int restrictionId; // there will potentially be many of these, maybe locationId is better if we need it at all
	private RestrictionType type;
	private Point location;
	private int locationId;
	private double distance;
	private boolean[] safeLanes;
	
	public static LaneRequirement createIfNeeded(LaneBasedRestriction lbr, RoutingParameters params, LineString lineString, boolean reversed, double preDist) {
		// in the case of the first/last seg, the linestring may be only a partial, 
		// and the restriction may not apply, in which case we return null
		if(!lineString.isWithinDistance(lbr.getLocation(), TOLERANCE)) return null;
		
		LaneRequirement lr = new LaneRequirement();
		lr.type = lbr.type;
		lr.location = lbr.getLocation();
		lr.locationId = lbr.getLocationId();
		lr.safeLanes = lbr.getSafeLanes(params);
		
		// calculate the distance along the segment to the restriction location
		LocationIndexedLine locationIndexedLine = new LocationIndexedLine(lineString);
	    LinearLocation ref = locationIndexedLine.project(lr.location.getCoordinate());
		double dist = new LengthLocationMap(lineString).getLength(ref);
	    
		if(reversed) {
			lr.distance = preDist + lineString.getLength() - dist;
		} else {
			lr.distance = preDist + dist;
		}
		return lr;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public int getLocationId() {
		return locationId;
	}

	public boolean[] getSafeLanes() {
		return safeLanes;
	}	

	public String format(ApiResponse response) {
		String distStr = response.getDistanceUnit().formatForDirections(distance);
		List<Integer> safeLaneList = new ArrayList<>(safeLanes.length);
		for(int i = 0; i < safeLanes.length; i++) {
			if(safeLanes[i]) safeLaneList.add(i+1); // lane numbers are offset by one from array indexes
		}
		String laneStr = "lane";
		if(safeLaneList.size() > 1) {
			laneStr = "lanes";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("At ").append(distStr).append(", due to the specifed vehicle ").append(type.visName)
				.append(", travel is permitted only in ").append(laneStr);
		for(int i = 0; i < safeLaneList.size(); i++) {
			if( i > 1 ) {
				// second or more of multiple
				if(i < safeLaneList.size() - 1) {
					sb.append(",");
				} else {
					// last lane number of multiple
					sb.append(" and");
				}
			}
			sb.append(" ").append(safeLaneList.get(i));
		}
		sb.append(".");
		return sb.toString();
	}

}
