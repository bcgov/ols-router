/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.data.enums.DistanceUnit;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.data.enums.RoutingCriteria;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.engine.basic.Attribute;
import ca.bc.gov.ols.router.engine.basic.GlobalDistortionField;
import ca.bc.gov.ols.rowreader.DateType;

public class ApiResponse {

	private LocalDateTime timeStamp;
	private long executionTime;
	private int srsCode;
	private String callback;
	
	protected DistanceUnit distanceUnit;
	protected RoutingCriteria criteria;
	protected Set<RouteOption> enabledOptions;
	protected String routeDescription;
	protected ZonedDateTime dataProcessingTimestamp;
	protected ZonedDateTime roadNetworkTimestamp;
	
	protected Instant departure;
	protected boolean correctSide;
	protected VehicleType vehicleType;
	protected boolean followTruckRoute;
	protected double truckRouteMultiplier;
	protected String xingCostString;
	protected double xingCostMultiplier;
	protected String turnCostString;
	protected GlobalDistortionField globalDistortionField;
	protected boolean roundTrip;
	protected EnumSet<Attribute> partition;
	protected int snapDistance;
	protected boolean simplifyDirections;
	protected int simplifyThreshold;
	protected RestrictionSource restrictionSource;
	protected Map<RestrictionType,Double> restrictionValues;
	protected Set<Integer> excludeRestrictions;
	protected boolean listRestrictions;
	

	public ApiResponse(RoutingParameters params, Map<DateType, ZonedDateTime> dates) {
		timeStamp = LocalDateTime.now();
		callback = params.getCallback();
		srsCode = params.getOutputSRS();
		distanceUnit = params.getDistanceUnit();
		criteria = params.getCriteria();
		enabledOptions = params.getEnabledOptions();
		routeDescription = params.getRouteDescription();
		if(dates != null) {
			dataProcessingTimestamp = dates.get(DateType.PROCESSING_DATE);
			roadNetworkTimestamp = dates.get(DateType.ITN_VINTAGE_DATE);
		}
		departure = params.getDeparture();
		correctSide = params.isCorrectSide();
		vehicleType = params.getVehicleType();
		followTruckRoute = params.isFollowTruckRoute();
		truckRouteMultiplier = params.getTruckRouteMultiplier();
		xingCostString = params.getXingCostString();
		turnCostString = params.getTurnCostString();
		globalDistortionField = params.getGlobalDistortionField();
		roundTrip = params.isRoundTrip();
		partition = params.getPartition();
		snapDistance = params.getSnapDistance();
		simplifyDirections = params.isSimplifyDirections();
		simplifyThreshold = params.getSimplifyThreshold();
		restrictionSource = params.getRestrictionSource();
		restrictionValues = params.getRestrictionValues();
		excludeRestrictions = params.getExcludeRestrictions();
		listRestrictions = params.isListRestrictions();
	}
	
	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(LocalDateTime timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public long getExecutionTime() {
		return executionTime;
	}
	
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	
	public int getSrsCode() {
		return srsCode;
	}
	
	public void setSrsCode(int srsCode) {
		this.srsCode = srsCode;
	}
	
	public String getCallback() {
		return callback;
	}
	
	public void setCallback(String callback) {
		this.callback = callback;
	}

	public RoutingCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(RoutingCriteria criteria) {
		this.criteria = criteria;
	}

	public Set<RouteOption> getEnabledOptions() {
		return enabledOptions;
	}
	
	public DistanceUnit getDistanceUnit() {
		return distanceUnit;
	}

	public String getRouteDescription() {
		return routeDescription;
	}

	public void setRouteDescription(String routeDescription) {
		this.routeDescription = routeDescription;
	}

	public void setDataProcessingTimestamp(ZonedDateTime dataProcessingTimestamp) {
		this.dataProcessingTimestamp = dataProcessingTimestamp;
	}

	public ZonedDateTime getDataProcessingTimestamp() {
		return dataProcessingTimestamp;
	}

	public void setRoadNetworkTimestamp(ZonedDateTime roadNetworkTimestamp) {
		this.roadNetworkTimestamp = roadNetworkTimestamp;
	}

	public ZonedDateTime getRoadNetworkTimestamp() {
		return roadNetworkTimestamp;
	}
	
	public Instant getDeparture() {
		return departure;
	}

	public boolean isCorrectSide() {
		return correctSide;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}

	public boolean isFollowTruckRoute() {
		return followTruckRoute;
	}

	public double getTruckRouteMultiplier() {
		return truckRouteMultiplier;
	}

	public String getXingCostString() {
		return xingCostString;
	}

	public double getXingCostMultiplier() {
		return xingCostMultiplier;
	}

	public String getTurnCostString() {
		return turnCostString;
	}

	public GlobalDistortionField getGlobalDistortionField() {
		return globalDistortionField;
	}

	public int getSnapDistance() {
		return snapDistance;
	}

	public boolean isSimplifyDirections() {
		return simplifyDirections;
	}

	public int getSimplifyThreshold() {
		return simplifyThreshold;
	}

	public RestrictionSource getRestrictionSource() {
		return restrictionSource;
	}

	public Map<RestrictionType, Double> getRestrictionValues() {
		return restrictionValues;
	}

	public Set<Integer> getExcludeRestrictions() {
		return excludeRestrictions;
	}

	public boolean isListRestrictions() {
		return listRestrictions;
	}

	public boolean isRoundTrip() {
		return roundTrip;
	}

	public EnumSet<Attribute> getPartition() {
		return partition;
	}

	public void reproject(GeometryReprojector gr) {
		// reproject is a no-op unless you have geometry data to reproject.
	}
	
	protected List<Point> reprojectPoints(List<Point> points, GeometryReprojector gr) {
		ArrayList<Point> reprojPoints = new ArrayList<Point>(points.size());
		for(Point p : points) {
			reprojPoints.add(gr.reproject(p, srsCode));
		}
		return reprojPoints;
	}

}
