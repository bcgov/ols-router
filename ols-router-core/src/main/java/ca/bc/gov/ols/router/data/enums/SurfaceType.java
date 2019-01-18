/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum SurfaceType {
	BOAT("B"),
	DECOMMISSIONED("D"),
	LOOSE("L"),
	OVERGROWN("O"),
	PAVED("P"),
	ROUGH("R"),
	SEASONAL("S"),
	UNKNOWN("U");
	
	private static final Logger logger = LoggerFactory.getLogger(SurfaceType.class.getCanonicalName());
	
	private String label;
	
	private SurfaceType(String label) {
		this.label = label;
	}
	
	/**
	 * Takes a string value and returns the corresponding SurfaceType object.
	 * 
	 * @param surfaceType string representation of the SurfaceType
	 * @return the SurfaceType corresponding to the given string representation.
	 */
	public static SurfaceType convert(String surfaceType) {
		if(surfaceType != null && !surfaceType.isEmpty()) {
			for(SurfaceType dt : values()) {
				if(dt.label.equalsIgnoreCase(surfaceType.substring(0, 1))) {
					return dt;
				}
			}
		}
		logger.warn("Invalid SurfaceType value: '{}'.", surfaceType);
		return UNKNOWN;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
