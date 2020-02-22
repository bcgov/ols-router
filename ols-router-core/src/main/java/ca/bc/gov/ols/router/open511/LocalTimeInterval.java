/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511;

import java.time.LocalTime;

public class LocalTimeInterval {
	private LocalTime startTime;
	private LocalTime endTime;
	
	public LocalTimeInterval(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public static LocalTimeInterval parse(String str) {
		String[] parts = str.split("-");
		return new LocalTimeInterval(LocalTime.parse(parts[0]), LocalTime.parse(parts[1]));
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}
	
}
