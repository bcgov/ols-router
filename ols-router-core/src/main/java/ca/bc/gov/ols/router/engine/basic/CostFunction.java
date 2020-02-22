/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

@FunctionalInterface
public interface CostFunction<A, B, C, R> {

	R apply(A a, B b, C c);
	
}
