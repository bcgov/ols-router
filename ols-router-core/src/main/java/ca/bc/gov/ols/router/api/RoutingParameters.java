/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import ca.bc.gov.ols.router.RouterConfig;
import ca.bc.gov.ols.router.data.enumTypes.DistanceUnit;
import ca.bc.gov.ols.router.data.enumTypes.RouteOption;
import ca.bc.gov.ols.router.data.enumTypes.RoutingCriteria;

public class RoutingParameters {
	
	private int outputSRS = 4326;
	private String callback = "jsonp";
	private boolean asAttachment = false;
	private RoutingCriteria criteria = RoutingCriteria.FASTEST;
	private DistanceUnit distanceUnit = DistanceUnit.KILOMETRE;
	private double[] point;
	private double[] points;
	private double[] fromPoints;
	private double[] toPoints;
	private Point pointPoint;
	private List<Point> pointPoints;
	private List<Point> pointFromPoints;
	private List<Point> pointToPoints;
	private Instant departure = Instant.now();
	private boolean correctSide = false;
	private Double height;
	private Double width;
	private Double length;
	private Double weight;
	private boolean followTruckRoute = false;
	private double truckRouteMultiplier = 2;
	private String routeDescription;
	private int maxPairs = Integer.MAX_VALUE;
	private boolean roundTrip = false;
	private int zoneCount = 1;
	private int zoneSize = 0;
	private boolean inbound = false;
	private EnumSet<RouteOption> disabledOptions = EnumSet.noneOf(RouteOption.class);
	
	public int getOutputSRS() {
		return outputSRS;
	}
	
	public void setOutputSRS(int outputSRS) {
		this.outputSRS = outputSRS;
	}
	
	public String getCallback() {
		return callback;
	}
	
	public void setCallback(String callback) {
		this.callback = callback;
	}
	
	public boolean isAsAttachment() {
		return asAttachment;
	}
	
	public void setAsAttachment(boolean asAttachment) {
		this.asAttachment = asAttachment;
	}
	
	public RoutingCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(RoutingCriteria criteria) {
		this.criteria = criteria;
	}

	public DistanceUnit getDistanceUnit() {
		return distanceUnit;
	}

	public void setDistanceUnit(DistanceUnit distanceUnit) {
		this.distanceUnit = distanceUnit;
	}

	public Point getPoint() {
		return pointPoint;
	}
	
	public void setPoint(double[] point) {
		this.point = point;
	}

	public List<Point> getPoints() {
		return pointPoints;
	}

	public List<Point> getFullPoints() {
		if(roundTrip) {
			List<Point> fullPoints = new ArrayList<Point>(pointPoints.size() + 1);
			fullPoints.addAll(pointPoints);
			fullPoints.add(pointPoints.get(0));
			return fullPoints;
		}
		return pointPoints;
	}

	public void setPoints(double[] points) {
		this.points = points;
	}

	public List<Point> getToPoints() {
		return pointToPoints;
	}
	
	public void setToPoints(double[] toPoints) {
		this.toPoints = toPoints;
	}

	public List<Point> getFromPoints() {
		return pointFromPoints;
	}
	
	public void setFromPoints(double[] fromPoints) {
		this.fromPoints = fromPoints;
	}

	public Instant getDeparture() {
		return departure;
	}

	public void setDeparture(Instant departure) {
		if(departure != null) { 
			this.departure = departure;
		}
	}

	public boolean isCorrectSide() {
		return correctSide;
	}

	public void setCorrectSide(boolean correctSide) {
		this.correctSide = correctSide;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getLength() {
		return length;
	}

	public void setLength(Double length) {
		this.length = length;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public boolean isFollowTruckRoute() {
		return followTruckRoute;
	}

	public void setFollowTruckRoute(boolean followTruckRoute) {
		this.followTruckRoute = followTruckRoute;
	}

	public double getTruckRouteMultiplier() {
		return truckRouteMultiplier;
	}

	public void setTruckRouteMultiplier(double truckRouteMultiplier) {
		this.truckRouteMultiplier = truckRouteMultiplier;
	}

	public String getRouteDescription() {
		return routeDescription;
	}

	public void setRouteDescription(String routeDescription) {
		this.routeDescription = routeDescription;
	}
	
	public int getMaxPairs() {
		return maxPairs;
	}

	public void setMaxPairs(int maxPairs) {
		this.maxPairs = maxPairs;
	}

	public boolean isRoundTrip() {
		return roundTrip;
	}

	public void setRoundTrip(boolean roundTrip) {
		this.roundTrip = roundTrip;
	}

	public int getZoneCount() {
		return zoneCount;
	}

	public void setZoneCount(int zoneCount) {
		this.zoneCount = zoneCount;
	}

	public int getZoneSize() {
		return zoneSize;
	}

	public void setZoneSize(int zoneSize) {
		this.zoneSize = zoneSize;
	}

	public boolean isInbound() {
		return inbound;
	}

	public void setInbound(boolean inbound) {
		this.inbound = inbound;
	}

	public void setDisable(String disabledOptionList) {
		disabledOptions = RouteOption.fromList(disabledOptionList);
	}

	public boolean isEnabled(RouteOption ro) {
		return !disabledOptions.contains(ro);
	}

	public void resolve(RouterConfig config, GeometryFactory gf, GeometryReprojector gr) {
		if(point != null && point.length == 2) {
			pointPoint = gr.reproject(gf.createPoint(new Coordinate(point[0], point[1])), config.getBaseSrsCode());
		}
		pointPoints = resolvePoints(points, gf, gr, config.getBaseSrsCode());
		pointFromPoints = resolvePoints(fromPoints, gf, gr, config.getBaseSrsCode());
		pointToPoints = resolvePoints(toPoints, gf, gr, config.getBaseSrsCode());
	}

	private static List<Point> resolvePoints(double[] points, GeometryFactory gf, GeometryReprojector gr, int baseSrs) {
		if(points != null && points.length % 2 == 0) {
			List<Point>pointPoints = new ArrayList<Point>(points.length/2);
			for(int i = 0; i < points.length/2; i++) {
				pointPoints.add(gr.reproject(gf.createPoint(new Coordinate(points[2*i], points[2*i+1])), baseSrs));
			}
			return pointPoints;
		}
		return null;
	}
}
