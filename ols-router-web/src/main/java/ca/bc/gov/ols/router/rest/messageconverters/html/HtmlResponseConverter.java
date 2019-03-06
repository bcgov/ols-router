/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.html;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.api.RouterDirectionsResponse;

public class HtmlResponseConverter extends AbstractHttpMessageConverter<ApiResponse> {

	@Autowired
	private Router router;

	public HtmlResponseConverter() {
		super(MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return ApiResponse.class.isAssignableFrom(clazz);
	}

	@Override
	protected RouterDirectionsResponse readInternal(
			Class<? extends ApiResponse> clazz,
			HttpInputMessage inputMessage) throws IOException {
		return null;
	}

	@Override
	protected void writeInternal(ApiResponse response,
			HttpOutputMessage outputMessage) throws IOException {
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		HtmlConverterHelper helper = new HtmlConverterHelper(router, out);
		helper.convertResponse(response);
		out.flush();
	}

}
