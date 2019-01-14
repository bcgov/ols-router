/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;

import gnu.trove.map.hash.TIntObjectHashMap;

public class TrafficLookup {
	TIntObjectHashMap<short[]> traffic;
	
	TrafficLookup(TIntObjectHashMap<short[]> traffic) {
		this.traffic = traffic;
	}
	
	public short lookup(int edgeId, LocalDateTime time) {
		short[] t = traffic.get(edgeId);
		if(t == null) return 0;
		int dayOffset = time.getDayOfWeek().getValue() - 1;
		int offset = dayOffset * 24 + time.getHour();
		short a = t[offset];
		short b = t[(offset+1) % (7*24)];
		// scale linearly between hours based on the minute
		double scale = (double)time.getMinute() / 60.0;
		return (short)Math.round(a * (1 - scale) + b * scale);
	}
}
