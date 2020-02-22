/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

public class FerryInfo {

	private final int minWaitTime;
	private final int travelTime;
	private boolean isScheduled;
	
	public FerryInfo(int minWaitTime, int travelTime, boolean isScheduled) {
		this.minWaitTime = minWaitTime;
		this.travelTime = travelTime;
		this.isScheduled = isScheduled;
	}

	public int getMinWaitTime() {
		return minWaitTime;
	}

	public int getTravelTime() {
		return travelTime;
	}

	public boolean isScheduled() {
		return isScheduled;
	}


}
