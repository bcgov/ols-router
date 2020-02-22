/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DividerType {
	NONE("n"),
	SOFT("s"),
	HARD("h");
	
	private static final Logger logger = LoggerFactory.getLogger(DividerType.class.getCanonicalName());
	
	private String label;
	
	private DividerType(String label) {
		this.label = label;
	}
	
	/**
	 * Takes a string value and returns the corresponding DividerType object.
	 * 
	 * @param dividerType string representation of the DividerType
	 * @return the DividerType corresponding to the given string representation.
	 */
	public static DividerType convert(String dividerType) {
		if(dividerType == null) {
			return NONE;
		}
		for(DividerType dt : values()) {
			if(dt.label.equalsIgnoreCase(dividerType.substring(0, 1))) {
				return dt;
			}
		}
		logger.warn("Invalid DividerType value: '{}'.", dividerType);
		return NONE;
	}
	
	/**
	 * Returns true for soft or hard DividerTypes
	 * @return true for soft or hard DividerTypes, false otherwise
	 */
	public boolean isDivided() {
		return !this.equals(DividerType.NONE);
	}
	
	/**
	 * @return the string representation of this DividerType object
	 */
	@Override
	public String toString() {
		return label;
	}
}
