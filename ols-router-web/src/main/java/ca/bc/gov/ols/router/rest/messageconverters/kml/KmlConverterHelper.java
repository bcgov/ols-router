/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.kml;

import java.io.IOException;
import java.io.Writer;

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
import org.locationtech.jts.geom.Point;

public class KmlConverterHelper extends ConverterHelper {

	public KmlConverterHelper(Router router, Writer out) {
		super(router, out);
	}
	
	protected void writeHeader(ApiResponse response) throws IOException {
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" "
				+ "xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\r\n"
				+ "<Document>\r\n");		
	}

	protected void writeFooter(ApiResponse response) throws IOException {
		out.write("</Document>\r\n</kml>");		
	}

	protected void writePoint(String name, String style, Point point) throws IOException {
		out.write("<Placemark>\r\n"
		  	+ "<open>1</open>\r\n"
		    + "<name>" + escape(name) + "</name>\r\n"
		    + "<styleUrl>" + config.getKmlStylesUrl() + style + "</styleUrl>\r\n"
			+ "<Point>\r\n"
			+ "<gx:drawOrder>1</gx:drawOrder>\r\n"
			+ "<coordinates>" + formatOrdinate(point.getX()) +"," + formatOrdinate(point.getY()) + "</coordinates>\r\n"
			+ "</Point>\r\n"
			+ "</Placemark>\r\n");
	}

	protected void writeFields(RouterDistanceResponse response) throws IOException {
		out.write("<name>Routing Results</name>\r\n"
				+ "<open>1</open>\r\n"
				+ "<Snippet maxLines=\"2\">\r\n"
				+ escape(response.getRouteDescription())
				+ "</Snippet>"
				+ "<ExtendedData>\r\n"
				+ "<Data name=\"routeDescription\"><value>" 
				+ escape(response.getRouteDescription())
				+ "</value></Data>\r\n"
				+ "<Data name=\"searchTimestamp\"><value>" 
				+ escape(response.getTimeStamp())
				+ "</value></Data>\r\n"
				+ "<Data name=\"executionTime\"><value>"
				+ escape(response.getExecutionTime())
				+ "</value></Data>\r\n");
		if(response instanceof RouterOptimizedResponse) {
			out.write("<Data name=\"routingExecutionTime\"><value>"
					+ escape(((RouterOptimizedResponse)response).getRoutingExecutionTime())
					+ "</value></Data>\r\n"
					+"<Data name=\"optimizationExecutionTime\"><value>"
					+ escape(((RouterOptimizedResponse)response).getOptimizationExecutionTime())
					+ "</value></Data>\r\n");
		}
		out.write("<Data name=\"version\"><value>"
				+ escape(RouterConfig.VERSION)
				+ "</value></Data>\r\n"
				+ "<Data name=\"disclaimer\"><value>" 
				+ escape(config.getDisclaimer())
				+ "</value></Data>\r\n"
				+ "<Data name=\"privacyStatement\"><value>" 
				+ escape(config.getPrivacyStatement())
				+ "</value></Data>\r\n"
				+ "<Data name=\"copyrightNotice\"><value>" 
				+ escape(config.getCopyrightNotice())
				+ "</value></Data>\r\n"
				+ "<Data name=\"copyrightLicense\"><value>" 
				+ escape(config.getCopyrightLicense())
				+ "</value></Data>\r\n"
				+ "<Data name=\"criteria\"><value>"
				+ escape(response.getCriteria().toString())
				+ "</value></Data>\r\n"
				+ "<Data name=\"routeFound\"><value>"
				+ escape(response.isRouteFound())
				+ "</value></Data>\r\n"
				+ "<Data name=\"distance\"><value>"
				+ escape(response.getDistanceStr())
				+ "</value></Data>\r\n"
				+ "<Data name=\"distanceUnit\"><value>"
				+ escape(response.getDistanceUnit().abbr())
				+ "</value></Data>\r\n"
				+ "<Data name=\"time\"><value>"
				+ escape(response.getTime())
				+ "</value></Data>\r\n"
				+ "<Data name=\"timeText\"><value>" 
				+ escape(TimeHelper.formatTime(response.getTime())) + "</value></Data>\r\n"
				+ "</ExtendedData>\r\n"
				+ "<styleUrl>"
				+ config.getKmlStylesUrl() + "#route_results</styleUrl>\r\n");
		
		for(int i = 0; i < response.getPoints().size(); i++) {
			if(i == 0) {
				writePoint("Start Point", "#startPoint", response.getPoints().get(i));
			} else {
				writePoint("Stop " + i, "#stop_" + i, response.getPoints().get(i));
			}
		}
	}
	
	protected void writeFields(RouterDistanceBetweenPairsResponse response) throws IOException {
		throw new RuntimeException("KML output not supported for RouterDistanceBetweenPairsResponse.");
	}
	
	protected void writeFields(RouterRouteResponse response) throws IOException {
		writeFields((RouterDistanceResponse)response);
		out.write("<Placemark>\r\n"
				+ "<name>route</name>\r\n"
				+ "<Snippet maxLines=\"0\"></Snippet>\r\n"
				+ "<styleUrl>"
				+ config.getKmlStylesUrl() + "#route</styleUrl>\r\n"
				+ "<LineString>\r\n"
				+ "<coordinates>\r\n"
				);
		CoordinateSequence coords = gr.reproject(response.getPath(), response.getSrsCode()).getCoordinateSequence();
		for(int i = 0; i < coords.size(); i++) {
			out.write(formatOrdinate(coords.getX(i)) + "," + formatOrdinate(coords.getY(i)) + " ");
		}
		out.write("</coordinates>\r\n"
				+ "</LineString>\r\n"
				+ "</Placemark>\r\n");
	}

	protected void writeFields(RouterDirectionsResponse response) throws IOException {
		writeFields((RouterRouteResponse)response);
		out.write("<Folder>\r\n"
				+ "<name>Directions</name>\r\n"
				+ "<Snippet maxLines=\"0\"></Snippet>"
				+ "<styleUrl>" + config.getKmlStylesUrl() + "#directions</styleUrl>\r\n"
				+ "<description>\r\n"
				+ "<![CDATA[\r\n"
				+ "<table>\r\n");
		for(Direction direction : response.getDirections()) {
			out.write("<tr><td>" + escape(direction.format(response)) + "</td></tr>\r\n");
		}
		out.write("</table>\r\n"
				+ "]]>"
				+ "</description>\r\n"
				+ "</Folder>\r\n");
	}

	@Override
	protected void writeFields(IsochroneResponse response) throws IOException {
		// TODO Auto-generated method stub	
	}

	static String escape(Object field) {
		if(field == null) {
			return "";
		}
		return StringEscapeUtils.escapeXml10(field.toString());
	}

}
