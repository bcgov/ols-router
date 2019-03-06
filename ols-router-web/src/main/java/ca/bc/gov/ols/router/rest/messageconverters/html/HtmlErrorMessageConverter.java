/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.html;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.stereotype.Component;

import ca.bc.gov.ols.router.rest.exceptions.ErrorMessage;

/**
 * Supports more than just HTML output types, this is the default exception format.
 * 
 * @author chodgson
 * 
 */
@Component
public class HtmlErrorMessageConverter extends AbstractHttpMessageConverter<ErrorMessage> {
	
	public HtmlErrorMessageConverter() {
		super(MediaType.APPLICATION_XHTML_XML,
				MediaType.TEXT_HTML,
				new org.springframework.http.MediaType("text", "csv", Charset.forName("UTF-8")),
				new org.springframework.http.MediaType("application", "gml+xml",
						Charset.forName("UTF-8")),
				MediaType.APPLICATION_JSON,
				new org.springframework.http.MediaType("application", "javascript",
						Charset.forName("UTF-8")),
				new org.springframework.http.MediaType("application", "zip",
						Charset.forName("UTF-8")));
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		return ErrorMessage.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}
	
	@Override
	protected ErrorMessage readInternal(Class<? extends ErrorMessage> clazz,
			HttpInputMessage inputMessage) throws IOException {
		return null;
	}
	
	@Override
	protected void writeInternal(ErrorMessage message, HttpOutputMessage outputMessage)
			throws IOException {
		outputMessage.getHeaders().setContentType(MediaType.TEXT_HTML);
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		out.write("<!DOCTYPE html>\r\n<html>\r\n<head></head>\r\n<body>\r\n");
		
		out.write(message.getMessage());
		out.write("</body>\r\n</html>");
		out.flush();
	}
	
}
