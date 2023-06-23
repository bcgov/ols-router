package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RestrictionType {
	HORIZONTAL,
	VERTICAL,
	WEIGHT,
	UNKNOWN;
	
	private static final Logger logger = LoggerFactory.getLogger(RestrictionType.class.getCanonicalName());
	
	/**
	 * Takes a string value and returns the corresponding RestrictionType object.
	 * 
	 * @param restrictionType string representation of the restrictionType
	 * @return the RestrictionType corresponding to the given string representation.
	 */
	public static RestrictionType convert(String restrictionType) {
		for(RestrictionType t : values()) {
			if(t.name().equalsIgnoreCase(restrictionType)) {
				return t;
			}
        } 
		logger.warn("Invalid RestrictionType value: '{}'.", restrictionType);
		return UNKNOWN;
	}
	
}
