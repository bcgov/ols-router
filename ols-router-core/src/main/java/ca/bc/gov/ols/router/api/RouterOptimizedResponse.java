/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

public interface RouterOptimizedResponse {
	public long getRoutingExecutionTime();
	public long getOptimizationExecutionTime();
	public int[] getVisitOrder();
}
