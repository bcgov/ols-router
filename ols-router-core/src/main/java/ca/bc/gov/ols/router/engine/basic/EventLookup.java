/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.ols.router.data.RoadClosureEvent;
import ca.bc.gov.ols.router.data.RoadDelayEvent;
import ca.bc.gov.ols.router.data.RoadEvent;

public class EventLookup {

	private BasicGraph graph;
	private TIntObjectHashMap<ArrayList<RoadEvent>> eventMap;
	
	public EventLookup(BasicGraph graph) {
		this.graph = graph;
		eventMap = new TIntObjectHashMap<ArrayList<RoadEvent>>();
	}
	
	public void addEvent(List<Integer> edgeIds, RoadEvent evt) {
		for(int edgeId : edgeIds) {
			ArrayList<RoadEvent> eventList = eventMap.get(edgeId);
			if(eventList == null) {
				eventList = new ArrayList<RoadEvent>(1);
				eventMap.put(edgeId, eventList);
			}
			eventList.add(evt);
		}
	}
	
	public int lookup(final int edgeId, final LocalDateTime dateTime) {
		ArrayList<RoadEvent> eventList = eventMap.get(edgeId);
		if(eventList != null) {
			for(RoadEvent evt : eventList) {
				if(evt.contains(dateTime)) {
					if(evt instanceof RoadClosureEvent) {
						LocalDateTime nextTime = evt.getTime().after(dateTime);
						if(nextTime == null) {
							return -1;
						}
						return (int) dateTime.until(nextTime, ChronoUnit.SECONDS);
					} else if(evt instanceof RoadDelayEvent) {
						return ((RoadDelayEvent)evt).getDelay();
					}
				} 
			}
		}
		return 0;
	}
}

