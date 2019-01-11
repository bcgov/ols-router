/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.data.enumTypes;

import java.time.DayOfWeek;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DayCode {
	MF(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)),
	MS(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)),
	SS(EnumSet.allOf(DayOfWeek.class));
	
	private final static Logger logger = LoggerFactory.getLogger(DayCode.class.getCanonicalName());
	
	private final EnumSet<DayOfWeek> daySet;
	
	private DayCode(EnumSet<DayOfWeek> daySet) {
		this.daySet = daySet;
	}
	
	public static EnumSet<DayOfWeek> parse(String dayCode) {
		if(dayCode != null && !dayCode.isEmpty()) {
			try {
				return DayCode.valueOf(dayCode.toUpperCase()).daySet;
			} catch (IllegalArgumentException iae) {
				logger.warn("Illegal DayCode \"%\", ignored.", dayCode);
			}
		}
		return null;
	}
	
	public EnumSet<DayOfWeek> getDaySet() {
		return daySet;
	}

	public static DayCode of(EnumSet<DayOfWeek> daySet) {
		for(DayCode dayCode : values()) {
			if(dayCode.daySet.equals(daySet)) {
				return dayCode;
			} 
		}
		return null;
	}
}
