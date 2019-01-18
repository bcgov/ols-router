/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.time;

import java.time.DayOfWeek;
import java.util.Set;

public class WeeklyDateRange {

	private final Set<DayOfWeek> daySet;
	private final DateInterval dateRange;

	public WeeklyDateRange(Set<DayOfWeek> daySet, DateInterval dateRange) {
		if(daySet == null || dateRange == null) {
			throw new IllegalArgumentException("Invalid arguments to create WeeklyDateRange; niether dayCode nor dateRange can be null");
		}
		this.daySet = daySet;
		this.dateRange = dateRange;
	}

	public Set<DayOfWeek> getDaySet() {
		return daySet;
	}

	public DateInterval getDateRange() {
		return dateRange;
	}

}
