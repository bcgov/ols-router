/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.google.gson.stream.JsonWriter;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.api.IsochroneResponse;
import ca.bc.gov.ols.router.api.RouterDirectionsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceBetweenPairsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceResponse;
import ca.bc.gov.ols.router.api.RouterOptimizedResponse;
import ca.bc.gov.ols.router.api.RouterRouteResponse;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.enums.DistanceUnit;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.directions.AbstractTravelDirection;
import ca.bc.gov.ols.router.directions.Direction;
import ca.bc.gov.ols.router.directions.LaneRequirement;
import ca.bc.gov.ols.router.directions.Partition;
import ca.bc.gov.ols.router.directions.StartDirection;
import ca.bc.gov.ols.router.engine.basic.Attribute;
import ca.bc.gov.ols.router.notifications.Notification;
import ca.bc.gov.ols.router.rest.messageconverters.ConverterHelper;
import ca.bc.gov.ols.router.util.TimeHelper;

public class JsonConverterHelper extends ConverterHelper {

	private JsonWriter jw;

	public JsonConverterHelper(Router router, Writer out) {
		super(router, out);
		jw = new JsonWriter(out);
	}
	
	protected void writeHeader(ApiResponse response) throws IOException {
		jw.beginObject();
	}
	
	protected void writeFooter(ApiResponse response) throws IOException {
		jw.endObject();
	}
	
	protected void writeFields(ApiResponse response) throws IOException {
		jw.name("routeDescription").value(response.getRouteDescription());
		jw.name("searchTimestamp").value(String.valueOf(response.getTimeStamp()));
		jw.name("executionTime").value(response.getExecutionTime());
		if(response instanceof RouterOptimizedResponse) {
			jw.name("routingExecutionTime").value(((RouterOptimizedResponse)response).getRoutingExecutionTime());
			jw.name("optimizationExecutionTime").value(((RouterOptimizedResponse)response).getOptimizationExecutionTime());
		}
		jw.name("version").value(RouterConfig.VERSION);
		jw.name("dataProcessingTimestamp").value(String.valueOf(response.getDataProcessingTimestamp()));
		jw.name("roadNetworkTimestamp").value(String.valueOf(response.getRoadNetworkTimestamp()));
		jw.name("disclaimer").value(config.getDisclaimer());
		jw.name("privacyStatement").value(config.getPrivacyStatement());
		jw.name("copyrightNotice").value(config.getCopyrightNotice());
		jw.name("copyrightLicense").value(config.getCopyrightLicense());
		jw.name("srsCode").value(response.getSrsCode());
		jw.name("criteria").value(response.getCriteria().toString());
		jw.name("enable").value(RouteOption.setToString(response.getEnabledOptions()));
		jw.name("distanceUnit").value(response.getDistanceUnit().abbr());
		jw.name("departure").value(String.valueOf(response.getDeparture()));
		jw.name("correctSide").value(response.isCorrectSide());
		jw.name("vehicleType").value(String.valueOf(response.getVehicleType()));
		jw.name("followTruckRoute").value(response.isFollowTruckRoute());
		jw.name("truckRouteMultiplier").value(response.getTruckRouteMultiplier());
		jw.name("xingCost").value(response.getXingCostString());
		jw.name("turnCost").value(response.getTurnCostString());
		jw.name("globalDistortionField");
		outputMap(jw, response.getGlobalDistortionField().toMap(response.getVehicleType()));
		//response.getGlobalDistortionField().toJson(jw, response.getVehicleType());
		jw.name("snapDistance").value(response.getSnapDistance());
		jw.name("simplifyDirections").value(response.isSimplifyDirections());
		jw.name("simplifyThreshold").value(response.getSimplifyThreshold());
		jw.name("restrictionSource").value(String.valueOf(response.getRestrictionSource()));
		jw.name("listRestrictions").value(response.isListRestrictions());
		jw.name("roundTrip").value(response.isRoundTrip());
		jw.name("restrictionValues");
		outputMap(jw, response.getRestrictionValues());
		jw.name("excludeRestrictions");
		outputList(jw, response.getExcludeRestrictions());
	}
	
	protected void writeFields(RouterDistanceResponse response) throws IOException {
		writeFields((ApiResponse)response);
		jw.name("points");
		writePointList(response.getPoints());
		jw.name("routeFound").value(response.isRouteFound());
		jw.name("distance").jsonValue(response.getDistanceStr());
		jw.name("time").value(response.getTime());
		jw.name("timeText").value(TimeHelper.formatTime(response.getTime()));		
	}
	
	private void writePointList(List<Point> points) throws IOException {
		jw.beginArray();
		for(Point p : points) {
			writePoint(p);
		}
		jw.endArray();
	}
	
