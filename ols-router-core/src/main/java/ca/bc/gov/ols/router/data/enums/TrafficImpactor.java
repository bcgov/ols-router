/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TrafficImpactor {
	NONE("", 100),
	BARRICADE("B", 0),
	CULDESAC("C", 100),
	DEADEND("D", 0),
	GATE("G", 100),
	LIGHT("L", 1),
	MALL("M", 100), // PEDESTRIAN MALL
	OVERPASS("O", 100),
	ROUNDABOUT("R", 100),
	STOPSIGN("S", 50),
	TOLLBOOTH("T", 100),
	UNDERPASS("U", 100),
	YIELD("Y", 75);
	
	private static final Logger logger = LoggerFactory.getLogger(TrafficImpactor.class.getCanonicalName());
	
	private final String label;
	private final int priority;
	
	private TrafficImpactor(String label, int priority) {
		this.label = label;
		this.priority = priority;
	}
	
	/**
	 * Takes a string value and returns the corresponding TrafficImpactor object.
	 * 
	 * @param trafficImpactor string representation of the TrafficImpactor
	 * @return the TrafficImpactor corresponding to the given string representation.
	 */
	public static TrafficImpactor convert(String trafficImpactor) {
		if(trafficImpactor == null || trafficImpactor.isEmpty() || trafficImpactor.equals(" ")) {
			return NONE;
		}
		for(TrafficImpactor lr : values()) {
			if(lr.label.equalsIgnoreCase(trafficImpactor)) {
				return lr;
			}
		}
		logger.warn("Invalid TrafficImpactor value: '{}'.", trafficImpactor);
		return NONE;
	}
	
	public boolean hasPriority(TrafficImpactor other){
		if(priority > other.priority) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
