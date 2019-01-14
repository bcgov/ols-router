/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.json;

import java.io.IOException;
import java.io.Writer;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.api.ApiResponse;

public class JsonpConverterHelper extends JsonConverterHelper {
	
	public JsonpConverterHelper(Router router, Writer out) {
		super(router, out);
	}

	protected void writeHeader(ApiResponse response) throws IOException {
		out.write(response.getCallback() + "({");
	}
	
	protected void writeFooter(ApiResponse response) throws IOException {
		out.write("});");
	}

}
