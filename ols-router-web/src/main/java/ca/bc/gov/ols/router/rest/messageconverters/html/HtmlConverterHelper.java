/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.html;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.api.IsochroneResponse;
import ca.bc.gov.ols.router.api.RouterDirectionsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceBetweenPairsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceResponse;
import ca.bc.gov.ols.router.api.RouterOptimizedResponse;
import ca.bc.gov.ols.router.api.RouterRouteResponse;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.directions.Direction;
import ca.bc.gov.ols.router.rest.messageconverters.ConverterHelper;
import ca.bc.gov.ols.router.util.TimeHelper;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class HtmlConverterHelper extends ConverterHelper {
	
	public HtmlConverterHelper(Router router, Writer out) {
		super(router, out);
	}
	
	public void writeHeader(ApiResponse response) throws IOException {
		out.write(("<!DOCTYPE html><html><head><title>Router Response</title></head><body><table>"));
	}

	public void writeFooter(ApiResponse response) throws IOException {
		out.write("</table></body></html>");
	}

	protected void writeField(String fieldName, Object fieldValue) throws IOException {
		writeField(fieldName, fieldName, fieldValue, true);
	}
	
	protected void writeField(String fieldName, Object fieldValue, boolean escape) throws IOException {
		writeField(fieldName, fieldName, fieldValue, escape);
	}

	protected void writeField(String glossaryName, String fieldName, Object fieldValue) throws IOException {
		writeField(glossaryName, fieldName, fieldValue, true);
	}

	protected void writeField(String glossaryName, String fieldName, Object fieldValue, boolean escape) throws IOException {
		writeFieldName(glossaryName, fieldName);
		writeFieldValueHeader();
		out.write(escape ? escape(fieldValue) : fieldValue.toString());
		writeFieldValueFooter();
	}
	
	protected void writeFieldName(String glossaryName, String fieldName) throws IOException {
		out.write("<tr><td class=\"name\"><a href=\"" + config.getGlossaryBaseUrl()
				+ "#" + glossaryName + "\">" + fieldName + ":</a></td>");		
	}

	protected void writeFieldValueHeader() throws IOException {
		out.write("<td class=\"value\">");
	}

	protected void writeFieldValueFooter() throws IOException {
		out.write("</td></tr>\n");
	}

	protected void writeFields(ApiResponse response) throws IOException {
		writeField("routeDescription", response.getRouteDescription());
		writeField("searchTimestamp", response.getTimeStamp());
		writeField("executionTime", response.getExecutionTime());
		if(response instanceof RouterOptimizedResponse) {
			writeField("routingExecutionTime", ((RouterOptimizedResponse)response).getRoutingExecutionTime());
			writeField("optimizationExecutionTime", ((RouterOptimizedResponse)response).getOptimizationExecutionTime());
		}
		writeField("version", RouterConfig.VERSION);
		writeField("disclaimer", config.getDisclaimer());
		writeField("privacyStatement", config.getPrivacyStatement());
		writeField("copyrightNotice", config.getCopyrightNotice());
		writeField("copyrightLicense", config.getCopyrightLicense());
		writeField("srsCode", response.getSrsCode());
		writeField("criteria", response.getCriteria().toString());		
		writeField("distanceUnit", response.getDistanceUnit().abbr());
	}
	
	protected void writeFields(RouterDistanceResponse response) throws IOException {
		writeFields((ApiResponse)response);
		writeField("points", buildPointListTable(response.getPoints()), false);
		writeField("routeFound", response.isRouteFound());
		writeField("distance", response.getDistanceStr());
		writeField("time", response.getTime());
		writeField("timeText", TimeHelper.formatTime(response.getTime()));
	}

	protected void writeFields(RouterDistanceBetweenPairsResponse response) throws IOException {
		writeFields((ApiResponse)response);
		writeField("fromPoints", buildPointListTable(response.getFromPoints()), false);
		writeField("toPoints", buildPointListTable(response.getToPoints()), false);
		writeFieldName("pairs", "pairs");
		writeFieldValueHeader();
		boolean skipFails = response.getMaxPairs() < Integer.MAX_VALUE;
		int curResult = 0;
		for(int from = 0; from < response.getFromPoints().size(); from++) {
			for( int to = 0; to < response.getToPoints().size(); to++) {
				// if we are skipping fails, only output non-fails
				if(!skipFails || response.getError(curResult) == null) {
					out.write("<table>");
					writeField("fromElement", from);
					writeField("toElement", to);
					writeField("distance", response.getDistanceStr(curResult));
					writeField("time", response.getTime(curResult));
					writeField("timeText", TimeHelper.formatTime(response.getTime(curResult)));
					String message = response.getError(curResult);
					if(message != null) {
						writeField("message", message);
					}
					out.write("</table>");
				}
				curResult++;
			}
		}
		writeFieldValueFooter();
	}

	protected void writeFields(RouterRouteResponse response) throws IOException {
		writeFields((RouterDistanceResponse)response);
		LineString ls = response.getPath();
		StringBuilder routeStr = new StringBuilder();
		if(ls != null) {
			routeStr.append("<table>");
			CoordinateSequence coords = ls.getCoordinateSequence();
			for(int i = 0; i < coords.size(); i++) {
				routeStr.append("<tr><td>" + formatOrdinate(coords.getX(i)) + "</td><td>" + formatOrdinate(coords.getY(i)) + "</td></tr>");
			}
			routeStr.append("</table>");
		}
		writeField("route", routeStr, false);		
	}
	
	protected void writeFields(RouterDirectionsResponse response) throws IOException {
		writeFields((RouterRouteResponse)response);
		StringBuilder dirStr = new StringBuilder("<table>");
		for(Direction direction : response.getDirections()) {
			dirStr.append("<tr><td>" + escape(direction.format(response)) + "</td></tr>");
		}
		dirStr.append("</table>");
		writeField("directions", dirStr, false);		
	}
	
	protected void writeFields(IsochroneResponse response) throws IOException {
		writeFields((ApiResponse)response);
		StringBuilder dirStr = new StringBuilder("<table>");
		for(Geometry g : response.getPolygons()) {
			//TODO
		}
		dirStr.append("</table>");
		writeField("directions", dirStr, false);		
	}

	static String buildPointListTable(List<Point> points) {
		StringBuilder pointStr = new StringBuilder("<table>");
		for(Point p : points) {
			pointStr.append("<tr><td>" + formatOrdinate(p.getX()) + "</td><td>" + formatOrdinate(p.getY()) + "</td></tr>");
		}
		pointStr.append("</table>");
		return pointStr.toString();
	}
	
	/**
	 * Escapes a single value
	 * 
	 * @param field the value to escape
	 */
	static String escape(Object field)
	{
		if(field == null) {
			return "";
		}
		return StringEscapeUtils.escapeXml10(field.toString());
	}

}
