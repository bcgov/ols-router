package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RestrictionType {
	HORIZONTAL("width", "m"),
	VERTICAL("height", "m"),
	GVW("GVW", "kg"), // Gross Vehicle Weight
	GVW_1AXLE("GVW-1AXLE", "kg"),
	GVW_2AXLE("GVW-2AXLE", "kg"),
	GVW_3AXLE("GVW-3AXLE", "kg"),
	UNKNOWN("unknown", "");
	
	private static final Logger logger = LoggerFactory.getLogger(RestrictionType.class.getCanonicalName());
	
	public final String visName;
	public final String unit;
	
	RestrictionType(String visName, String unit) {
		this.visName = visName;
		this.unit = unit;
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
