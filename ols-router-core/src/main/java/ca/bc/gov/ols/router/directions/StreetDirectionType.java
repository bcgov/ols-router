/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.directions;

public enum StreetDirectionType {
	TURN_SLIGHT_LEFT("Turn slight left"),
	TURN_LEFT("Turn left"), 
	TURN_SHARP_LEFT("Turn sharp left"),  
	TURN_SLIGHT_RIGHT("Turn slight right"), 
	TURN_RIGHT("Turn right"), 
	TURN_SHARP_RIGHT("Turn sharp right"), 
	CONTINUE("Continue");
	
	private String wording;
	
	private StreetDirectionType(String wording) {
		this.wording = wording;
	}
	
	@Override
	public String toString() {
		return wording;
	}
	
}
