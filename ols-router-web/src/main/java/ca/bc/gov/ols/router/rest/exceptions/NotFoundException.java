/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.exceptions;

public class NotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private ErrorMessage errorMessage;
	
	public NotFoundException(String message) {
		errorMessage = new ErrorMessage(message);
	}
	
	public ErrorMessage getErrorMessage() {
		return errorMessage;
	}
	
}
