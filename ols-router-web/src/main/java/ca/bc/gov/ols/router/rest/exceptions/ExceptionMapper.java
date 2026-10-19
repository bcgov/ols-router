/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ca.bc.gov.ols.router.rest.AccessLogFilter;

@ControllerAdvice
public class ExceptionMapper {
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorMessage> handleRuntimeException(RuntimeException re,
			HttpServletRequest request) {
		StringWriter sw = new StringWriter();
		re.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		String message = "Unexpected internal problem: " + re.getMessage()
				+ "\n" + exceptionAsString;
		request.setAttribute(AccessLogFilter.ERROR_MESSAGE_ATTR,
				"Unexpected internal problem: " + re.getMessage());
		return new ResponseEntity<ErrorMessage>(new ErrorMessage(message),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException iae,
			HttpServletRequest request) {
		String message = "Invalid parameter: " + iae.getMessage();
		request.setAttribute(AccessLogFilter.ERROR_MESSAGE_ATTR, message);
		return new ResponseEntity<ErrorMessage>(new ErrorMessage(message),
				HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorMessage> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException msrpe, HttpServletRequest request) {
		String message = "Missing parameter " + msrpe.getParameterName() + ": "
				+ msrpe.getMessage();
		request.setAttribute(AccessLogFilter.ERROR_MESSAGE_ATTR, message);
		return new ResponseEntity<ErrorMessage>(new ErrorMessage(message),
				HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(TypeMismatchException.class)
	public ResponseEntity<ErrorMessage> handleTypeMismatchException(
			TypeMismatchException tme, HttpServletRequest request) {
		String message = "Error in parameter " + tme.getPropertyName() + ": "
				+ tme.getMessage();
		request.setAttribute(AccessLogFilter.ERROR_MESSAGE_ATTR, message);
		return new ResponseEntity<ErrorMessage>(new ErrorMessage(message),
				HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(InvalidParameterException.class)
	public ResponseEntity<ErrorMessage> handleInvalidParameterException(
			InvalidParameterException ipe, HttpServletRequest request) {
		request.setAttribute(AccessLogFilter.ERROR_MESSAGE_ATTR, ipe.getErrorMessage().getMessage());
		return new ResponseEntity<ErrorMessage>(ipe.getErrorMessage(),
				HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorMessage> handleNotFoundException(
			NotFoundException nfe, HttpServletRequest request) {
		request.setAttribute(AccessLogFilter.ERROR_MESSAGE_ATTR, nfe.getErrorMessage().getMessage());
		return new ResponseEntity<ErrorMessage>(nfe.getErrorMessage(),
				HttpStatus.NOT_FOUND);
	}
	
}
