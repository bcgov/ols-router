/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import java.text.NumberFormat;
import java.util.HashMap;


public enum DistanceUnit {
	METRE(1, "m", "metres", "meter", "meters"),
	KILOMETRE(0.001, "km", "kilometres", "kilometer", "kilometers"),
	MILE(0.000621371192, "mi", "miles"),
	FOOT(3.28084, "ft", "feet");

	private static HashMap<String,DistanceUnit> nameMap = null;
	
	private String abbr;
	private String plural;
	private double toMeters;
	private String[] alternates;
	
	private DistanceUnit(double toMeters, String abbr, String plural, String... alternates ) {
		this.abbr = abbr;
		this.plural = plural;
		this.toMeters = toMeters;
		this.alternates = alternates;
	}
	
	/**
	 * Takes a string value and returns the corresponding DistanceUnit object.
	 * 
	 * @param distanceUnit string representation of the DistanceUnit
	 * @return the DistanceUnit corresponding to the given string representation.
	 */
	public static DistanceUnit convert(String distanceUnit) {
		if(nameMap == null) {
			buildNameMap();
		}
		DistanceUnit unit = nameMap.get(distanceUnit.toLowerCase());
		if(unit == null) {
			throw new IllegalArgumentException("Invalid DistanceUnit value: '"
					+ distanceUnit + "'.");
		}
		return unit;
	}

	public String abbr() {
		return abbr;
	}
	
	public String plural() {
		return plural;
	}

	public double convertTo(double distance, DistanceUnit newDistanceUnit) {
		return distance * newDistanceUnit.toMeters / toMeters;
	}
	
	public String formatForDisplay(double dist) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		if(!METRE.equals(this)) { 
			nf.setMaximumFractionDigits(3);
		}
		return nf.format(dist);
	}
	
	/**
	 * Return a formatted string representing the given distance in this DistanceUnit, for use in directions.
	 * The number is rounded to a "nice" value and units are up/down-converted to appropriately sized units.
	 * 
	 * @param dist the distance value to format
	 * @return the direction-use-formatted distance string with units
	 */
	public String formatForDirections(double dist) {
		// distance of zero means that this direction has no associated distance
		// eg. the end point
		if(dist == 0) {
			return "";
		}
		
		// switch to appropriate metric unit, if in metric
		DistanceUnit unit = this;
		if(METRE.equals(unit) && dist > 1000) {
			dist = dist/1000;
			unit = KILOMETRE;
		} else if(KILOMETRE.equals(unit) && dist < 1) {
			dist = dist * 1000;
			unit = METRE;
		} else if(MILE.equals(unit) && dist < 0.189394) {// 1000 ft in miles
			dist = dist * 5280;
			unit = FOOT;
		}
		
		NumberFormat nf = NumberFormat.getInstance();
		// don't show any decimal digits
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		
		StringBuilder sb = new StringBuilder();
		// round metres/feet to nearest 100/50/5 to make nice numbers
		if(METRE.equals(unit) || FOOT.equals(unit)) {
			int intDist;
			if(dist > 1000) {
				intDist = 100 * (int)Math.round(dist/100);
			} else if(dist > 100) {
				intDist = 50 * (int)Math.round(dist/50);
			} else if(dist > 10) { 
				intDist = 5 * (int)Math.round(dist/5);
			} else {
				intDist = (int)Math.round(dist);
			}
			sb.append(nf.format(intDist));
		} else {
			if(dist < 10) {
				// allow one digit of fractional km/miles
				nf.setMaximumFractionDigits(1);
			}
			sb.append(nf.format(dist));
		} 
		sb.append(" " + unit.abbr());
		return sb.toString();		
	}
	
	private static void buildNameMap() {
		nameMap = new HashMap<String,DistanceUnit>();
		for(DistanceUnit du : values()) {
			nameMap.put(du.name().toLowerCase(), du);
			nameMap.put(du.abbr.toLowerCase(), du);
			nameMap.put(du.plural.toLowerCase(), du);
			for(String alt : du.alternates) {
				nameMap.put(alt.toLowerCase(), du);
			}
		}
	}
	
}
