/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enumTypes;

/**
 * The possible addressing schemes (aka. parity)
 */
public enum AddressScheme {
	EVEN, ODD, CONTINUOUS, SINGLE;
	public static AddressScheme convert(char c) {
		switch(c) {
		case 'E':
			return EVEN;
		case 'O':
			return ODD;
		case 'C':
			return CONTINUOUS;
		case 'S':
			return SINGLE;
		}
		throw new IllegalArgumentException("Invalid address scheme (aka. parity) value: '" + c
				+ "' (must be one of 'E', 'O', 'C', 'S')");
	}
	
	public static String parityToString(AddressScheme scheme) {
		if(AddressScheme.EVEN.equals(scheme)) {
			return "E";
		} else if(AddressScheme.ODD.equals(scheme)) {
			return "O";
		} else if(AddressScheme.CONTINUOUS.equals(scheme)) {
			return "C";
		} else if(AddressScheme.SINGLE.equals(scheme)) {
			return "S";
		}
		return null;
	}
	
	public boolean includes(int i) {
		switch(this) {
		case EVEN:
			return i % 2 == 0;
		case ODD:
			return i % 2 != 0;
		default:
			return true;
		}
	}
	
}