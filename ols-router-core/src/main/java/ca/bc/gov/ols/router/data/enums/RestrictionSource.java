package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RestrictionSource {
	ITN,
	RDM,
	BMIS,
	UNKNOWN;
	
	private static final Logger logger = LoggerFactory.getLogger(RestrictionSource.class.getCanonicalName());
	
	/**
	 * Takes a string value and returns the corresponding RestrictionSource object.
	 * 
	 * @param restrictionSource string representation of the restrictionSource
	 * @return the RestrictionSource corresponding to the given string representation.
	 */
	public static RestrictionSource convert(String restrictionSource) {
		for(RestrictionSource rs : values()) {
			if(rs.name().equalsIgnoreCase(restrictionSource)) {
				return rs;
			}
        } 
		logger.warn("Invalid RestrictionSource value: '{}'.", restrictionSource);
		return UNKNOWN;
	}
	
}
