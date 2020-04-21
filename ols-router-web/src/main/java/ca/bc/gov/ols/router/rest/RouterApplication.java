/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.RouterFactory;
import ca.bc.gov.ols.router.api.GeometryReprojector;

@Component
public class RouterApplication {
	private static final Logger logger = LoggerFactory.getLogger(RouterApplication.class.getCanonicalName());
	
	private Router router;
	private GeometryReprojector reprojector = new GeotoolsGeometryReprojector();
	
	public RouterApplication() {
		logger.info("RouterApplication() constructor called");
		RouterFactory routerFactory = new RouterFactory();
		routerFactory.setGeometryReprojector(reprojector);
		router = routerFactory.getRouter();
	}
	
	@Bean
	public Router router() {
		return router;
	}
		
}
