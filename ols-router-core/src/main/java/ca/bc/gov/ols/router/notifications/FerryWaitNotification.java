/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.notifications;

import ca.bc.gov.ols.router.util.TimeHelper;

public class FerryWaitNotification implements Notification {
	private final String streetName;
	private int time = 0;
	
	public FerryWaitNotification(String streetName, int time) {
		this.streetName = streetName;
		this.time = time;
	}

	@Override
	public String getType() {
		return "FerryWait";
	}

	@Override
	public String getMessage() {
		return "Wait for " + streetName + " for " + TimeHelper.formatTime(time);
	}

}