	private void writePoint(Point p) throws IOException {
		jw.beginArray();
		jw.jsonValue(formatOrdinate(p.getX())); 
		jw.jsonValue(formatOrdinate(p.getY()));
		jw.endArray();
	}

	protected void writeFields(RouterDistanceBetweenPairsResponse response) throws IOException {
		writeFields((ApiResponse)response);
		jw.name("fromPoints");
		writePointList(response.getFromPoints());
		jw.name("toPoints");
		writePointList(response.getToPoints());
		jw.name("pairs");
		jw.beginArray();
		boolean skipFails = response.getMaxPairs() < Integer.MAX_VALUE;
		int curResult = 0;
		for(int from = 0; from < response.getFromPoints().size(); from++) {
			for( int to = 0; to < response.getToPoints().size(); to++) {
				if(!skipFails || response.getError(curResult) == null) {
					jw.beginObject();
					jw.name("from").value(from);
					jw.name("to").value(to);
					jw.name("distance").value(response.getDistanceStr(curResult));
					jw.name("time").value(response.getTime(curResult));
					jw.name("timeText").value(TimeHelper.formatTime(response.getTime(curResult)));
					String message = response.getError(curResult);
					if(message != null) {
						jw.name("message").value(message);
						jw.name("routeFound").value(false);
					} else {
						jw.name("routeFound").value(true);
					}
					jw.endObject();
				}
				curResult++;
			}
		}
		jw.endArray();
	}

	protected void writeFields(RouterRouteResponse response) throws IOException {
		writeFields((RouterDistanceResponse)response);
		if(response instanceof RouterOptimizedResponse) {
			int[] visitOrder = ((RouterOptimizedResponse)response).getVisitOrder();
			if(visitOrder != null) {
				jw.name("visitOrder");
				jw.beginArray();
				for(int order : visitOrder) {
					jw.value(order);
				}
				jw.endArray();
			}
		}
		List<Partition> parts = response.getPartitions();
		if(parts != null && !parts.isEmpty()) {
			jw.name("partition").value(String.join(",", response.getPartition().stream().map(Attribute::toString).collect(Collectors.toList())));
			jw.name("partitions");
			jw.beginArray();
			for(Partition p : parts) {
				jw.beginObject();
				jw.name("index").value(p.getIndex());
				jw.name("distance").jsonValue(response.getDistanceUnit().formatForDisplay(DistanceUnit.METRE.convertTo(p.getDistance(), response.getDistanceUnit())));
				for(Entry<Attribute, Object> entry : p.getValues().entrySet()) {
					jw.name(entry.getKey().toString());
					Object val = entry.getValue();
					if(val == null) {
						jw.jsonValue(null);
					} else if(val instanceof Boolean) {
						jw.jsonValue(val.toString());
					} else {
						jw.value(val.toString());
					}
				}
				jw.endObject();
			}
			jw.endArray();
		}
		List<Integer> restrictions = response.getRestrictions();
		if(restrictions != null && !restrictions.isEmpty()) {
			jw.name("restrictions");
			jw.beginArray();
			for(Integer r : restrictions) {
				jw.value(r);
			}
			jw.endArray();
		}
		
		jw.name("route");
		jw.beginArray();
		if(response.getPath() != null) {
			CoordinateSequence coords = response.getPath().getCoordinateSequence();
			for(int i = 0; i < coords.size(); i++) {
				coordinate(jw, coords.getX(i), coords.getY(i));
			}
		}
		jw.endArray();
		
		if(response.getEnabledOptions().contains(RouteOption.TRANSPORT_LINE_ID)) {
			jw.name("tlids");
			jw.beginArray();
			if(response.getTlids() != null) {
				for(Integer tlid : response.getTlids()) {
					jw.value(tlid);
				}
			}
			jw.endArray();
		}
	}
	
	protected void writeFields(RouterDirectionsResponse response) throws IOException {
		writeFields((RouterRouteResponse)response);
		notifications(jw, response.getNotifications());
		jw.name("directions");
		jw.beginArray();
		for(Direction dir : response.getDirections()) {
			jw.beginObject();
			jw.name("type").value(dir.getType());
			if(dir instanceof AbstractTravelDirection) {
				AbstractTravelDirection td = (AbstractTravelDirection)dir;
				jw.name("name").value(td.getStreetName());
				jw.name("distance").jsonValue(response.getDistanceUnit().formatForDisplay(td.getDistance()));
				jw.name("time").value(Math.round(td.getTime()));
				if(dir instanceof StartDirection) {
					StartDirection sd = (StartDirection)dir;
					jw.name("heading").value(sd.getHeading().toString());
				}
			}
			jw.name("text").value(dir.format(response));
			jw.name("point");
			coordinate(jw, dir.getPoint().getX(), dir.getPoint().getY());
			notifications(jw, dir.getNotifications());
			laneRequirements(jw, dir.getLaneRequirements(), response);
			jw.endObject();
		}
		jw.endArray();
	}

