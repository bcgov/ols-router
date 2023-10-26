/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.data.enums.DistanceUnit;
import ca.bc.gov.ols.router.directions.AbstractTravelDirection;
import ca.bc.gov.ols.router.directions.Direction;
import ca.bc.gov.ols.router.directions.LaneRequirement;
import ca.bc.gov.ols.router.directions.Partition;
import ca.bc.gov.ols.router.engine.basic.BasicGraph;
import ca.bc.gov.ols.router.notifications.Notification;
import ca.bc.gov.ols.rowreader.DateType;

public class RouterDirectionsResponse extends RouterRouteResponse {

	private List<Direction> directions;
	private Collection<Notification> notifications;
	
	public RouterDirectionsResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates) {
		super(params, dates);
		directions = Collections.emptyList();
	}
	
	public RouterDirectionsResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates, double distance, double time, LineString path, 
			List<Partition> partitions, List<Integer> tlids, List<Integer> restrictions, List<Direction> directions, Collection<Notification> notifications) {
		super(params, dates, distance, time, path, partitions, tlids, restrictions);
		this.directions = directions;
		this.notifications = notifications;
		for(Direction dir : directions) {
			if(dir instanceof AbstractTravelDirection) {
				((AbstractTravelDirection)dir).setDistance(DistanceUnit.METRE.convertTo(((AbstractTravelDirection)dir).getDistance(), params.getDistanceUnit()));
			}
			if(dir.getLaneRequirements() != null) {
				for(LaneRequirement lr : dir.getLaneRequirements()) {
					lr.setDistance(DistanceUnit.METRE.convertTo(lr.getDistance(), params.getDistanceUnit()));
				}
			}
		}
	}

	public List<Direction> getDirections() {
		return directions;
	}
	
	public Collection<Notification> getNotifications() {
		return notifications;
	}

	@Override
	public void reproject(GeometryReprojector gr) {
		super.reproject(gr);
		for(Direction dir : directions) {
			dir.setPoint(gr.reproject(dir.getPoint(),getSrsCode()));
			if(dir.getLaneRequirements() != null) {
				for(LaneRequirement lr: dir.getLaneRequirements() ) {
					lr.setLocation(gr.reproject(lr.getLocation(), getSrsCode()));
				}
			}
		}
	}
}
