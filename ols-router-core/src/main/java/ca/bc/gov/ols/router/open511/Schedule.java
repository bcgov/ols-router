/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511;

import java.util.List;

import ca.bc.gov.ols.router.open511.enums.ScheduleException;

public class Schedule {
	List<RecurringSchedule> recurringSchedules;
	List<ScheduleException> exceptions;
	List<ScheduleInterval> intervals;
	
	public List<RecurringSchedule> getRecurringSchedules() {
		return recurringSchedules;
	}
	
	public void setRecurringSchedules(List<RecurringSchedule> recurringSchedules) {
		this.recurringSchedules = recurringSchedules;
	}
	
	public List<ScheduleException> getExceptions() {
		return exceptions;
	}
	
	public void setExceptions(List<ScheduleException> exceptions) {
		this.exceptions = exceptions;
	}
	
	public List<ScheduleInterval> getIntervals() {
		return intervals;
	}
	
	public void setIntervals(List<ScheduleInterval> intervals) {
		this.intervals = intervals;
	}
	
}
