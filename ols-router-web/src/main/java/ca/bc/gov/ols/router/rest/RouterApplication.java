/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.RouterFactory;
import ca.bc.gov.ols.router.api.GeometryReprojector;
import ca.bc.gov.ols.router.config.RouterConfig;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { CassandraAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
public class RouterApplication {
	private static final Logger logger = LoggerFactory.getLogger(RouterApplication.class.getCanonicalName());


	private Router router;
	private GeometryReprojector reprojector = new GeotoolsGeometryReprojector();

	public static void main(String[] args) {
		SpringApplication.run(RouterApplication.class, args);
	}

	public RouterApplication(ThreadPoolTaskScheduler taskScheduler) {
		logger.info("RouterApplication() constructor called");
		RouterFactory routerFactory = new RouterFactory();
		routerFactory.setGeometryReprojector(reprojector);
		router = routerFactory.getRouter();
		RouterConfig config = router.getConfig();
		
		taskScheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				logger.info("scheduled job running!");
				router.update();

			}
		}, Instant.now(), Duration.ofSeconds(config.getRdmUpdateInterval()));
	}

	@Bean
	Router router() {
		return router;
	}
	
}
