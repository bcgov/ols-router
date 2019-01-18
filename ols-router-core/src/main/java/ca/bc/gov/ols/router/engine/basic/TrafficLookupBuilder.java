/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TrafficLookupBuilder {
	// the number of hours (in each direction in time) that a sample can influence
	// beyond this time, samples are forced to base speed (speedLimt)
	private static final double SPREAD = 2D;

	private BasicGraph graph;
	TIntObjectHashMap<Map<DayOfWeek,List<TrafficEntry>>> trafficData;
	
	public TrafficLookupBuilder(BasicGraph graph) {
		this.graph = graph;
		trafficData = new TIntObjectHashMap<Map<DayOfWeek,List<TrafficEntry>>>();
	}
	
	public void addTraffic(int edgeId, String days, LocalTime time, int speed) {
		String[] dayArray = days.split("\\|");
		for(String day : dayArray) {
			addEntry(edgeId, DayOfWeek.of(Integer.parseInt(day)), time, (short) speed);
		}
	}
		
	private void addEntry(int edgeId, DayOfWeek day, LocalTime time, short speed) {
		Map<DayOfWeek,List<TrafficEntry>> edgeEntries = trafficData.get(edgeId);
		if(edgeEntries == null) {
			edgeEntries = new EnumMap<DayOfWeek,List<TrafficEntry>>(DayOfWeek.class);
			trafficData.put(edgeId, edgeEntries);
		}
		List<TrafficEntry> dayEntries = edgeEntries.get(day);
		if(dayEntries == null) {
			dayEntries = new ArrayList<TrafficEntry>();
			edgeEntries.put(day, dayEntries);
		}
		dayEntries.add(new TrafficEntry(time, speed));
	}
	
	public TrafficLookup build() {
		TIntObjectHashMap<short[]> traffic = new TIntObjectHashMap<short[]>();
		trafficData.forEachEntry(new TIntObjectProcedure<Map<DayOfWeek,List<TrafficEntry>>>() {
			@Override
			public boolean execute(int edgeId, Map<DayOfWeek, List<TrafficEntry>> trafficMap) {
				trafficData.remove(edgeId);
				short baseSpeed = graph.getSpeedLimit(edgeId);
				short[] t = new short[7*24];
				traffic.put(edgeId, t);
				for(DayOfWeek day : DayOfWeek.values()) {
					int dayOffset = (day.getValue()-1) * 24;
					List<TrafficEntry> entryList = trafficMap.get(day);
					if(entryList == null || entryList.isEmpty()) {
						for(int hour = 0; hour < 24; hour++) {
							t[dayOffset+hour] = baseSpeed;
						}
						continue;
					}
					Collections.sort(entryList, Comparator.comparing((entry) -> entry.hours));
					// insert extra data points to push times > SPREAD hours from known data points to the baseSpeed
					for(int entryIdx = 0; entryIdx <= entryList.size(); entryIdx++) {
						if(entryIdx == 0) {
							TrafficEntry entry = entryList.get(entryIdx);
							// if the first entry is more than SPREAD hours after midnight
							if(entry.hours - SPREAD > 0) {
								// add an extra one at the beginning to force midnight to baseSpeed
								entryList.add(0, new TrafficEntry(0, baseSpeed));
								entryIdx++;
							}
							entryList.add(entryIdx, new TrafficEntry(entry.hours - SPREAD, baseSpeed));
							entryIdx++;
						} else if(entryIdx == entryList.size()) {
							TrafficEntry entry = entryList.get(entryIdx-1);
							entryList.add(new TrafficEntry(entry.hours + SPREAD, baseSpeed));
							entryIdx++;
							// if the last sample is more than SPREAD hours before midnight 
							if(entry.hours + SPREAD < 24) {
								// add an extra one at the end to force midnight to baseSpeed
								entryList.add(new TrafficEntry(24, baseSpeed));
								entryIdx++;
							}
						} else {
							TrafficEntry entry1 = entryList.get(entryIdx-1);
							TrafficEntry entry2 = entryList.get(entryIdx);
							if(entry2.hours - entry1.hours >= 2*SPREAD) {
								entryList.add(entryIdx, new TrafficEntry(entry1.hours + SPREAD, baseSpeed));
								entryList.add(entryIdx+1, new TrafficEntry(entry2.hours - SPREAD, baseSpeed));
								entryIdx += 2;
							}
						}						
					}
					// now go through all the entries and interpolate the hours
					for(int entryIdx = 0; entryIdx < entryList.size()-1; entryIdx++) {
						TrafficEntry entry1 = entryList.get(entryIdx);
						TrafficEntry entry2 = entryList.get(entryIdx+1);							
						for(int hour = Math.max(0, (int)Math.round(Math.ceil(entry1.hours))); hour < Math.min(24, entry2.hours); hour++) {
							double scale = ((double)hour - entry1.hours) / (entry2.hours - entry1.hours);
							t[dayOffset+hour] = (short)Math.round((double)entry1.speed * (1-scale) + (double)entry2.speed * scale);								
						}
					}
				}
				return true;
			}
		});
		return new TrafficLookup(traffic);
	}

}

class TrafficEntry {
	final double hours;
	final short speed;
	
	public TrafficEntry(LocalTime time, short speed) {
		this.hours = (double)time.getHour() + ((double)time.getMinute())/60;
		this.speed = speed;
	}

	TrafficEntry(double hours, short speed) {
		this.hours = hours;
		this.speed = speed;
	}

	@Override
	public String toString() {
		return hours + ":" + speed;
	}
}