/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enumTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.data.WeeklyTimeRange;

public enum TurnTimeCode {
	MF_24(WeeklyTimeRange.create("MF", "ALWAYS")),
	MF_AM(WeeklyTimeRange.create("MF", "07:00-09:00")),
	MF_AMPM(WeeklyTimeRange.create("MF", "07:00-09:00|16:00-18:00")),
	MF_DAY(WeeklyTimeRange.create("MF", "07:00-18:00")),
	MF_PM(WeeklyTimeRange.create("MF", "16:00-18:00")),
	MS_24(WeeklyTimeRange.create("MS", "ALWAYS")),
	MS_AM(WeeklyTimeRange.create("MS", "07:00-09:00")),
	MS_AMPM(WeeklyTimeRange.create("MS", "07:00-09:00|16:00-18:00")),
	MS_DAY(WeeklyTimeRange.create("MS", "07:00-18:00")),
	MS_PM(WeeklyTimeRange.create("MS", "16:00-18:00")),
	SS_24(WeeklyTimeRange.create("SS", "ALWAYS")),
	SS_AM(WeeklyTimeRange.create("SS", "07:00-09:00")),
	SS_AMPM(WeeklyTimeRange.create("SS", "07:00-09:00|16:00-18:00")),
	SS_DAY(WeeklyTimeRange.create("SS", "07:00-18:00")),
	SS_PM(WeeklyTimeRange.create("SS", "16:00-18:00"));

	private final static Logger logger = LoggerFactory.getLogger(TurnTimeCode.class.getCanonicalName());

	private final WeeklyTimeRange timeRange;
	
	private TurnTimeCode(WeeklyTimeRange timeRange) {
		this.timeRange = timeRange;
	}
	
	/**
	 * Takes a string value and returns the corresponding TurnTimeCode object.
	 * 
	 * @param turnTimeCode string representation of the DividerType
	 * @return the TurnTimeCode corresponding to the given string representation.
	 */
	public static TurnTimeCode convert(String turnTimeCode) {
		if(turnTimeCode != null && !turnTimeCode.isEmpty()) {
			try {
				return TurnTimeCode.valueOf((turnTimeCode.substring(0, 2) + "_" + turnTimeCode.substring(3)).toUpperCase());
			} catch (IllegalArgumentException iae) {
				logger.warn("Illegal turnTimeCode \"%\", ignored.", turnTimeCode);
			}
		}
		return null;
	}

	public WeeklyTimeRange getTimeRange() {
		return timeRange;
	}

}
