/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	
	private static final String CSP_POLICY = "script-src 'self' 'unsafe-eval'";
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
        	.authorizeHttpRequests((authorizeHttpRequests) ->
        		authorizeHttpRequests.anyRequest().permitAll()
        	);

        //.headers().and().contentSecurityPolicy(CSP_POLICY);
			//.addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy", CSP_POLICY))
			//.and().requestMatchers("/**");
        return http.build();
    }
	
}
