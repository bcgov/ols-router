/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.time;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

public class DaySet implements TemporalSet {
	private final Set<DayOfWeek> days;

	public DaySet(Set<DayOfWeek> days) {
		this.days = Set.copyOf(days);
	}

	@Override
	public boolean contains(LocalDateTime dateTime) {
		return days.contains(dateTime.getDayOfWeek());
	}

	@Override
	public boolean isAlways() {
		return EnumSet.allOf(DayOfWeek.class).equals(days);
	}

	@Override
	public LocalDateTime after(LocalDateTime dateTime) {
		if(isAlways()) return null;
		while(contains(dateTime)) {
			dateTime = dateTime.plusDays(1);
		}
		return dateTime;
	}

}
