/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasources;

import java.time.LocalDate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public abstract class AbstractBasicRowReader implements RowReader {
	
	protected GeometryFactory gf;
	private WKTReader wktReader;
	
	public AbstractBasicRowReader(GeometryFactory gf) {
		this.gf = gf;
		wktReader = new WKTReader(gf);
	}
	
	@Override
	public abstract Object getObject(String column);
	
	@Override
	public int getInt(String column) {
		Object result = getObject(column);
		if(result == null) {
			return 0;
		}
		return Integer.valueOf(result.toString());
	}
	
	@Override
	public Integer getInteger(String column) {
		Object result = getObject(column);
		if(result == null || result.toString().isEmpty()) {
			return null;
		}
		return Integer.valueOf(result.toString());
	}
	
	@Override
	public double getDouble(String column) {
		Object result = getObject(column);
		if(result == null) {
			return Double.NaN;
		}
		return Double.valueOf(result.toString());
	}
	
	@Override
	public String getString(String column) {
		Object result = getObject(column);
		if(result == null) {
			return null;
		}
		return result.toString();
	}
	
	@Override
	public LocalDate getDate(String column) {
		Object result = getObject(column);
		if(result == null) {
			return null;
		}
		return LocalDate.parse(result.toString());
	}
	
	@Override
	public Point getPoint() {
		Object xObj = getObject("x");
		Object yObj = getObject("y");
		if(xObj == null || yObj == null) {
			return null;
		}
		double x = Double.valueOf(xObj.toString());
		double y = Double.valueOf(yObj.toString());
		return gf.createPoint(new Coordinate(x, y));
	}
	
	@Override
	public Point getPoint(String column) {
		Object result = getObject(column);
		if(result == null) {
			return null;
		}
		try {
			Point p = (Point)wktReader.read(result.toString());
			return p;
		} catch(ParseException pe) {
			throw new RuntimeException("ParseException while parsing point WKT: '" + result + "'",
					pe);
		}
		
	}
	
	@Override
	public LineString getLineString() {
		Object result = getObject("wkt");
		if(result == null) {
			return null;
		}
		try {
			LineString ls = (LineString)wktReader.read(result.toString());
			return ls;
		} catch(ParseException pe) {
			throw new RuntimeException("ParseException while parsing LineString WKB", pe);
		}
	}
	
	
}