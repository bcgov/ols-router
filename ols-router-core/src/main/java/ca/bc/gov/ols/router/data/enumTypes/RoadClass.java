/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enumTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RoadClass {
	ALLEYWAY("alleyway", true, 1),
	ARTERIAL_MAJOR("arterial_major", true, 2),
	ARTERIAL_MINOR("arterial_minor", true, 2),
	COLLECTOR_MAJOR("collector_major", true, 2),
	COLLECTOR_MINOR("collector_minor", true, 2),
	DRIVEWAY("driveway", true, 1),
	FERRY("ferry", true, 0),
	FERRY_PASSENGER("ferry_passenger", false, 0),
	FREEWAY("freeway", true, 3),
	HIGHWAY_MAJOR("highway_major", true, 3),
	HIGHWAY_MINOR("highway_minor", true, 3),
	LANE("lane", true, 1),
	LOCAL("local", true, 1),
	PEDESTRIAN_MALL("pedestrian_mall", false, 1),
	RAMP("ramp", true, 2),
	RECREATION("recreation", true, 1),
	RESOURCE("resource", true, 1),
	RESTRICTED("restricted", true, 1),
	RUNWAY("runway", false, 0),
	SERVICE("service", true, 1),
	STRATA("strata", true, 1),
	TRAIL("trail", false, 0),
	TRAIL_RECREATION("trail_recreation", false, 0),
	WATER_ACCESS("water_access", false, 0),
	YIELD_LANE("yield_lane", true, 1),
	UNKNOWN("unknown", false, 1);
	
	private static final Logger logger = LoggerFactory.getLogger(RoadClass.class.getCanonicalName());
	
	private final String label;
	private final boolean routeable;
	private final int group;
	
	private RoadClass(String label, boolean routeable, int group) {
		this.label = label;
		this.routeable = routeable;
		this.group = group;
	}
	
	/**
	 * Takes a string value and returns the corresponding RoadClass object.
	 * 
	 * @param roadClass string representation of the RoadClass
	 * @return the RoadClass corresponding to the given string representation.
	 */
	public static RoadClass convert(String roadClass) {
		for(RoadClass rc : values()) {
			if(rc.label.equalsIgnoreCase(roadClass)) {
				return rc;
			}
		}
		//throw new IllegalArgumentException("Invalid RoadClass value: '" + roadClass + "'.");
		logger.warn("Invalid RoadClass value: '" + roadClass + "'.");;
		return UNKNOWN;
	}
	
	/**
	 * @return the string representation of this RoadClass object
	 */
	@Override
	public String toString() {
		return label;
	}
	
	public boolean isRouteable() {
		return routeable;
	}
	
	public int getGroup() {
		return group;
	}
	
}
