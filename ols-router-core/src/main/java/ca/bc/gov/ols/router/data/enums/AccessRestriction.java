/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AccessRestriction {
	NONE("N"),
	A("A"),
	B("B"),
	RESTRICTED("R");
	
	private static final Logger logger = LoggerFactory.getLogger(AccessRestriction.class.getCanonicalName());
	
	private String label;
	
	private AccessRestriction(String label) {
		this.label = label;
	}
	
	/**
	 * Takes a string value and returns the corresponding AccessRestriction object.
	 * 
	 * @param accessRestriction string representation of the AccessRestriction
	 * @return the AccessRestriction corresponding to the given string representation.
	 */
	public static AccessRestriction convert(String accessRestriction) {
		if(accessRestriction == null) {
			return NONE;
		}
		for(AccessRestriction dt : values()) {
			if(dt.label.equalsIgnoreCase(accessRestriction.substring(0, 1))) {
				return dt;
			}
		}
		logger.warn("Invalid AccessRestriction value: '{}'.", accessRestriction);
		return NONE;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
