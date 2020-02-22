/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class RecurringSchedule {
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalTime dailyStartTime;
	private LocalTime dailyEndTime;
	private Set<DayOfWeek> days;
	
	public LocalDate getStartDate() {
		return startDate;
	}
	
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	
	public LocalDate getEndDate() {
		return endDate;
	}
	
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	
	public LocalTime getDailyStartTime() {
		return dailyStartTime;
	}
	
	public void setDailyStartTime(LocalTime dailyStartTime) {
		this.dailyStartTime = dailyStartTime;
	}
	
	public LocalTime getDailyEndTime() {
		return dailyEndTime;
	}
	
	public void setDailyEndTime(LocalTime dailyEndTime) {
		this.dailyEndTime = dailyEndTime;
	}
	
	public Set<DayOfWeek> getDays() {
		return days;
	}
	
	public void setDays(Set<DayOfWeek> days) {
		this.days = days;
	}
	
}
