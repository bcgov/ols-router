/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.time;

import java.time.DayOfWeek;
import java.util.EnumSet;

public class WeeklyDateRange {

	private final EnumSet<DayOfWeek> daySet;
	private final DateInterval dateRange;

	public WeeklyDateRange(EnumSet<DayOfWeek> daySet, DateInterval dateRange) {
		if(daySet == null || dateRange == null) {
			throw new IllegalArgumentException("Invalid arguments to create WeeklyDateRange; niether dayCode nor dateRange can be null");
		}
		this.daySet = daySet;
		this.dateRange = dateRange;
	}

	public EnumSet<DayOfWeek> getDaySet() {
		return daySet;
	}

	public DateInterval getDateRange() {
		return dateRange;
	}

}
