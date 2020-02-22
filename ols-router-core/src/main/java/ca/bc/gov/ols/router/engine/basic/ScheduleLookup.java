/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.bc.gov.ols.router.time.WeeklyDateRange;

public class ScheduleLookup {
	
	private boolean sorted = false;
	private BasicGraph graph;
	private TIntObjectHashMap<ArrayList<LocalDateTime>> departureTimesByEdgeId;
	private TIntObjectHashMap<FerryInfo> ferryInfoByEdgeId;
	private static final int[] NONE = {0, -1};
	
	public ScheduleLookup(BasicGraph graph) {
		this.graph = graph;
		departureTimesByEdgeId = new TIntObjectHashMap<ArrayList<LocalDateTime>>();
		ferryInfoByEdgeId = new TIntObjectHashMap<FerryInfo>();
	}

	public void addFerryInfo(int edgeId, FerryInfo ferryInfo) {
		ferryInfoByEdgeId.put(edgeId, ferryInfo);
	}

	public FerryInfo getFerryInfo(int edgeId) {
		return ferryInfoByEdgeId.get(edgeId);
	}

	public void addSchedule(int edgeId, WeeklyDateRange schedule, LocalTime startTime) {
		ArrayList<LocalDateTime> departureTimes = departureTimesByEdgeId.get(edgeId);
		if(departureTimes == null) {
			departureTimes = new ArrayList<LocalDateTime>();
			departureTimesByEdgeId.put(edgeId, departureTimes);
		}
		int days = (int)ChronoUnit.DAYS.between(schedule.getDateRange().getStart(), schedule.getDateRange().getEnd());
		for(int i = 0; i <= days; i++) {
			if(schedule.getDaySet().contains(schedule.getDateRange().getStart().plusDays(i).getDayOfWeek())) {
				departureTimes.add(schedule.getDateRange().getStart().plusDays(i).atTime(startTime));
				sorted = false;
			}
		}
	}
	
	public void sort() {
		if(sorted) {
			return;
		}
		departureTimesByEdgeId.forEachValue(new TObjectProcedure<ArrayList<LocalDateTime>>() {
			@Override
			public boolean execute(ArrayList<LocalDateTime> departureTimes) {
				Collections.sort(departureTimes);
				departureTimes.trimToSize();
				return true;
			}
		});
		sorted = true;
	}

	// returns the number of seconds it will take to wait for schedule and the number of seconds to traverse the segment
	public int[] lookup(final int edgeId, final LocalDateTime dateTime) {
		sort();
		FerryInfo ferryInfo = ferryInfoByEdgeId.get(edgeId);
		if(ferryInfo == null) {
			return NONE;
		}
		ArrayList<LocalDateTime> departureTimes = departureTimesByEdgeId.get(edgeId);
		if(departureTimes == null) {
			if(ferryInfo.isScheduled()) {
				// this is a scheduled ferry but we don't have the schedule, assume an expected wait time of half the sailing time (typical of 2-vessel routes)
				return new int[] {ferryInfo.getTravelTime()/2,ferryInfo.getTravelTime()};
			}
			// unscheduled ferries are typically serviced by a single vessel and so have a longer expected wait time
			return new int[] {ferryInfo.getTravelTime(),ferryInfo.getTravelTime()};
		}
		// binary search the list of departureTimes to find the next available departure
		int idx = Collections.binarySearch(departureTimes, dateTime.plusSeconds(ferryInfo.getMinWaitTime()));
		// -ve idx means there was no exact match (typical) so we convert it to the next date/time in the future
		if(idx < 0) {
			idx = -(idx + 1); 
		}
		if(idx == departureTimes.size()) {
			return new int[] {ferryInfo.getMinWaitTime(),ferryInfo.getTravelTime()};
		}
		return new int[] {(int)ChronoUnit.SECONDS.between(dateTime, departureTimes.get(idx)), ferryInfo.getTravelTime()};
	}

	public List<LocalDateTime> lookupRange(final int edgeId, final LocalDateTime start, final LocalDateTime end) {
		sort();
		FerryInfo ferryInfo = ferryInfoByEdgeId.get(edgeId);
		if(ferryInfo == null) {
			return null;
		}
		ArrayList<LocalDateTime> departureTimes = departureTimesByEdgeId.get(edgeId);
		if(departureTimes == null) {
			if(ferryInfo.isScheduled()) {
				// we don't know what the schedule is
				return null;			
			}
			// empty means an unscheduled ferry
			return Collections.emptyList();
		}
		// binary search the list of departureTimes to find the next available departure
		int idx = Collections.binarySearch(departureTimes, start);
		// -ve idx means there was no exact match (typical) so we convert it to the next date/time in the future
		if(idx < 0) {
			idx = -(idx + 1); 
		}
		if(idx == departureTimes.size()) {
			return null;
		}
		int endIdx = idx;
		while(departureTimes.get(endIdx).isBefore(end)) {
			endIdx++;
		}
		return departureTimes.subList(idx, endIdx);
		
	}
	
}

