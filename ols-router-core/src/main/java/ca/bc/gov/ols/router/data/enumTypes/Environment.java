/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data.enumTypes;

public enum Environment {
	DEVEL, DELIV, TEST, PROD;
	
	/**
	 * Converts from a string representation of the Environment value to the Environment object.
	 * 
	 * @param environment the string representation of the Environment
	 * @return the Environment object corresponding to the given string representation
	 */
	public static Environment convert(String environment) {
		for(Environment env : values()) {
			if(env.toString().equalsIgnoreCase(environment)) {
				return env;
			}
		}
		return null;
	}
	
}