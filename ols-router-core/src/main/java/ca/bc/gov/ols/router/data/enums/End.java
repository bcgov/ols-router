/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enums;

/**
 * The possible ends of a street.
 */
public enum End {
	FROM, TO;
	
	public static End convert(String s) {
		char c = s.charAt(0);
		switch(c) {
		case 'F':
		case 'f':
			return FROM;
		case 'T':
		case 't':
			return TO;
		default:
			throw new IllegalArgumentException("Invalid End) value: '" + c
					+ "' (must be one of 'F', 'f', 'T', 't')");
		}
	}
	
	public End opposite() {
		switch(this) {
		case FROM:
			return TO;
		case TO:
			return FROM;
		}
		return null;
	}
}