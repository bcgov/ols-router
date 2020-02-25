/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import gnu.trove.map.hash.THashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class JsonRowReader implements RowReader {
	private static final Logger logger = LoggerFactory.getLogger(JsonRowReader.class.getCanonicalName());

	private JsonReader jsonReader = null;
	private GeometryFactory gf = null;
	private Map<String,Object> curRow = null;
	private int readCount = 0;
	ArrayList<Coordinate> coordBuffer = new ArrayList<Coordinate>(1000);
	private Map<String,String> dates = new HashMap<String,String>();
	
	
	public JsonRowReader(String fileName, GeometryFactory gf) {
		this.gf = gf;
		try {
			construct(new BufferedReader(new FileReader(fileName)));
		} catch(IOException ioe) {
			logger.error("Error opening stream for file {}.", fileName, ioe);
			throw new RuntimeException(ioe);
		}
	}
	
	public JsonRowReader(InputStream inStream, GeometryFactory gf) {
		this.gf = gf;
		construct(new InputStreamReader(inStream));
	}
	
	public JsonRowReader(Reader inReader, GeometryFactory gf) {
		this.gf = gf;
		construct(inReader);
	}


	private void construct(Reader inReader) {
		jsonReader = new JsonReader(inReader);
		try {
			JsonToken tok = jsonReader.peek();
			if(tok == JsonToken.BEGIN_ARRAY) {
				jsonReader.beginArray(); // array of feature types
				jsonReader.beginObject(); // dates feature type
				while(jsonReader.hasNext()) {
					if("features".equals(jsonReader.nextName())) {
						jsonReader.beginArray(); // features
						jsonReader.beginObject(); // feature
						while(jsonReader.hasNext()) {
							if("properties".equals(jsonReader.nextName())) {
								jsonReader.beginObject();
								while(jsonReader.hasNext()) {
									String name = jsonReader.nextName().toUpperCase();
									String date = jsonReader.nextString();
									dates.put(name, date);
								}
								jsonReader.endObject(); // properties
							} else {
								jsonReader.skipValue();
							}
						}
						jsonReader.endObject(); // feature
						jsonReader.endArray(); // features
						break;
					} else {
						jsonReader.skipValue();
					}
				}
				jsonReader.endObject(); // dates feature type
			}
			jsonReader.beginObject(); // actual data feature type
			while(jsonReader.hasNext()) {
				if("features".equals(jsonReader.nextName())) {
					jsonReader.beginArray();
					break;
				} else {
					jsonReader.skipValue();
				}
			}
			readCount = 0;
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	@Override
	public boolean next() {
		curRow = new THashMap<String,Object>();
		try {
			if(jsonReader.hasNext()) {
				jsonReader.beginObject();
				// loop over feature object's variables
				while(jsonReader.hasNext()) {
					switch(jsonReader.nextName()) {
					case "geometry":
						JsonToken tok = jsonReader.peek();
						if(tok.equals(JsonToken.NULL)) {
							jsonReader.nextNull();
							curRow.put("geom", null);
							break;
						}
						jsonReader.beginObject();
						String type = null;
						while(jsonReader.hasNext()) {
							double x;
							double y;
							switch(jsonReader.nextName()) {
							case "type":
								type = jsonReader.nextString();
								break;
							case "coordinates":
								if(type == null) {
									throw new RuntimeException("GeoJSON geometry type must come before coordinates.");
								}
								switch(type) {
								case "Point":
									jsonReader.beginArray();
									x = jsonReader.nextDouble();
									y = jsonReader.nextDouble();
									jsonReader.endArray();
									curRow.put("geom", gf.createPoint(new Coordinate(x,y)));
									break;
								case "LineString":
									coordBuffer.clear();
									jsonReader.beginArray();
									while(jsonReader.hasNext()) {
										jsonReader.beginArray();
										x = jsonReader.nextDouble();
										y = jsonReader.nextDouble();
										coordBuffer.add(new Coordinate(x,y));
										jsonReader.endArray();
									}
									jsonReader.endArray();
									curRow.put("geom", gf.createLineString(coordBuffer.toArray(new Coordinate[coordBuffer.size()])));
									break;
								default:
									throw new RuntimeException("Unsupported GeoJSON geometry type: " + type );
								}
								break;
							default:
								jsonReader.skipValue();
							}
						}
						jsonReader.endObject();
						break;
					case "properties":
						jsonReader.beginObject();
						while(jsonReader.hasNext()) {
							String name = jsonReader.nextName().toLowerCase();
							switch(jsonReader.peek()) {
							case STRING:
								curRow.put(name, jsonReader.nextString());
								break;
							case NUMBER:
								curRow.put(name, new BigDecimal(jsonReader.nextString()));
					            break;
					        case BOOLEAN:
					        	curRow.put(name, jsonReader.nextBoolean());
					            break;
					        case NULL:
					        	jsonReader.nextNull();
					            curRow.put(name, null);
					            break;
							default:
								jsonReader.skipValue();
							}
						}
						jsonReader.endObject();
						break;
					default:
						jsonReader.skipValue();
					}
				}
				jsonReader.endObject();
				readCount++;
				return true;
			} else {
				return false;
			}
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	@Override
	public Object getObject(String column) {
		if(curRow == null) {
			return null;
		}
		return curRow.get(column);
	}
	
	@Override
	public int getInt(String column) {
		if(curRow == null || curRow.get(column) == null) {
			return AbstractBasicRowReader.NULL_INT_VALUE;
		}
		return ((BigDecimal)(curRow.get(column))).intValue();
	}

	@Override
	public int getIntNullValue() {
		return AbstractBasicRowReader.NULL_INT_VALUE;
	}

	@Override
	public Integer getInteger(String column) {
		if(curRow == null || curRow.get(column) == null) {
			return null;
		}
		return ((BigDecimal)(curRow.get(column))).intValue();
	}
	
	@Override
	public double getDouble(String column) {
		if(curRow == null|| curRow.get(column) == null) {
			return Double.NaN;
		}
		return ((BigDecimal)(curRow.get(column))).doubleValue();
	}
	
	@Override
	public String getString(String column) {
		if(curRow == null) {
			return null;
		}
		return (String)(curRow.get(column));
	}
	
	@Override
	public LocalDate getDate(String column) {
		if(curRow == null) {
			return null;
		}
		return (LocalDate)(curRow.get(column));
	}
	
	@Override
	public Point getPoint() {
		if(curRow == null) {
			return null;
		}
		return (Point)(curRow.get("geom"));
	}
	
	@Override
	public Point getPoint(String col) {
		if(curRow == null) {
			return null;
		}
		return (Point)(curRow.get(col));
	}
	
	@Override
	public LineString getLineString() {
		if(curRow == null) {
			return null;
		}
		return (LineString)(curRow.get("geom"));
	}
	
	@Override
	public void close() {
		try {
			logger.info("JsonRowReader closed after reading: {} records", readCount);
			jsonReader.endArray();
			jsonReader.endObject();
			jsonReader.close();
			jsonReader = null;
			gf = null;
			curRow = null;
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}


}
