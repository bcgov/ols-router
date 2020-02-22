/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511;

import java.time.LocalDateTime;

import ca.bc.gov.ols.router.time.TemporalSet;

public class ScheduleInterval implements TemporalSet{
	private LocalDateTime start = null;
	private LocalDateTime end = null;
	
	public ScheduleInterval(LocalDateTime start, LocalDateTime end) {
		this.start = start;
		this.end = end;
	}
	
	public static ScheduleInterval parse(String str) {
		String[] parts = str.split("\\/");
		
		LocalDateTime start = LocalDateTime.parse(parts[0]);
		LocalDateTime end = null;
		if(parts.length > 1) {
			end = LocalDateTime.parse(parts[1]);
		} 
		return new ScheduleInterval(start, end);
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	@Override
	public boolean contains(LocalDateTime dateTime) {
		if((start == null || start.compareTo(dateTime) <= 0) 
				&& (end == null || end.isAfter(dateTime))) {
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
		return end.plusSeconds(1);
	}

}
