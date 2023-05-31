/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.RouterFactory;
import ca.bc.gov.ols.router.api.GeometryReprojector;

@SpringBootApplication
@EnableAutoConfiguration(exclude={CassandraAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
public class RouterApplication {
	private static final Logger logger = LoggerFactory.getLogger(RouterApplication.class.getCanonicalName());
	
	private Router router;
	private GeometryReprojector reprojector = new GeotoolsGeometryReprojector();
	
	public static void main(String[] args) {
		SpringApplication.run(RouterApplication.class, args);
	}
	
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
