package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RestrictionType {
	HORIZONTAL("width"),
	VERTICAL("height"),
	GVW("GVW"),
	GVW_1AXLE("GVW-1AXLE"),
	GVW_2AXLE("GVW-2AXLE"),
	GVW_3AXLE("GVW-3AXLE"),
	UNKNOWN("unknown");
	
	private static final Logger logger = LoggerFactory.getLogger(RestrictionType.class.getCanonicalName());
	
	public final String visName;
	
	RestrictionType(String visName) {
		this.visName = visName;
	}

	/**
	 * Takes a string value and returns the corresponding RestrictionType object.
	 * 
	 * @param restrictionType string representation of the restrictionType
	 * @return the RestrictionType corresponding to the given string representation.
	 */
	public static RestrictionType convert(String restrictionType) {
		for(RestrictionType t : values()) {
			if(t.name().equalsIgnoreCase(restrictionType) || t.visName.equalsIgnoreCase(restrictionType)) {
				return t;
			}
        } 
		logger.warn("Invalid RestrictionType value: '{}'.", restrictionType);
		return UNKNOWN;
	}
	
}
