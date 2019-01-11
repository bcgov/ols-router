/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.data;

import ca.bc.gov.app.router.time.TemporalSet;

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

}
