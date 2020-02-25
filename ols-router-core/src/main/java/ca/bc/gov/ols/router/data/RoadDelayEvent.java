/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;

import java.time.LocalDateTime;

import ca.bc.gov.ols.router.time.TemporalSet;

/**
 * A RoadDelayEvent is a type of RoadEvent that causes a known, 
 * fixed delay in traversing the associated segment(s).
 * 
 * @author chodgson@refractions.net
 */
public class RoadDelayEvent extends RoadEvent {
	private final int delay; // in seconds
	
	/**
	 * Creates a RoadDelayEvent at the specified time, with the specified delay.
	 * @param time a TemporalSet representing the set of times during which the specified delay should be applied
	 * @param secondsDelay the number of seconds of delay which should be applied when traversing segments associate with this event
	 */
	public RoadDelayEvent(TemporalSet time, int secondsDelay) {
		super(time);
		this.delay = secondsDelay;
	}

	/**
	 * Returns the delay, in seconds, caused by this road event 
	 * @return the delay, in seconds
	 */
	public int getDelay() {
		return delay;
	}

	@Override
	public int getDelay(LocalDateTime dateTime) {
		return delay;
	}

}
