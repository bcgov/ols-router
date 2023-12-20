package ca.bc.gov.ols.router.data.enums;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RestrictionType is not a real enum so that it can support newly added restrictionTypes without code changes.
 * However we will eventually want to add support for new types so that we have friendly names and units.
 */
public class RestrictionType {
	private static final Logger logger = LoggerFactory.getLogger(RestrictionType.class.getCanonicalName());
	
	private static final HashMap<String, RestrictionType> typeMap = new HashMap<String, RestrictionType>();
	public static final RestrictionType HORIZONTAL = new RestrictionType("HORIZONTAL", "Width", "m", true);
	public static final RestrictionType VERTICAL = new RestrictionType("VERTICAL", "Height", "m", true);
	public static final RestrictionType WEIGHT_GVW = new RestrictionType("WEIGHT-GVW", "Gross Vehicle Weight", "kg", true); // Gross Vehicle Weight
	public static final RestrictionType WEIGHT_1AXLE = new RestrictionType("WEIGHT-1AXLE", "Single Axle Weight", "kg", true); // weight on a single axle
	public static final RestrictionType WEIGHT_2AXLE = new RestrictionType("WEIGHT-2AXLE", "Tandem Axle Weight", "kg", true); // weight on a tandem-axle
	public static final RestrictionType WEIGHT_3AXLE = new RestrictionType("WEIGHT-3AXLE", "Tridem Axle Weight", "kg", true); // weight on a tridem-axle
	public static final RestrictionType NCV = new RestrictionType("NCV", "No Commercial Vehicle", "", false);
	public static final RestrictionType NSI = new RestrictionType("NSI", "No Self Issue", "", false);
	
	public final String name;
	public final String visName;
	public final String unit;
	public final boolean hasValue;
	
	private RestrictionType(String name, String visName, String unit, boolean hasValue) {
		this.name = name;
		this.visName = visName;
		this.unit = unit;
		this.hasValue = hasValue;
		typeMap.put(name, this);
	}

	
	/**
	 * Takes a string value and returns the corresponding RestrictionType object.
	 * Returns null if the type is not known.
	 * 
	 * @param restrictionType string representation of the restrictionType
	 * @return the RestrictionType corresponding to the given string representation, or null if there isn't one
	 */
	public static RestrictionType get(String restrictionType) {
		return typeMap.get(restrictionType);
	}
	
	/**
	 * Takes a string value and returns the corresponding RestrictionType object.
	 * Makes a new type if the given type is not known.
	 * 
	 * @param restrictionType string representation of the restrictionType
	 * @return the RestrictionType corresponding to the given string representation.
	 */
	public static RestrictionType convert(String restrictionType) {
		RestrictionType t = typeMap.get(restrictionType);
		if(t == null) {
			t = new RestrictionType(restrictionType, restrictionType, "", true);
			logger.warn("Unknown RestrictionType value: '{}'.", restrictionType);
        } 
		return t;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof RestrictionType 
				&& this.name.equals(((RestrictionType)other).name)) { 
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
}
