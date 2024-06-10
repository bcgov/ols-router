/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.kml;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.text.StringEscapeUtils;

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

	protected void writeField(String fieldName, Object fieldValue) throws IOException {
		out.write("<Data name=\"" + fieldName + "\"><value>" + escape(fieldValue) + "</value></Data>\r\n");
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
				+ "<ExtendedData>\r\n");
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
		writeField("criteria", response.getCriteria().toString());
		writeField("dataProcessingTimestamp", String.valueOf(response.getDataProcessingTimestamp()));
		writeField("roadNetworkTimestamp", String.valueOf(response.getRoadNetworkTimestamp()));
		writeField("routeFound", response.isRouteFound());
		writeField("distance", response.getDistanceStr());
		writeField("distanceUnit", response.getDistanceUnit().abbr());
		writeField("time", response.getTime());
		writeField("timeText", TimeHelper.formatTime(response.getTime()));
		writeField("departure", String.valueOf(response.getDeparture()));
		writeField("correctSide", response.isCorrectSide());
		writeField("vehicleType", String.valueOf(response.getVehicleType()));
		writeField("followTruckRoute", response.isFollowTruckRoute());
		writeField("truckRouteMultiplier", response.getTruckRouteMultiplier());
		writeField("xingCost", response.getXingCostString());
		writeField("turnCost", response.getTurnCostString());
		writeField("globalDistortionField", formatMap(response.getGlobalDistortionField().toMap(response.getVehicleType())));
		writeField("snapDistance", response.getSnapDistance());
		writeField("simplifyDirections", response.isSimplifyDirections());
		writeField("simplifyThreshold", response.getSimplifyThreshold());
		writeField("restrictionSource", String.valueOf(response.getRestrictionSource()));
		writeField("restrictionValues", formatMap(response.getRestrictionValues()));
		writeField("excludeRestrictions", String.valueOf(response.getExcludeRestrictions()));

		out.write("</ExtendedData>\r\n"
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
