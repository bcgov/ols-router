/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Custom OperationCustomizer that removes JTS geometry internal fields
 * from the expanded @ParameterObject parameters in the OpenAPI spec.
 */
@Component
public class JtsFieldFilterCustomizer implements OperationCustomizer {

	private static final Set<String> JTS_FIELD_PREFIXES = Set.of(
		"pointPoint.", "pointPoints.", "pointFromPoints.", "pointToPoints."
	);

	@Override
	public Operation customize(Operation operation, HandlerMethod handlerMethod) {
		List<Parameter> params = operation.getParameters();
		if(params != null) {
			List<Parameter> filtered = new ArrayList<>();
			for(Parameter p : params) {
				String name = p.getName();
				boolean keep = true;
				for(String prefix : JTS_FIELD_PREFIXES) {
					if(name.startsWith(prefix)) {
						keep = false;
						break;
					}
				}
				if(keep) {
					filtered.add(p);
				}
			}
			operation.setParameters(filtered);
		}
		return operation;
	}
}