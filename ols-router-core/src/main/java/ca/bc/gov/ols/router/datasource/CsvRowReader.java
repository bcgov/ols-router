/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.locationtech.jts.geom.GeometryFactory;

public class CsvRowReader extends XsvRowReader {

	public CsvRowReader(String fileName, GeometryFactory gf) {
		super(gf);
		this.fileName = fileName; 
		try {
			construct(new BufferedReader(new FileReader(fileName)), ',');
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public CsvRowReader(InputStream inStream, GeometryFactory gf) {
		super(gf);
		construct(new InputStreamReader(inStream), ',');
	}
	
	public CsvRowReader(Reader inReader, GeometryFactory gf) {
		super(gf);
		construct(inReader, ',');
	}
	
}