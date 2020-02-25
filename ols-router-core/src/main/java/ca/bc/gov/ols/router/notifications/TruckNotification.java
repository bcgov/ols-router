/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.notifications;

import java.util.Objects;

import ca.bc.gov.ols.router.data.RoadTruckNoticeEvent;
import ca.bc.gov.ols.router.data.enums.TruckNoticeType;
import ca.bc.gov.ols.util.StringUtils;


public class TruckNotification implements Notification {
	private TruckNoticeType type = null;
	private String message;

	public TruckNotification(RoadTruckNoticeEvent evt) {
		type = evt.getType();
		message = evt.getNotice();
	}
	
	@Override
	public String getType() {
		return "Truck" + StringUtils.capitalizeFirst(type.toString());
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof TruckNotification
				&& type.equals(((TruckNotification)o).type)
				&& message.equals(((TruckNotification)o).message)) {
					return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(type, message);
	}
}
