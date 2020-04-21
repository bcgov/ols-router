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

public class TsvRowReader extends XsvRowReader {

	public TsvRowReader(String fileName, GeometryFactory gf) {
		super(gf);
		this.fileName = fileName; 
		try {
			construct(new BufferedReader(new FileReader(fileName)), '\t');
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public TsvRowReader(InputStream inStream, GeometryFactory gf) {
		super(gf);
		construct(new InputStreamReader(inStream), '\t');
	}
	
	public TsvRowReader(Reader inReader, GeometryFactory gf) {
		super(gf);
		construct(inReader, '\t');
	}
	
}