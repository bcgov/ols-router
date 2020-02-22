/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.exceptions;

import org.springframework.validation.BindingResult;

public class InvalidParameterException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private ErrorMessage message;
	
	public InvalidParameterException(BindingResult bindingResult) {
		message = new ErrorMessage(bindingResult);
	}
	
	public InvalidParameterException(ErrorMessage message) {
		this.message = message;
	}
	
	public ErrorMessage getErrorMessage() {
		return message;
	}
}