	protected void writeFields(IsochroneResponse response) throws IOException {
		jw.name("type").value("FeatureCollection");
		writeFields((ApiResponse)response);
		jw.name("features");
		jw.beginArray();
		List<Geometry> polygons = response.getPolygons();
		for(int i = 0; i < polygons.size(); i++) {
			jw.beginObject();
			jw.name("type").value("Feature");
			jw.name("ID").value((long)i+1);
			jw.name("geometry");
			geometry(jw, polygons.get(i));
			jw.name("properties");
			jw.beginObject();
			jw.name("title").value((long)response.getZoneSize() * (i+1));
			jw.endObject(); // properties
			jw.endObject(); // feature
		}
		jw.endArray();
	}
	
	public static void notifications(JsonWriter jw, Collection<Notification> notifications) throws IOException {
		if(notifications == null) return;
		jw.name("notifications");
		jw.beginArray();
		for(Notification note : notifications) {
			jw.beginObject();
			jw.name("type").value(note.getType());
			jw.name("message").value(note.getMessage());
			jw.endObject();
		}
		jw.endArray();		
	}
	
	public static void laneRequirements(JsonWriter jw, List<LaneRequirement> laneRequirements, RouterDirectionsResponse response) throws IOException {
		if(laneRequirements == null) return;
		jw.name("laneRequirements");
		jw.beginArray();
		for(LaneRequirement lr : laneRequirements) {
			jw.beginObject();
			jw.name("message").value(lr.format(response));
			jw.name("distance").value(lr.getDistance());
			jw.name("location");
			geometry(jw, lr.getLocation());
			jw.name("locationId").value(lr.getLocationId());
			jw.name("safeLanes").beginArray();
			boolean[] safeLanes = lr.getSafeLanes();
			for(int i = 0; i < safeLanes.length; i++) {
				jw.value(safeLanes[i]);
			}
			jw.endArray();
			jw.endObject();
		}
		jw.endArray();		
	}
	
	public static void geometry(JsonWriter jw, Geometry g) throws IOException {
		jw.beginObject();
		jw.name("type").value(g.getGeometryType());
		jw.name("coordinates");
		switch(g.getGeometryType()) {
			case "Point":
				coordinate(jw, ((Point)g).getX(), ((Point)g).getY());
				break;
			case "LineString":
				coordinates(jw, ((LineString)g).getCoordinateSequence());
				break;
			case "Polygon":
				polygon(jw, ((Polygon)g)); 
				break;
			case "MultiPolygon":
				multiPolygon(jw, ((MultiPolygon)g)); 
				break;
			default: jw.value("Unknown geometry type");
		}
		jw.endObject();

	}

	public static void multiPolygon(JsonWriter jw, MultiPolygon mp) throws IOException {
		jw.beginArray();
		for(int i = 0; i < mp.getNumGeometries(); i++) {
			polygon(jw, (Polygon)mp.getGeometryN(i));
		}
		jw.endArray();
	}

	public static void polygon(JsonWriter jw, Polygon p) throws IOException {
		jw.beginArray();
		coordinates(jw, p.getExteriorRing().getCoordinateSequence());
		for(int i = 0; i < p.getNumInteriorRing(); i++) {
			coordinates(jw, p.getInteriorRingN(i).getCoordinateSequence());
		}
		jw.endArray();
	}

	public static void coordinates(JsonWriter jw, CoordinateSequence cs) throws IOException {
		jw.beginArray();
		for(int i = 0; i < cs.size(); i++) {
			coordinate(jw, cs.getX(i), cs.getY(i));
		}
		jw.endArray();		
	}

	public static void coordinate(JsonWriter jw, double x, double y) throws IOException {
		jw.beginArray();
		jw.jsonValue(formatOrdinate(x));
		jw.jsonValue(formatOrdinate(y));
		jw.endArray();		
	}

	public static void outputValue(JsonWriter jw, Object value) throws IOException {
		if(value == null) {
			jw.value("");
		} else if(value instanceof Number) {
			jw.value((Number)value);
		} else {
			jw.value(value.toString());
		}		
	}
	
	public static <E> void outputList(JsonWriter jw, Collection<E> list) throws IOException {
		jw.beginArray();
		if(list != null && !list.isEmpty()) {
			for(E item : list) {
				outputValue(jw, item);
			}
		}
		jw.endArray();
	}

	public static <A,B> void outputMap(JsonWriter jw, Map<A,B> map) throws IOException {
		jw.beginObject();
		if(map != null && !map.isEmpty()) {
			for(Entry<A, B> entry : map.entrySet()) {
				jw.name(entry.getKey().toString());
				outputValue(jw, entry.getValue());
			}
		}
		jw.endObject();	
	}

}
