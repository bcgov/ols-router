/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;


public class XsvRowWriter implements RowWriter  {
	private static final Logger logger = LoggerFactory.getLogger(XsvRowWriter.class.getCanonicalName());

	private CSVWriter csvWriter;
	private List<String> schema;
	private int writeCount = 0;
	private String[] data; // reusable row data storage
	
	public XsvRowWriter(File file, char separator, List<String> schema, boolean quotes) {
		try {
			logger.info("CsvRowWriter opened for file: {}", file.getCanonicalPath());
			if(quotes) {
				csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(file)), separator);
			} else {
				csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(file)), separator, CSVWriter.NO_QUOTE_CHARACTER);
			}
			// write header line
			csvWriter.writeNext(schema.toArray(new String[schema.size()]));
			this.schema = schema;
			writeCount = 0;
			data = new String[schema.size()];
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	@Override
	public void writeRow(Map<String, Object> row) {
		for(int i = 0; i < schema.size(); i++) {
			Object obj = row.get(schema.get(i));
			if(obj == null) {
				data[i] = null;
			} else {
				data[i] = obj.toString();
			}
		}
		csvWriter.writeNext(data);
		writeCount++;
	}

	@Override
	public void close() {
		try {
			logger.info("CsvRowWriter closed after writing: {} records", writeCount);
			csvWriter.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}
