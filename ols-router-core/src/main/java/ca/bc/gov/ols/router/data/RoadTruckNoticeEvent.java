/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;

import java.time.LocalDateTime;

import ca.bc.gov.ols.router.data.enums.TruckNoticeType;
import ca.bc.gov.ols.router.time.TemporalSet;

/**
 * A RoadTruckNoticeEvent is a type of RoadEvent where there is some
 * additional information that trucks need to be aware of when traveling on the road.
 * 
 * @author chodgson@refractions.net
 *
 */
public class RoadTruckNoticeEvent extends RoadEvent {

	private TruckNoticeType type;
	private String notice;
	
	public RoadTruckNoticeEvent(TemporalSet time, TruckNoticeType type, String notice) {
		super(time);
		this.type = type;
		this.notice = notice;
	}

	public TruckNoticeType getType() {
		return type;
	}

	public String getNotice() {
		return notice;
	}

	@Override
	public int getDelay(LocalDateTime dateTime) {
		return 0;
	}
	
}
