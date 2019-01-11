/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;

import ca.bc.gov.app.router.data.enumTypes.DayCode;
import ca.bc.gov.app.router.time.TemporalSet;

/** 
 * A WeeklyTimeRange describes a range of 
 * days of the week and time ranges of those days 
 */ 
public class WeeklyTimeRange implements TemporalSet {
	public static final WeeklyTimeRange ALWAYS = new WeeklyTimeRange(EnumSet.allOf(DayOfWeek.class), new LocalTime[] {LocalTime.MIN, LocalTime.MAX});
	private final EnumSet<DayOfWeek> daySet;
	private final LocalTime[] timeRanges;

	public WeeklyTimeRange(EnumSet<DayOfWeek> daySet, LocalTime[] timeRanges) {
		if(daySet == null || timeRanges == null) {
			throw new IllegalArgumentException("Invalid arguments to create WeeklyTimeRange; niether dayCode nor timeRanges can be null");
		}
		this.daySet = daySet;
		this.timeRanges = timeRanges;
	}
	
	public boolean contains(LocalDateTime dateTime) {
		if(dateTime == null) {
			return isAlways();
		}
		if(daySet.contains(dateTime.getDayOfWeek())) {
			LocalTime time = LocalTime.from(dateTime);
			for(int i = 0; i < timeRanges.length; i += 2) {
				if(timeRanges[i].isBefore(timeRanges[i+1])) {
					// start time is before end time, normal order
					if(time.isAfter(timeRanges[i]) && time.isBefore(timeRanges[i+1])) {
						return true;
					}
				} else {
					// start time is after end time, outside order
					if(time.isAfter(timeRanges[i]) || time.isBefore(timeRanges[i+1])) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isAlways() {
		if(daySet.equals(DayCode.SS.getDaySet()) 
				&& timeRanges[0].equals(LocalTime.MIN)
				&& timeRanges[1].equals(LocalTime.MAX)) {
			return true;
		}
		return false;
	}

	@Override
	public LocalDateTime after(LocalDateTime dateTime) {
		if(isAlways()) return null;
		boolean changed = true;
		restart:
		while(changed) {
			changed = false;
			if(daySet.contains(dateTime.getDayOfWeek())) {
				LocalTime time = LocalTime.from(dateTime);
				for(int i = 0; i < timeRanges.length; i += 2) {
					if(timeRanges[i].isBefore(timeRanges[i+1])) {
						// start time is before end time, normal order
						if(time.isAfter(timeRanges[i]) && time.isBefore(timeRanges[i+1])) {
							dateTime = dateTime.with(timeRanges[i+1]);
							changed = true;
							continue restart;
						}
					} else {
						// start time is after end time, outside order
						if(time.isBefore(timeRanges[i+1])) {
							dateTime = dateTime.with(timeRanges[i+1]);
							changed = true;
							continue restart;
						} else if(time.isAfter(timeRanges[i])) {
							dateTime = dateTime.plusDays(1).with(LocalTime.MIN);
							changed = true;
							continue restart;
						}
					}					
				}
			}
		}
		return dateTime;
	}

	public EnumSet<DayOfWeek> getDaySet() {
		return daySet;
	}

	public LocalTime[] getTimeRanges() {
		return timeRanges;
	}

	public String getTimeRangeString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < timeRanges.length; i += 2) {
			if(i != 0) {
				sb.append("|");
			}
			if(timeRanges[i].equals(LocalTime.MIN) && timeRanges[i+1].equals(LocalTime.MAX)) {
				sb.append("ALWAYS");
			} else {
				sb.append(timeRanges[i] + "-" + timeRanges[i+1]);
			}
		}
		return sb.toString();
	}

	public static WeeklyTimeRange create(String dayCodeStr, String timeRangeStr) {
		EnumSet<DayOfWeek> daySet = DayCode.parse(dayCodeStr);
		if(timeRangeStr == null || timeRangeStr.isEmpty()) {
			return null;
		}
		LocalTime[] timeRanges = null;
		if("ALWAYS".equals(timeRangeStr)) {
			timeRanges = new LocalTime[2];
			timeRanges[0] = LocalTime.MIN;
			timeRanges[1] = LocalTime.MAX;
			return new WeeklyTimeRange(daySet, timeRanges);
		}
		String[] rangeStrs = timeRangeStr.split("\\|");
		timeRanges = new LocalTime[rangeStrs.length * 2];
		for(int i = 0; i < rangeStrs.length; i++) {
			String rangeStr = rangeStrs[i];
			String[] timeStrs = rangeStr.split("-");
			if(timeStrs.length != 2) {
				throw new IllegalArgumentException("Invalid time range string for creation of WeeklyTimeRange: " + timeRangeStr );
			}
			timeRanges[i*2] = LocalTime.parse(timeStrs[0]);
			timeRanges[i*2+1] = LocalTime.parse(timeStrs[1]);
		}
		return new WeeklyTimeRange(daySet, timeRanges);
	}

}
