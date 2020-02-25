/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import ca.bc.gov.ols.router.time.TemporalSet;

/**
 * A RoadClosureEvent is a type of RoadEvent where the road
 * is completely closed and cannot be traversed during the specified times.
 * 
 * @author chodgson@refractions.net
 *
 */
public class RoadClosureEvent extends RoadEvent {

	public RoadClosureEvent(TemporalSet time) {
		super(time);
	}

	@Override
	public int getDelay(LocalDateTime dateTime) {
		LocalDateTime nextTime = time.after(dateTime);
		if(nextTime == null) {
			return -1;
		}
		return (int) dateTime.until(nextTime, ChronoUnit.SECONDS);
	}

}
