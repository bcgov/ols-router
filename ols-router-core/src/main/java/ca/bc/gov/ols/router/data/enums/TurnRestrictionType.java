package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TurnRestrictionType {
	I, // Implicit (generic)
	Y, // 2 or 3 ramp/turning lane intersection 
	X, // 4 or more ramp/turning lane intersection
	V, // Divided merge U/V-turn
	U, // Divided U-turn
	E, // Explicit
	UNKNOWN; //Unknown
	
	private static final Logger logger = LoggerFactory.getLogger(TurnRestrictionType.class.getCanonicalName());
	
	/**
	 * Takes a string value and returns the corresponding TurnRestrictionType object.
	 * 
	 * @param turnRestrictionType string representation of the turnRestrictionType
	 * @return the TurnRestrictionType corresponding to the given string representation.
	 */
	public static TurnRestrictionType convert(String turnRestrictionType) {
		for(TurnRestrictionType t : values()) {
			if(t.name().equals(turnRestrictionType)) {
				return t;
			}
        } 
		//logger.warn("Invalid TurnRestrictionType value: '{}'.", turnRestrictionType);
		return UNKNOWN;
	}
	
}
