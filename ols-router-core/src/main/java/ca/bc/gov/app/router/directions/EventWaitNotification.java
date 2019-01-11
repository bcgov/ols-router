/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.directions;

import ca.bc.gov.app.router.util.TimeHelper;


public class EventWaitNotification implements Notification {
	private int time = 0;

	public EventWaitNotification(int time) {
		this.time = time;
	}
	
	@Override
	public String getType() {
		return "EventWait";
	}

	@Override
	public String getMessage() {
		return "Wait for " + TimeHelper.formatTime(time) + " due to delays";
	}

}
