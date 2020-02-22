/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.onebusaway.csv_entities.CsvInputSource;

public class UrlCsvInputSource implements CsvInputSource {

	private String baseUrlString;

	public UrlCsvInputSource(String baseUrlString) {
		this.baseUrlString = baseUrlString;
	}

	public boolean hasResource(String name) throws IOException {
		try (InputStream stream = getResource(name)) {
		} catch(IOException ioe) {
			return false;
		}
		return true;
	}

	public InputStream getResource(String name) throws IOException {
		String fileUrlString = baseUrlString + name;
		if(fileUrlString.startsWith("file:")) {
			return new FileInputStream(new File(fileUrlString.substring(5)));
		}
		URL fileUrl = new URL(fileUrlString);
		return fileUrl.openStream();		
	}

	public void close() throws IOException {
		// TODO: not sure how to implement this; may not be required
	}
	
}
