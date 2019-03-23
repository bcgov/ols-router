/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DayCode {
	MF(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)),
	MS(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)),
	FS(EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)),
	FU(EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),
	SS(EnumSet.allOf(DayOfWeek.class));
	
	private final static Logger logger = LoggerFactory.getLogger(DayCode.class.getCanonicalName());
	
	private final EnumSet<DayOfWeek> daySet;
	
	private DayCode(EnumSet<DayOfWeek> daySet) {
		this.daySet = daySet;
	}
	
	public static Set<DayOfWeek> parse(String dayCode) {
		if(dayCode != null && !dayCode.isEmpty()) {
			try {
				return DayCode.valueOf(dayCode.toUpperCase()).daySet;
			} catch (IllegalArgumentException iae) {
				logger.warn("Illegal DayCode \"{}\", ignored.", dayCode);
			}
		}
		return null;
	}
	
	public EnumSet<DayOfWeek> getDaySet() {
		return daySet;
	}

	public static DayCode of(Set<DayOfWeek> set) {
		for(DayCode dayCode : values()) {
			if(dayCode.daySet.equals(set)) {
				return dayCode;
			} 
		}
		return null;
	}
}
