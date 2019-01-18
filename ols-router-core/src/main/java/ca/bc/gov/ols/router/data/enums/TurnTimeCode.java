/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.data.WeeklyTimeRange;
import static ca.bc.gov.ols.router.data.WeeklyTimeRange.*;

public enum TurnTimeCode {
	MF_24(new WeeklyTimeRange(DayCode.MF.getDaySet(), TIME_RANGE_ALWAYS)),
	MF_AM(new WeeklyTimeRange(DayCode.MF.getDaySet(), TIME_RANGE_AM)),
	MF_AMPM(new WeeklyTimeRange(DayCode.MF.getDaySet(), TIME_RANGE_AMPM)),
	MF_DAY(new WeeklyTimeRange(DayCode.MF.getDaySet(), TIME_RANGE_DAY)),
	MF_PM(new WeeklyTimeRange(DayCode.MF.getDaySet(), TIME_RANGE_PM)),
	MS_24(new WeeklyTimeRange(DayCode.MS.getDaySet(), TIME_RANGE_ALWAYS)),
	MS_AM(new WeeklyTimeRange(DayCode.MS.getDaySet(), TIME_RANGE_AM)),
	MS_AMPM(new WeeklyTimeRange(DayCode.MS.getDaySet(), TIME_RANGE_AMPM)),
	MS_DAY(new WeeklyTimeRange(DayCode.MS.getDaySet(), TIME_RANGE_DAY)),
	MS_PM(new WeeklyTimeRange(DayCode.MS.getDaySet(), TIME_RANGE_PM)),
	SS_24(new WeeklyTimeRange(DayCode.SS.getDaySet(), TIME_RANGE_ALWAYS)),
	SS_AM(new WeeklyTimeRange(DayCode.SS.getDaySet(), TIME_RANGE_AM)),
	SS_AMPM(new WeeklyTimeRange(DayCode.SS.getDaySet(), TIME_RANGE_AMPM)),
	SS_DAY(new WeeklyTimeRange(DayCode.SS.getDaySet(), TIME_RANGE_DAY)),
	SS_PM(new WeeklyTimeRange(DayCode.SS.getDaySet(), TIME_RANGE_PM));

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
				logger.warn("Illegal turnTimeCode '{}' ignored.", turnTimeCode);
			}
		}
		return null;
	}

	public WeeklyTimeRange getTimeRange() {
		return timeRange;
	}

}
