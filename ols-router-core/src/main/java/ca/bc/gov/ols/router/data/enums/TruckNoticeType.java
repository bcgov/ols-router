package ca.bc.gov.ols.router.data.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TruckNoticeType {
	RESTRICTION,
	ADVISORY,
	BORDER,
	FERRY;
	
	private static final Logger logger = LoggerFactory.getLogger(SurfaceType.class.getCanonicalName());
	
	/**
	 * Takes a string value and returns the corresponding TruckNoticeType object.
	 * 
	 * @param truckNoticeType string representation of the TruckNoticeType
	 * @return the TruckNoticeType corresponding to the given string representation.
	 */
	public static TruckNoticeType convert(String truckNoticeType) {
		if(truckNoticeType != null && !truckNoticeType.isEmpty()) {
			for(TruckNoticeType t : values()) {
				if(t.name().equalsIgnoreCase(truckNoticeType)) {
					return t;
				}
			}
		}
		logger.warn("Invalid TruckNoticeType value: '{}'.", truckNoticeType);
		return null;
	}
	
}
