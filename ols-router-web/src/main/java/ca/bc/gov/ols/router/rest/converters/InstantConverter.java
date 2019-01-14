/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.converters;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;

public class InstantConverter implements Converter<String, Instant> {
	
	@Override
	public Instant convert(String in) {
		if(in == null || in.isEmpty()) {
			return null;
		}
		return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(in));
	}

}
