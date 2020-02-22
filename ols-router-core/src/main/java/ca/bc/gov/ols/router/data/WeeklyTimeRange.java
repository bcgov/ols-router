/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ca.bc.gov.ols.router.data.enums.DayCode;
import ca.bc.gov.ols.router.time.TemporalSet;

/** 
 * A WeeklyTimeRange describes a range of 
 * days of the week and time ranges of those days 
 */ 
public class WeeklyTimeRange implements TemporalSet {
	public static final LocalTime TIME_AM_START = LocalTime.parse("07:00");
	public static final LocalTime TIME_AM_END = LocalTime.parse("09:00");
	public static final LocalTime TIME_PM_START = LocalTime.parse("16:00");
	public static final LocalTime TIME_PM_END = LocalTime.parse("18:00");
	
	public static final List<LocalTime> TIME_RANGE_ALWAYS = Collections.unmodifiableList(Arrays.asList(LocalTime.MIN, LocalTime.MAX));
	public static final List<LocalTime> TIME_RANGE_AM = Collections.unmodifiableList(Arrays.asList(TIME_AM_START, TIME_AM_END));
	public static final List<LocalTime> TIME_RANGE_PM = Collections.unmodifiableList(Arrays.asList(TIME_PM_START, TIME_PM_END));
	public static final List<LocalTime> TIME_RANGE_AMPM = Collections.unmodifiableList(Arrays.asList(TIME_AM_START, TIME_AM_END, TIME_PM_START, TIME_PM_END));
	public static final List<LocalTime> TIME_RANGE_DAY = Collections.unmodifiableList(Arrays.asList(TIME_AM_START, TIME_PM_END));

	public static final WeeklyTimeRange ALWAYS = new WeeklyTimeRange(EnumSet.allOf(DayOfWeek.class), TIME_RANGE_ALWAYS);
	private final Set<DayOfWeek> daySet;
	private final List<LocalTime> timeRanges;

	public WeeklyTimeRange(Set<DayOfWeek> daySet, List<LocalTime> timeRanges) {
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
			for(int i = 0; i < timeRanges.size(); i += 2) {
				if(timeRanges.get(i).isBefore(timeRanges.get(i+1))) {
					// start time is before end time, normal order
					if(time.isAfter(timeRanges.get(i)) && time.isBefore(timeRanges.get(i+1))) {
						return true;
					}
				} else {
					// start time is after end time, outside order
					if(time.isAfter(timeRanges.get(i)) || time.isBefore(timeRanges.get(i+1))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isAlways() {
		return daySet.equals(DayCode.SS.getDaySet()) && timeRanges.equals(TIME_RANGE_ALWAYS);
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
				for(int i = 0; i < timeRanges.size(); i += 2) {
					if(timeRanges.get(i).isBefore(timeRanges.get(i+1))) {
						// start time is before end time, normal order
						if(time.isAfter(timeRanges.get(i)) && time.isBefore(timeRanges.get(i+1))) {
							dateTime = dateTime.with(timeRanges.get(i+1));
							changed = true;
							continue restart;
						}
					} else {
						// start time is after end time, outside order
						if(time.isBefore(timeRanges.get(i+1))) {
							dateTime = dateTime.with(timeRanges.get(i+1));
							changed = true;
							continue restart;
						} else if(time.isAfter(timeRanges.get(i))) {
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

	public Set<DayOfWeek> getDaySet() {
		return daySet;
	}

	public List<LocalTime> getTimeRanges() {
		return timeRanges;
	}

	public String getTimeRangeString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < timeRanges.size(); i += 2) {
			if(i != 0) {
				sb.append("|");
			}
			if(timeRanges.get(i).equals(LocalTime.MIN) && timeRanges.get(i+1).equals(LocalTime.MAX)) {
				sb.append("ALWAYS");
			} else {
				sb.append(timeRanges.get(i) + "-" + timeRanges.get(i+1));
			}
		}
		return sb.toString();
	}

	public static WeeklyTimeRange create(String dayCodeStr, String timeRangeStr) {
		Set<DayOfWeek> daySet = DayCode.parse(dayCodeStr);
		if(timeRangeStr == null || timeRangeStr.isEmpty()) {
			return null;
		}
		LocalTime[] timeRanges = null;
		if("ALWAYS".equals(timeRangeStr)) {
			return new WeeklyTimeRange(daySet, TIME_RANGE_ALWAYS);
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
		return new WeeklyTimeRange(daySet, Arrays.asList(timeRanges));
	}

	public static boolean isAlways(WeeklyTimeRange range) {
		return (range != null && range.isAlways()); 
	}
}
