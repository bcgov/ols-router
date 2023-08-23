/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ca.bc.gov.ols.router.rest.converters.BooleanConverter;
import ca.bc.gov.ols.router.rest.converters.DistanceUnitConverter;
import ca.bc.gov.ols.router.rest.converters.InstantConverter;
import ca.bc.gov.ols.router.rest.converters.RoutingCriteriaConverter;
import ca.bc.gov.ols.router.rest.messageconverters.html.HtmlErrorMessageConverter;
import ca.bc.gov.ols.router.rest.messageconverters.html.HtmlResponseConverter;
import ca.bc.gov.ols.router.rest.messageconverters.json.JsonDefaultsResponseConverter;
import ca.bc.gov.ols.router.rest.messageconverters.json.JsonNavInfoResponseConverter;
import ca.bc.gov.ols.router.rest.messageconverters.json.JsonResponseConverter;
import ca.bc.gov.ols.router.rest.messageconverters.json.JsonpResponseConverter;
import ca.bc.gov.ols.router.rest.messageconverters.kml.KmlErrorMessageConverter;
import ca.bc.gov.ols.router.rest.messageconverters.kml.KmlResponseConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private HtmlErrorMessageConverter htmlErrorMessageConverter;
	
	@Autowired
	private KmlErrorMessageConverter kmlErrorMessageConverter;
	
	@Autowired
	private HtmlResponseConverter htmlResponseConverter;

	@Autowired
	private JsonResponseConverter jsonResponseConverter;

	@Autowired
	private JsonpResponseConverter jsonpResponseConverter;

	@Autowired
	private KmlResponseConverter kmlResponseConverter;
	
	@Autowired
	private JsonNavInfoResponseConverter jsonNavInfoResponseConverter;

	@Autowired
	private JsonDefaultsResponseConverter jsonDefaultsResponseConverter;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(htmlErrorMessageConverter);
		converters.add(kmlErrorMessageConverter);
		converters.add(htmlResponseConverter);
		converters.add(jsonResponseConverter);
		converters.add(jsonpResponseConverter);
		converters.add(kmlResponseConverter);
		converters.add(jsonNavInfoResponseConverter);
		converters.add(jsonDefaultsResponseConverter);
	}	

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new BooleanConverter());
		registry.addConverter(new RoutingCriteriaConverter());
		registry.addConverter(new DistanceUnitConverter());
		registry.addConverter(new InstantConverter());
	}
	
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer
				.favorPathExtension(true)
				.favorParameter(false)
				.useRegisteredExtensionsOnly(true)
				.defaultContentType(MediaType.APPLICATION_XHTML_XML)
				.mediaType("xhtml", MediaType.APPLICATION_XHTML_XML)
				.mediaType("html", MediaType.TEXT_HTML)
				.mediaType("json", MediaType.APPLICATION_JSON)
				.mediaType("geojson", new MediaType("application", "vnd.geo+json",
						Charset.forName("UTF-8")))
				.mediaType("jsonp",
						new MediaType("application", "javascript", Charset.forName("UTF-8")))
				.mediaType("geojsonp",
						new MediaType("application", "javascript", Charset.forName("UTF-8")))
				.mediaType("kml", new MediaType("application", "vnd.google-earth.kml+xml",
						Charset.forName("UTF-8")));
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(true);
	}
}
