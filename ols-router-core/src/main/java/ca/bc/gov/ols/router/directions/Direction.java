/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.directions;

import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.notifications.Notification;

public abstract class Direction {
	protected Point point;
	protected Set<Notification> notifications;
	
	public Direction(Point point) {
		this.point = point;
	}

	public Point getPoint() {
		return point;
	}
	
	public void setPoint(Point point) {
		this.point = point;
	}

	public Set<Notification> getNotifications() {
		return notifications;
	}
	
	public void addNotification(Notification notification) {
		if(notifications == null) {
			notifications = new HashSet<Notification>(1);
		}
		notifications.add(notification);
	}
	
	abstract public String getType();
	
	abstract public String format(ApiResponse response);
}
