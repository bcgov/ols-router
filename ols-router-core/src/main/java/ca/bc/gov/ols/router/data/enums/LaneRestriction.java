/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LaneRestriction {
	NONE(""),
	RESTRICTED("R"),
	NARROW("N");
	
	private static final Logger logger = LoggerFactory.getLogger(LaneRestriction.class.getCanonicalName());
	
	private String label;
	
	private LaneRestriction(String label) {
		this.label = label;
	}
	
	/**
	 * Takes a string value and returns the corresponding LaneRestriction object.
	 * 
	 * @param laneRestriction string representation of the LaneRestriction
	 * @return the LaneRestriction corresponding to the given string representation.
	 */
	public static LaneRestriction convert(String laneRestriction) {
		if(laneRestriction == null) {
			return NONE;
		}
		for(LaneRestriction lr : values()) {
			if(lr.label.equalsIgnoreCase(laneRestriction)) {
				return lr;
			}
		}
		logger.warn("Invalid LaneRestriction value: '{}'.", laneRestriction);
		return NONE;
	}
	
	@Override
	public String toString() {
		return label;
	}
}