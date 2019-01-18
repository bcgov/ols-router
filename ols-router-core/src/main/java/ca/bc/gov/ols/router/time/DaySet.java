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
	private final Set<DayOfWeek> daySet;

	public DaySet(Set<DayOfWeek> daySet) {
		this.daySet = Set.copyOf(daySet);
	}

	@Override
	public boolean contains(LocalDateTime dateTime) {
		if(daySet.contains(dateTime.getDayOfWeek())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isAlways() {
		if(EnumSet.allOf(DayOfWeek.class).equals(daySet)) {
			return true;
		}
		return false;
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
