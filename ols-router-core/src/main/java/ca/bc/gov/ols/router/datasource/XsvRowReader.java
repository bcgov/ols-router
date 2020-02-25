/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import gnu.trove.map.hash.THashMap;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public abstract class XsvRowReader extends AbstractBasicRowReader implements RowReader {
	private static final Logger logger = LoggerFactory.getLogger(XsvRowReader.class.getCanonicalName());

	public static final String UTF8_BOM = "\uFEFF";
	private String[] nextLine;
	private Map<String,Integer> schema;
	private CSVReader reader;
	protected String fileName;
	private int readCount;

	protected XsvRowReader(GeometryFactory gf) {
		super(gf);
	}

	protected void construct(Reader inReader, char separator) {
		schema = new THashMap<String,Integer>();
		try {
			reader = new CSVReader(inReader, separator);
			String[] header = reader.readNext(); 
			if(header == null) {
				throw new RuntimeException("XSV file empty: " + fileName);
			}
			for(int i = 0; i < header.length; i++) {
				if(i == 0 && header[i].startsWith(UTF8_BOM)) {
					header[i] = header[i].substring(1);
				}
				schema.put(header[i].trim().toLowerCase(),i);
			}
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public boolean next() {
		try {
			nextLine = reader.readNext();
			if(nextLine != null) {
				readCount++;
				return true;
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		return false;
	}

	@Override
	public Object getObject(String column) {
		Integer colNum = schema.get(column.toLowerCase());
		if(colNum == null) {
			return null;
		}
		String val = nullSafeTrim(nextLine[colNum]);
		if(val == null || val.isEmpty()) {
			return null;
		}
		return val;
	}

	@Override
	public Point getPoint(String prefix) {
		Object xObj = getObject(prefix + "x");
		Object yObj = getObject(prefix + "y");
		if(xObj == null || xObj.toString().isEmpty()
				|| yObj == null || yObj.toString().isEmpty()) {
			return null;
		}
		double x = Double.valueOf(xObj.toString());
		double y = Double.valueOf(yObj.toString());
		return gf.createPoint(new Coordinate(x, y));
	}

	private static String nullSafeTrim(String string) {
		if(string == null) {
			return null;
		}
		return string.trim();
	}

	@Override
	public void close() {
		try {
			logger.info("XsvRowReader closed after reading: {} records", readCount);
			reader.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}