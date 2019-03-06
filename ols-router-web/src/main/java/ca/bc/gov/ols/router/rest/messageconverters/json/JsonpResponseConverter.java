/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.api.ApiResponse;

public class JsonpResponseConverter extends JsonResponseConverter {

	@Autowired
	private Router router;

	public JsonpResponseConverter() {
		super(new MediaType("application", "javascript",
				Charset.forName("UTF-8")));
	}

	@Override
	protected void writeInternal(ApiResponse response,
			HttpOutputMessage outputMessage) throws IOException {
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		JsonpConverterHelper helper = new JsonpConverterHelper(router, out);
		helper.convertResponse(response);
		out.flush();
	}

}
