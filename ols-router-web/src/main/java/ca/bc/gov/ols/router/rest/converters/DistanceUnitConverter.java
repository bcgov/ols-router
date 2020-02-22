/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.converters;

import org.springframework.core.convert.converter.Converter;

import ca.bc.gov.ols.router.data.enums.DistanceUnit;

public class DistanceUnitConverter implements Converter<String, DistanceUnit> {

	@Override
	public DistanceUnit convert(String in) {
		return DistanceUnit.convert(in);
	}
	

}
