/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.directions;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

import ca.bc.gov.ols.router.api.ApiResponse;

public abstract class Direction {
	protected Point point;
	protected List<Notification> notifications;
	
	public Direction(Point point) {
		this.point = point;
	}

	public Point getPoint() {
		return point;
	}
	
	public void setPoint(Point point) {
		this.point = point;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}
	
	public void addNotification(Notification notification) {
		if(notifications == null) {
			notifications = new ArrayList<Notification>(1);
		}
		notifications.add(notification);
	}
	
	abstract public String getType();
	
	abstract public String format(ApiResponse response);
}
