/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.converters;

import org.springframework.core.convert.converter.Converter;

public class BooleanConverter implements Converter<String, Boolean> {
	
	@Override
	public Boolean convert(String in) {
		if(in == null || in.isEmpty()) {
			return null;
		} else if("0".equals(in)
				|| "n".equalsIgnoreCase(in)
				|| "no".equalsIgnoreCase(in)
				|| "f".equalsIgnoreCase(in)
				|| "false".equalsIgnoreCase(in)) {
			return Boolean.FALSE;
		} else if("1".equals(in)
				|| "y".equalsIgnoreCase(in)
				|| "yes".equalsIgnoreCase(in)
				|| "t".equalsIgnoreCase(in)
				|| "true".equalsIgnoreCase(in)) {
			return Boolean.TRUE;
		} else {
			throw new IllegalArgumentException(
					"Parameter must be one of 1, 0, y, n, yes, no, t, f, true, false");
		}
	}
	
}
