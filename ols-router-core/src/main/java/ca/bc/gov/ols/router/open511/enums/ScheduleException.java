/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511.enums;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.ols.router.open511.LocalTimeInterval;

public class ScheduleException {
	private LocalDate date;
	private List<LocalTimeInterval> times;
	
	public static ScheduleException parse(String str) {
		ScheduleException exc = new ScheduleException();
		String[] parts = str.split("\\s");
		LocalDate date = LocalDate.parse(parts[0]);
		List<LocalTimeInterval> times = new ArrayList<LocalTimeInterval>(parts.length - 1);
		for(int i = 1; i < parts.length; i++) {
			times.add(LocalTimeInterval.parse(parts[i]));
		}
		exc.setDate(date);
		exc.setTimes(times);
		return exc;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public List<LocalTimeInterval> getTimes() {
		return times;
	}

	public void setTimes(List<LocalTimeInterval> times) {
		this.times = times;
	}
	
}
