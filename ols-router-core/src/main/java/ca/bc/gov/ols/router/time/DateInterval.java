/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateInterval implements TemporalSet {
	private LocalDate start = null;
	private LocalDate end = null;
	
	public DateInterval(LocalDate start, LocalDate end) {
		this.start = start;
		this.end = end;
	}
	
	public LocalDate getStart() {
		return start;
	}

	public void setStart(LocalDate start) {
		this.start = start;
	}

	public LocalDate getEnd() {
		return end;
	}

	public void setEnd(LocalDate end) {
		this.end = end;
	}

	@Override
	public boolean contains(LocalDateTime dateTime) {
		LocalDate date = dateTime.toLocalDate();
		if((start == null || start.compareTo(date) <= 0) 
				&& (end == null || end.isAfter(date))) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isAlways() {
		if(start == null && end == null) {
			return true;
		}
		return false;
	}
	

	@Override
	public LocalDateTime after(LocalDateTime dateTime) {
		if(!contains(dateTime)) return dateTime;
		return end.plusDays(1).atStartOfDay();
	}

}
