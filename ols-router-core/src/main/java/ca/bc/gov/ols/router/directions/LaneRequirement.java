package ca.bc.gov.ols.router.directions;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.enums.RestrictionType;
import ca.bc.gov.ols.router.restrictions.LaneBasedRestriction;

public class LaneRequirement {
	
	// private int restrictionId; // there will potentially be many of these, maybe locationId is better if we need it at all
	private RestrictionType type;
	private Point location;
	private int locationId;
	private double distance;
	private boolean[] safeLanes;
	
	public LaneRequirement(LaneBasedRestriction lbr, RoutingParameters params, LineString lineString, boolean reversed, double preDist) {
		this.type = lbr.type;
		this.location = lbr.getLocation();
		this.locationId = lbr.getLocationId();
		this.safeLanes = lbr.getSafeLanes(params);
		if(reversed) {
			this.distance = preDist + lineString.getLength() - lbr.dist;
		} else {
			this.distance = preDist + lbr.dist;
		}
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
