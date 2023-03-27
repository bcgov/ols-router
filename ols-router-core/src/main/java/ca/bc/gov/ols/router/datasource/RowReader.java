/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import java.time.LocalDate;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

/**
 * RowReader defines a generic interface similar to JDBC ResultSet, allowing for compatibility with
 * other tabular data sources.
 * 
 * @author chodgson@refractions.net
 */
public interface RowReader {
	
	/**
	 * Increments the row pointer to the next row of data.
	 * 
	 * @return false if there are no more rows of data available, true otherwise
	 */
	public boolean next();
	
	/**
	 * Returns the value of the specified column as an Object.
	 * 
	 * @param column the name of the column whose value to return
	 * @return the value of the specified column
	 */
	public Object getObject(String column);
	
	/**
	 * Returns the value of the specified column as an int.
	 * 
	 * @param column the name of the column whose value to return
	 * @return the value of the specified column
	 */
	public int getInt(String column);
	
	/**
	 * Returns the value that would be returned by getInt() if the underlying value is null.
	 * 
	 * @return the value that would be returned by getInt() if the underlying value is null
	 */
	public int getIntNullValue();
	
	/**
	 * Returns the value of the specified column as an Integer.
	 * 
	 * @param column the name of the column whose value to return
	 * @return the value of the specified column
	 */
	public Integer getInteger(String column);
	
	/**
	 * Returns the value of the specified column as a double.
	 * 
	 * @param column the name of the column whose value to return
	 * @return the value of the specified column
	 */
	public double getDouble(String column);
	
	/**
	 * Returns the value of the specified column as a String.
	 * 
	 * @param column the name of the column whose value to return
	 * @return the value of the specified column
	 */
	public String getString(String column);
	
	/**
	 * Returns the value of the specified column as a Date.
	 * 
	 * @param column the name of the column whose value to return
	 * @return the value of the specified column
	 */
	public LocalDate getDate(String column);
	
	/**
	 * Returns a Point object for the default location represented by this row of data.
	 * 
	 * @return the Point location of this row of data
	 */
	public Point getPoint();
	
	/**
	 * Returns a Point object for the named column.
	 * 
	 * @param column the name of the column to get the point from.
	 * @return the point object from the named column
	 */
	Point getPoint(String column);
	
	/**
	 * Returns a LineString object for the linear geometry represented by this row of data. Uses the
	 * default column name "wkt" to reference the geometry column.
	 * 
	 * @return the linear geometry represented by this row of data
	 */
	public LineString getLineString();
	
	/**
	 * Closes this RowReader, releasing any resources (file handles, database handles/connections,
	 * etc.) associated with it.
	 */
	public void close();
	
}
