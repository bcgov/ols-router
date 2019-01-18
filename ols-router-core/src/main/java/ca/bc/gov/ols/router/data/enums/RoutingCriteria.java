/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

public enum RoutingCriteria {
	FASTEST("fastest"),
	SHORTEST("shortest");
	
	private String label;
	
	private RoutingCriteria(String label) {
		this.label = label;
	}
	
	public static RoutingCriteria convert(String name) {
		for(RoutingCriteria rc : values()) {
			if(rc.label.equalsIgnoreCase(name)) {
				return rc;
			}
		}
		throw new IllegalArgumentException("Invalid RoutingCriteria value: '"
				+ name + "'.");
	}
	
	public String toString() {
		return label;
	}}
