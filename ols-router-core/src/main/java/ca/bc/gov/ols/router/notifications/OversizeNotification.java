/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.notifications;


public class OversizeNotification implements Notification {
	
	@Override
	public String getType() {
		return "Oversize";
	}

	@Override
	public String getMessage() {
		return "The vehicle dimensions specified are larger than standard and would require an oversize permit.";
	}

}
