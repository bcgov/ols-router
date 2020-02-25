/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class GeomParseUtil {
	
	public static double[] parseDoubleArray(String in) {
		String[] strings = in.split(",");
		double[] coords = new double[strings.length];
		for(int i = 0; i < strings.length; i++) {
			coords[i] = Double.parseDouble(strings[i]);
		}
		return coords;
	}
	
	public static Polygon buildBbox(double[] coords, GeometryFactory gf) {
		if(coords.length != 4) {
			throw new NumberFormatException();
		}
		LinearRing ring = gf.createLinearRing(new Coordinate[] {
				new Coordinate(coords[0], coords[1]), new Coordinate(coords[0], coords[3]),
				new Coordinate(coords[2], coords[3]), new Coordinate(coords[2], coords[1]),
				new Coordinate(coords[0], coords[1])});
		return gf.createPolygon(ring, null);
	}
	
	public static Polygon parseBbox(String in, GeometryFactory gf) {
		if(in == null) {
			throw new NumberFormatException();
		}
		double[] coords = parseDoubleArray(in);
		return buildBbox(coords, gf);
	}
	
	public static Point parseXy(String in, GeometryFactory gf) {
		if(in == null) {
			throw new NumberFormatException();
		}
		double[] coords = parseDoubleArray(in);
		if(coords.length != 2) {
			throw new NumberFormatException();
		}
		return gf.createPoint(new Coordinate(coords[0], coords[1]));
	}
}
