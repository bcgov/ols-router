/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class JsonRowWriter implements RowWriter {
	private static final Logger logger = LoggerFactory.getLogger(JsonRowWriter.class.getCanonicalName());
	
	static final DecimalFormat DEGREE_FORMAT = new DecimalFormat("###.#####");
	static final DecimalFormat METRE_FORMAT = new DecimalFormat("###.##");

	
	private JsonWriter jw;
	private BufferedWriter bw;
	private int writeCount = 0;

	public JsonRowWriter(File file, String name) {
		try {
			logger.info("JsonRowWriter opened for file: {}", file.getCanonicalPath());
			bw = new BufferedWriter(new FileWriter(file));
			jw = new JsonWriter(bw);
			jw.beginObject();
			jw.name("name").value(name);
			jw.name("type").value("FeatureCollection");
			jw.name("crs");
				jw.beginObject();
				jw.name("type").value("name");
				jw.name("properties");
					jw.beginObject();
					jw.name("name").value("EPSG:3005");
					jw.endObject();
				jw.endObject();
			jw.name("features");
			jw.beginArray();
			writeCount = 0;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public void writeRow(Map<String, Object> row) {
		try {
			bw.append("\n");
			jw.beginObject();
			jw.name("type").value("Feature");
			if(row.get("geom") != null) {
				jw.name("geometry");
				writeGeometry((Geometry)row.get("geom"));
			}
			jw.name("properties");
			jw.beginObject();
			for(Entry<String, Object> entry: row.entrySet()) {
				String key = entry.getKey();
				Object val = entry.getValue();
				if("geom".equals(key)) {
					continue;
				}
				// skip null fields
				if(val != null) {
					jw.name(key);
					if(val instanceof String) {
						jw.value((String)val);
					} else if(val instanceof Integer) {
						jw.value(((Integer)val).intValue());
					} else if(val instanceof Double) {
						jw.value(((Double)val).doubleValue());
					} else if(val instanceof Boolean) {
						jw.value(((Boolean)val).booleanValue());
					} else {
						jw.value(val.toString());
					}
				}
			}
			jw.endObject();
			jw.endObject();
			writeCount++;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	private void writeGeometry(Geometry g) throws IOException {
		jw.beginObject();
		jw.name("type").value(g.getGeometryType());
		jw.name("coordinates");
		switch(g.getGeometryType()) {
			case "Point":
				coordinate(((Point)g).getX(), ((Point)g).getY());
				break;
			case "LineString":
				coordinates(((LineString)g).getCoordinateSequence());
				break;
			case "Polygon":
				polygon(((Polygon)g)); 
				break;
			case "MultiPolygon":
				multiPolygon(((MultiPolygon)g)); 
				break;
			default: jw.value("Unknown geometry type");
		}
		jw.endObject();

	}

	private void multiPolygon(MultiPolygon mp) throws IOException {
		jw.beginArray();
		for(int i = 0; i < mp.getNumGeometries(); i++) {
			polygon((Polygon)mp.getGeometryN(i));
		}
		jw.endArray();
	}

	private void polygon(Polygon p) throws IOException {
		jw.beginArray();
		coordinates(p.getExteriorRing().getCoordinateSequence());
		for(int i = 0; i < p.getNumInteriorRing(); i++) {
			coordinates(p.getInteriorRingN(i).getCoordinateSequence());
		}
		jw.endArray();
	}

	private void coordinates(CoordinateSequence cs) throws IOException {
		jw.beginArray();
		for(int i = 0; i < cs.size(); i++) {
			coordinate(cs.getX(i), cs.getY(i));
		}
		jw.endArray();		
	}

	private void coordinate(double x, double y) throws IOException {
		jw.beginArray();
		jw.jsonValue(formatOrdinate(x));
		jw.jsonValue(formatOrdinate(y));
		jw.endArray();		
	}

	private static String formatOrdinate(double ord) {
		if(ord <= 180 && ord >= -180) {
			return DEGREE_FORMAT.format(ord);
		}
		return METRE_FORMAT.format(ord);
	}
	
	@Override
	public void close() {
		try {
			logger.info("JsonRowWriter closed after writing: {} records", writeCount);
			jw.endArray();
			jw.endObject();
			jw.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}
