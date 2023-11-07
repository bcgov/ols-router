/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.enums.TrafficImpactor;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.enums.DistanceUnit;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.data.enums.RoutingCriteria;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.data.enums.XingClass;
import ca.bc.gov.ols.router.engine.basic.Attribute;
import ca.bc.gov.ols.router.engine.basic.GlobalDistortionField;

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
	private VehicleType vehicleType = VehicleType.CAR;
	private Double length;
	private boolean followTruckRoute = false;
	private double truckRouteMultiplier = 9;
	private static Map<TrafficImpactor,Double> defaultXingCostMap;
	private Map<TrafficImpactor,Double> xingCostMap;
	private static double defaultXingCostMultiplier = 1;
	private double xingCostMultiplier = 1;
	private static Map<VehicleType,Map<TurnDirection, Double>> defaultTurnCostMap;
	private Map<TurnDirection, Double> turnCostMap;
	private String gdfString;
	private GlobalDistortionField globalDistortionField;
	private String routeDescription;
	private int maxPairs = Integer.MAX_VALUE;
	private boolean roundTrip = false;
	private int zoneCount = 1;
	private int zoneSize = 0;
	private boolean inbound = false;
	private EnumSet<Attribute> partitionAttributes = EnumSet.noneOf(Attribute.class);
	private EnumSet<RouteOption> enabledOptions;
	private boolean setEnableCalled = false;
	private boolean turnCostsSet = false;
	private boolean followTruckRouteSet = false;
	private RestrictionSource restrictionSource = RestrictionSource.ITN;
	private int snapDistance = 1000;
	private boolean simplifyDirections = false;
	private int simplifyThreshold = 250;
	private Map<RestrictionType,Double> restrictionValues = new HashMap<RestrictionType,Double>();
	private boolean listRestrictions = false;
	private Set<Integer> excludeRestrictions = Collections.emptySet();
	
	static {
		double[] xingCost = RouterConfig.getInstance().getDefaultXingCost();
		defaultXingCostMap = buildXingCostMap(xingCost);
		if(xingCost.length == 4) {
			defaultXingCostMultiplier = xingCost[3];
		}
		double[] turnCost = RouterConfig.getInstance().getDefaultTurnCost();
		defaultTurnCostMap = buildVehicleTypeTurnCostMap(turnCost);
	}
	
	public RoutingParameters() {
		RouterConfig config = RouterConfig.getInstance();
		enabledOptions = RouteOption.fromList(config.getDefaultEnableOptions());
		xingCostMap = defaultXingCostMap;
		xingCostMultiplier = defaultXingCostMultiplier;
		turnCostMap = defaultTurnCostMap.get(vehicleType);
		truckRouteMultiplier = config.getDefaultTruckRouteMultiplier();
		snapDistance = config.getDefaultSnapDistance();
		setSimplifyThreshold(config.getDefaultSimplifyThreshold());
	}

	private static EnumMap<TrafficImpactor,Double> buildXingCostMap(double[] xingCost) {
		EnumMap<TrafficImpactor,Double> xingCostMap = new EnumMap<TrafficImpactor, Double>(TrafficImpactor.class);
		for(TrafficImpactor imp : TrafficImpactor.values()) {
			xingCostMap.put(imp, 0.0);
		}
		if(xingCost.length == 4) {
			xingCostMap.put(TrafficImpactor.YIELD, xingCost[0]);
			xingCostMap.put(TrafficImpactor.ROUNDABOUT, xingCost[0]);
			xingCostMap.put(TrafficImpactor.STOPSIGN, xingCost[1]);
			xingCostMap.put(TrafficImpactor.LIGHT, xingCost[2]);
		}
		return xingCostMap;
	}

	private static Map<VehicleType,Map<TurnDirection, Double>> buildVehicleTypeTurnCostMap(double[] turnCost) {
		EnumMap<VehicleType,Map<TurnDirection, Double>> turnCostMap = new EnumMap<VehicleType,Map<TurnDirection, Double>>(VehicleType.class);
		for(VehicleType vehicleType : VehicleType.values()) {
			Map<TurnDirection, Double> innerMap = new EnumMap<TurnDirection, Double>(TurnDirection.class);
			for(TurnDirection turnDir : TurnDirection.values()) {
				innerMap.put(turnDir, 0.0);
			}
			turnCostMap.put(vehicleType,innerMap);
		}
		if(turnCost.length == 4) {
			turnCostMap.get(VehicleType.CAR).put(TurnDirection.LEFT, turnCost[0]);
			turnCostMap.get(VehicleType.CAR).put(TurnDirection.RIGHT, turnCost[1]);
			turnCostMap.get(VehicleType.TRUCK).put(TurnDirection.LEFT, turnCost[2]);
			turnCostMap.get(VehicleType.TRUCK).put(TurnDirection.RIGHT, turnCost[3]);
		}
		return turnCostMap;
	}
	
	private static Map<TurnDirection, Double> buildTurnCostMap(Double leftCost, Double rightCost) {
		Map<TurnDirection, Double> turnCostMap = new EnumMap<TurnDirection, Double>(TurnDirection.class);
		for(TurnDirection turnDir : TurnDirection.values()) {
			switch(turnDir) {
			case LEFT:
				turnCostMap.put(turnDir, leftCost);
				break;
			case RIGHT:
				turnCostMap.put(turnDir, rightCost);
				break;
			case UTURN:
				turnCostMap.put(turnDir, 5.0);
				break;
			default:
				turnCostMap.put(turnDir, 0.0);
			}
		}
		return turnCostMap;
	}

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

	public VehicleType getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = VehicleType.convert(vehicleType);
		if(!turnCostsSet) {
			turnCostMap = defaultTurnCostMap.get(this.vehicleType);
		}
		if(!followTruckRouteSet) {
			followTruckRoute = true;
		}
	}

	public void setHeight(Double height) {
		if(this.restrictionValues.get(RestrictionType.VERTICAL) == null) {
			this.restrictionValues.put(RestrictionType.VERTICAL, height);
		}
	}

	public void setWidth(Double width) {
		if(this.restrictionValues.get(RestrictionType.HORIZONTAL) == null) {
			this.restrictionValues.put(RestrictionType.HORIZONTAL, width);
		}
	}

	public Double getLength() {
		return length;
	}

	public void setLength(Double length) {
		this.length = length;
	}

	public void setWeight(Double weight) {
		if(this.restrictionValues.get(RestrictionType.WEIGHT_GVW) == null) {
			this.restrictionValues.put(RestrictionType.WEIGHT_GVW, weight);
		}
	}

	public boolean isFollowTruckRoute() {
		return followTruckRoute;
	}

	public void setFollowTruckRoute(boolean followTruckRoute) {
		followTruckRouteSet = true;
		this.followTruckRoute = followTruckRoute;
	}

	public double getTruckRouteMultiplier() {
		return truckRouteMultiplier;
	}

	public void setTruckRouteMultiplier(double truckRouteMultiplier) {
		this.truckRouteMultiplier = truckRouteMultiplier;
	}

	public double getXingCost(TrafficImpactor imp, XingClass xingClass) {
		return xingClass.applyMultiplier(xingCostMap.get(imp), xingCostMultiplier);
	}

	public double getTurnCost(TurnDirection td, XingClass xingClass) {
		return xingClass.applyMultiplier(turnCostMap.get(td), xingCostMultiplier);
	}

	public void setXingCost(double[] xingCost) {
		xingCostMap = buildXingCostMap(xingCost);
		if(xingCost.length == 4) {
			xingCostMultiplier = xingCost[3];
		}
	}

	public void setTurnCost(double[] turnCost) {
		if(turnCost.length == 2) {
			turnCostsSet = true;
			turnCostMap = buildTurnCostMap(turnCost[0], turnCost[1]);
		}
	}
	
	public GlobalDistortionField getGlobalDistortionField() {
		if(globalDistortionField == null) {
			globalDistortionField = new GlobalDistortionField(RouterConfig.getInstance().getDefaultGlobalDistortionField(getVehicleType()));
			globalDistortionField.applyString(gdfString);
		}
		return globalDistortionField;
	}
	
	public void setGdf(String gdfString) {
		this.gdfString = gdfString; 
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
		if(setEnableCalled == false) {
			enabledOptions = EnumSet.complementOf(RouteOption.fromList(disabledOptionList));
		}
	}
	
	public void setEnable(String enabledOptionList) {
		setEnableCalled = true;
		enabledOptions = RouteOption.fromList(enabledOptionList);
	}

	public void disableOption(RouteOption ro) {
		enabledOptions.remove(ro);
	}

	public void enableOption(RouteOption ro) {
		enabledOptions.add(ro);
	}

	public boolean isEnabled(RouteOption ro) {
		return enabledOptions.contains(ro);
	}
	
	public Set<RouteOption> getEnabledOptions() {
		return enabledOptions;
	}
	
	public int getSnapDistance() {
		return snapDistance;
	}

	public void setSnapDistance(int snapDistance) {
		this.snapDistance = snapDistance;
	}

	public void setPartition(String partitionList) {
		partitionAttributes = Attribute.fromList(partitionList);
	}
	
	public EnumSet<Attribute> getPartition() {
		return partitionAttributes;
	}
	
	public void setRestrictionSource(RestrictionSource restrictionSource) {
		this.restrictionSource = restrictionSource;
	}
	
	public RestrictionSource getRestrictionSource() {
		return restrictionSource;
	}

	public boolean isSimplifyDirections() {
		return simplifyDirections;
	}

	public void setSimplifyDirections(boolean simplifyDirections) {
		this.simplifyDirections = simplifyDirections;
	}
	
	public int getSimplifyThreshold() {
		return simplifyThreshold;
	}

	public void setSimplifyThreshold(int simplifyThreshold) {
		this.simplifyThreshold = simplifyThreshold;
	}
	
	public Map<RestrictionType,Double> getRestrictionValues() {
		return restrictionValues;
	}
	
	public Double getRestrictionValue(RestrictionType type) {
		return restrictionValues.get(type);
	}
	
	/**
	 * Parses incoming restriction values into a map.
	 * Expected input format is: 
	 * <pre>{@code
	 * &restrictionValues=<RestrictionType>:<value>,<RestrictionType>:<value>
	 * }</pre>
	 * eg. VERTICAL:4.2,WEIGHT-GVW:15000
	 * @param values the string representation of the values
	 */
	public void setRestrictionValues(String values) {
		StringBuilder errorMessage = new StringBuilder();
		String[] pairs = values.split(",");
		for(String pair : pairs) {
			String[] keyValue = pair.split(":");
			if(keyValue.length == 2) {
				RestrictionType type = RestrictionType.get(keyValue[0]);
				if(type != null) {
					try {
						Double value = Double.parseDouble(keyValue[1]);
						restrictionValues.put(type, value);
					} catch(Exception e) {
						errorMessage.append("Invalid restriction value: '" + keyValue[1] + "';");
					}
				}
			} else {
				errorMessage.append("Malformed restriction value pair (should have a single colon separator): '" + keyValue + "';");
			}
		}
		if(!errorMessage.isEmpty()) {
			throw new IllegalArgumentException(errorMessage.toString());
		}
	}

	public boolean isListRestrictions() {
		return listRestrictions;
	}
	
	public void setListRestrictions(boolean listRestrictions) {
		this.listRestrictions = listRestrictions;
	}
	
	public Set<Integer> getExcludeRestrictions() {
		return excludeRestrictions;
	}
	
	public void setExcludeRestrictions(int[] excludeRestrictions) {
		this.excludeRestrictions = Arrays.stream(excludeRestrictions).boxed().collect(Collectors.toCollection(HashSet::new));
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
