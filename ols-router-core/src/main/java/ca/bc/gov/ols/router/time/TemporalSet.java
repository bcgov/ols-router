/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.time;

import java.time.LocalDateTime;

public interface TemporalSet {

	public static final TemporalSet ALWAYS = new Always();
	
	public boolean contains(LocalDateTime dateTime);
	
	public boolean isAlways();
	
	// returns the first date/time that is outside of this set and after the given date
	public LocalDateTime after(LocalDateTime dateTime);
	
	public default TemporalSet and(TemporalSet other) {
		return new TemporalSetIntersection(this, other);
	}

	public default TemporalSet or(TemporalSet other) {
		return new TemporalSetUnion(this, other);
	}

	
}
