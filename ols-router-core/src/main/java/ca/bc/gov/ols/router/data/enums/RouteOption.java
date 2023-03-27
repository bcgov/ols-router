/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RouteOption {
	EVENTS("ev"),
	GLOBAL_DISTORTION_FIELD("gdf"),
	LOCAL_DISTORTION_FIELD("ldf"),
	SCHEDULING("sc"), 
	TIME_DEPENDENCY("td"),
	TRAFFIC("tf"),
	TURN_COSTS("tc"),
	TURN_RESTRICTIONS("tr"),
	XING_COSTS("xc");
	
	private static final Logger logger = LoggerFactory.getLogger(RouteOption.class.getCanonicalName());
	
	private String abbr;
	
	private RouteOption(String abbr) {
		this.abbr = abbr;
	}
	
	/**
	 * Takes a string value and returns the corresponding RouteOption object.
	 * 
	 * @param routeOption string representation of the RouteOption
	 * @return the RouteOption corresponding to the given string representation.
	 */
	public static RouteOption convert(String routeOption) {
		if(routeOption != null && !routeOption.isEmpty()) {
			for(RouteOption ro : values()) {
				if(ro.abbr.equalsIgnoreCase(routeOption) || ro.name().equalsIgnoreCase(routeOption)) {
					return ro;
				}
			}
		}
		return null;
	}
	
	/**
	 * Takes a string with a comma-separated list of RouteOption values 
	 * and returns an EnumSet of the corresponding RouteOptions.
	 * 
	 * @param routeOptionList string of comma-separated list of RouteOption values
	 * @return the EnumSet of RouteOptions corresponding to the given string list
	 */
	public static EnumSet<RouteOption> fromList(String routeOptionList) {
		EnumSet<RouteOption> optionSet = EnumSet.noneOf(RouteOption.class);
		String[] routeOptions = routeOptionList.split(",");
		for(String routeOption : routeOptions) {
			if(routeOption != null && !routeOption.isEmpty()) {
				for(RouteOption ro : values()) {
					if(ro.abbr.equalsIgnoreCase(routeOption) || ro.name().equalsIgnoreCase(routeOption)) {
						optionSet.add(ro);
					}
				}
			}
		}
		return optionSet;
	}
	
	@Override
	public String toString() {
		return abbr;
	}

	public static String setToString(Set<RouteOption> enabledOptions) {
		StringBuilder sb = new StringBuilder();
		for(RouteOption option : enabledOptions) {
			sb.append(option.abbr);
			sb.append(",");
		}
		if(sb.length() > 1) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}
}
