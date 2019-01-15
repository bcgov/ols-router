/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.exceptions;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class ErrorMessage {
	
	private String message;
	
	public ErrorMessage(String message) {
		this.message = message;
	}
	
	public ErrorMessage(BindingResult bindingResult) {
		StringBuilder msg = new StringBuilder(
				"Invalid parameter values for the following parameters:");
		for(ObjectError error : bindingResult.getAllErrors()) {
			if(error instanceof FieldError) {
				FieldError fe = (FieldError)error;
				msg.append("<br>Parameter '" + fe.getField()
						+ "', value '" + fe.getRejectedValue() + "'");
			} else {
				msg.append("<br>" + error.getDefaultMessage());
			}
		}
		message = msg.toString();
	}
	
	public String getMessage() {
		return message;
	}
}
