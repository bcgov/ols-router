/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.data;

import java.time.LocalDateTime;

import ca.bc.gov.app.router.time.TemporalSet;

public class RoadEvent {

	private final TemporalSet time;
	
	public RoadEvent(TemporalSet time) {
		this.time = time;
	}
	
	public boolean contains(LocalDateTime dateTime) {
		return time.contains(dateTime);
	}

	public TemporalSet getTime() {
		return time;
	}
	
}
