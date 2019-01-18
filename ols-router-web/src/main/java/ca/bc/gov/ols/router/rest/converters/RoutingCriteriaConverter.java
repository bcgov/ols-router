/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.converters;

import org.springframework.core.convert.converter.Converter;

import ca.bc.gov.ols.router.data.enums.RoutingCriteria;

public class RoutingCriteriaConverter implements Converter<String, RoutingCriteria> {

	@Override
	public RoutingCriteria convert(String in) {
		return RoutingCriteria.convert(in);
	}
	

}
