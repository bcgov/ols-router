/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.kml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.rest.exceptions.ErrorMessage;

@Component
public class KmlErrorMessageConverter extends AbstractHttpMessageConverter<ErrorMessage> {
	
	@Autowired
	private Router router;
	
	public KmlErrorMessageConverter() {
		super(new MediaType("application", "vnd.google-earth.kml+xml",
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
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		RouterConfig config = router.getConfig();
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" "
				+ "xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\r\n");
		
		out.write(errorMessageToKML(message, config));
		out.write("</kml>");
		//out.flush();
	}
	
	static String errorMessageToKML(ErrorMessage message, RouterConfig config) {
		return "<Document>\r\n"
				+ "<name>Error Message</name>\r\n"
				+ "<open>1</open>\r\n"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"errorMessage\"><value>" + message.getMessage()
				+ "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ "<styleUrl>" + config.getKmlStylesUrl()
				+ "#error_message</styleUrl>\r\n"
				+ "</Document>\r\n";
	}
	
}
