/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511;

import ca.bc.gov.ols.router.open511.enums.Open511RestrictionType;

public class Restriction {
	private Open511RestrictionType restrictionType;
	private double value;
	
	public Open511RestrictionType getRestrictionType() {
		return restrictionType;
	}
	
	public void setRestrictionType(Open511RestrictionType restrictionType) {
		this.restrictionType = restrictionType;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
}
