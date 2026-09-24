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

import io.swagger.v3.oas.annotations.Parameter;

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
	
	@Parameter(description = "The EPSG code of the spatial reference system (SRS) to use for output geometries.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer",
					allowableValues = {"4326", "4269", "3005", "26907", "26908", "26909", "26910", "26911"},
					defaultValue = "4326"),
			example = "4326")
	private int outputSRS = 4326;
	@Parameter(description = "If provided, the JSON result will be wrapped in a function call of the given name (for JSONP support).",
			example = "jsonp")
	private String callback = "jsonp";
	@Parameter(description = "If true, the result is returned as a file attachment.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "boolean", defaultValue = "false"))
	private boolean asAttachment = false;
	@Parameter(description = "The routing criteria to use when determining the route. Default is fastest.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string",
					allowableValues = {"fastest", "shortest"}, defaultValue = "fastest"))
	private RoutingCriteria criteria = RoutingCriteria.FASTEST;
	@Parameter(description = "The unit of measure for distances in the response. Default is km.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string",
					allowableValues = {"km", "mi"}, defaultValue = "km"))
	private DistanceUnit distanceUnit = DistanceUnit.KILOMETRE;
	@Parameter(description = "The coordinate (x,y) of the single point for the spatial request, "
			+ "in the format \"x,y\". Must be in the same SRS as the outputSRS parameter.",
			example = "-123.3648,48.4254")
	private double[] point;
	@Parameter(description = "The coordinates (x,y) of the points to route between, in the format \"x,y,x,y...\". "
			+ "Must be in the same SRS as the outputSRS parameter.",
			example = "-123.3648,48.4254,-123.3701,48.4181")
	private double[] points;
	@Parameter(description = "The coordinates (x,y) of the from points for betweenPairs requests, "
			+ "in the format \"x,y,x,y...\". Must be in the same SRS as the outputSRS parameter.",
			example = "-123.7079,48.7786,-123.5378,48.3820")
	private double[] fromPoints;
	@Parameter(description = "The coordinates (x,y) of the to points for betweenPairs requests, "
			+ "in the format \"x,y,x,y...\". Must be in the same SRS as the outputSRS parameter.",
			example = "-124.9729,49.7151,-123.1394,49.7040")
	private double[] toPoints;
	@Parameter(hidden = true)
	private Point pointPoint;
	@Parameter(hidden = true)
	private List<Point> pointPoints;
	@Parameter(hidden = true)
	private List<Point> pointFromPoints;
	@Parameter(hidden = true)
	private List<Point> pointToPoints;
	@Parameter(description = "The date and time of departure, used to evaluate time-dependent routing "
			+ "constraints such as turn restrictions and ferry schedules.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "date-time"))
	private Instant departure = Instant.now();
	@Parameter(description = "If true, the route will arrive on the correct side of the road.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "boolean", defaultValue = "false"))
	private boolean correctSide = false;
	@Parameter(description = "The type of vehicle to route for.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string",
					allowableValues = {"CAR", "TRUCK"}, defaultValue = "CAR"))
	private VehicleType vehicleType = VehicleType.CAR;
	@Parameter(description = "If true, the route will follow designated truck routes where possible.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "boolean", defaultValue = "false"))
	private boolean followTruckRoute = false;
	@Parameter(description = "A multiplier applied to the cost of non-truck-route segments "
			+ "when following truck routes.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "9"))
	private double truckRouteMultiplier = 9;
	private static Map<TrafficImpactor,Double> defaultXingCostMap;
	@Parameter(hidden = true)
	private Map<TrafficImpactor,Double> xingCostMap;
	private static double defaultXingCostMultiplier = 1;
	private double xingCostMultiplier = 1;
	private static Map<VehicleType,Map<TurnDirection, Double>> defaultTurnCostMap;
	@Parameter(hidden = true)
	private Map<TurnDirection, Double> turnCostMap;
	@Parameter(hidden = true)
	private String gdfString;
	@Parameter(hidden = true)
	private GlobalDistortionField globalDistortionField;
	@Parameter(description = "A description of the request, echoed back in the response.")
	private String routeDescription;
	@Parameter(description = "The maximum number of toPoints to evaluate for each fromPoint in betweenPairs requests.")
	private int maxPairs = Integer.MAX_VALUE;
	@Parameter(description = "If true, the route returns to its starting point.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "boolean", defaultValue = "false"))
	private boolean roundTrip = false;
	@Parameter(description = "The number of zones/contours to generate for isochrone requests, between 1 and 10.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "1"))
	private int zoneCount = 1;
	@Parameter(description = "The size of each zone/contour, in minutes for time-based isochrones "
			+ "or metres for distance-based isochrones.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "0"))
	private int zoneSize = 0;
	@Parameter(description = "If true, the isochrone/loop is computed inbound to the point rather than outbound.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "boolean", defaultValue = "false"))
	private boolean inbound = false;
	@Parameter(hidden = true)
	private EnumSet<Attribute> partitionAttributes = EnumSet.noneOf(Attribute.class);
	@Parameter(hidden = true)
	private EnumSet<RouteOption> enabledOptions;
	@Parameter(description = "A comma-separated list of modules to enable. Enable takes precedence over disable. "
			+ "<br><br>When the enable parameter is used, unlisted values will be disabled.<br><br>Module names include:<br> "
			+ "sc - ferry schedules; disabled by default and only suitable for demos<br>"
			+ "tf - historic traffic congestion; disabled by default and only suitable for demos<br>"
			+ "ev - road events; disabled by default and only suitable for demos<br>"
			+ "td - time-dependency; disabled by default; disabling this disables sc, tf, and ev modules<br>"
			+ "tr - turn restrictions; enabled by default; if td is disabled, time-dependent turn restrictions are ignored<br>"
			+ "tc - turn costs (e.g., left turns take longer than right turns); enabled by default<br>"
			+ "xc - crossing costs (e.g., crossing a major road takes longer than the other way around); enabled by default<br>"
			+ "gdf - global distortion field; applies friction factors by ITN road class; enabled by default<br>"
			+ "tl - transportation line IDs; disabled by default",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "gdf,tr,xc,tc"))
	private String enable = "gdf,tr,xc,tc";
	@Parameter(description = "A comma-separated list of modules to disable. Enable takes precedence over disable. "
			+ "<br><br>When the disable parameter is used, unlisted values will be enabled. (e.g., sc,tf,ev,td).<br> "
			+ "Module names include:<br> "
			+ "sc - ferry schedules; disabled by default and only suitable for demos<br>"
			+ "tf - historic traffic congestion; disabled by default and only suitable for demos<br>"
			+ "ev - road events; disabled by default and only suitable for demos<br>"
			+ "td - time-dependency; disabled by default; disabling this disables sc, tf, and ev modules<br>"
			+ "tr - turn restrictions; enabled by default; if td is disabled, time-dependent turn restrictions are ignored<br>"
			+ "tc - turn costs; enabled by default<br>"
			+ "xc - crossing costs; enabled by default<br>"
			+ "gdf - global distortion field; enabled by default<br>"
			+ "tl - transportation line IDs; disabled by default",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "td,ev,sc,tf,tl"))
	private String disable = "td,ev,sc,tf,tl";
	private boolean setEnableCalled = false;
	private boolean turnCostsSet = false;
	private boolean followTruckRouteSet = false;
	@Parameter(description = "The source of the restriction data to use.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string",
					allowableValues = {"ITN", "RDM"}, defaultValue = "ITN"))
	private RestrictionSource restrictionSource = RestrictionSource.ITN;
	@Parameter(description = "The maximum distance in metres that a point can be from the road network "
			+ "and still be snapped to it.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "1000"))
	private int snapDistance = 1000;
	@Parameter(description = "If true, the directions are simplified to reduce the number of instructions.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "boolean", defaultValue = "false"))
	private boolean simplifyDirections = false;
	@Parameter(description = "The maximum length in metres that a direction segment can be before it is "
			+ "split into multiple directions when simplifying.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "250"))
	private int simplifyThreshold = 250;
	@Parameter(hidden = true)
	private Map<RestrictionType,Double> restrictionValues = new HashMap<RestrictionType,Double>();
	@Parameter(description = "If true, the restriction identifiers satisfied by the route are listed in the response.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(type = "boolean", defaultValue = "false"))
	private boolean listRestrictions = false;
	@Parameter(description = "A list of restriction identifiers to exclude from consideration.",
			example = "1,2,3")
	private Set<Integer> excludeRestrictions = Collections.emptySet();
	@Parameter(description = "The minimum straight-line distance in metres between points on the same road segment "
			+ "below which routing is computed directly between the points rather than through the road network.",
			schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "0"))
	private int minRoutingDistance = 0;
	
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

	@Parameter(hidden = true)
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
		if(height != null && this.restrictionValues.get(RestrictionType.VERTICAL) == null) {
			this.restrictionValues.put(RestrictionType.VERTICAL, height);
		}
	}

	public void setWidth(Double width) {
		if(width != null && this.restrictionValues.get(RestrictionType.HORIZONTAL) == null) {
			this.restrictionValues.put(RestrictionType.HORIZONTAL, width);
		}
	}

	public void setLength(Double length) {
		if(length != null && this.restrictionValues.get(RestrictionType.LENGTH) == null) {
			this.restrictionValues.put(RestrictionType.LENGTH, length);
		}
	}

	public void setWeight(Double weight) {
		if(weight != null && this.restrictionValues.get(RestrictionType.WEIGHT_GVW) == null) {
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

	@Parameter(hidden = true)
	public String getXingCostString() {
		return xingCostMap.get(TrafficImpactor.YIELD)
				+ "," + xingCostMap.get(TrafficImpactor.STOPSIGN)
				+ "," + xingCostMap.get(TrafficImpactor.LIGHT)
				+ "," + xingCostMultiplier;
	}

	public double getTurnCost(TurnDirection td, XingClass xingClass) {
		return xingClass.applyMultiplier(turnCostMap.get(td), xingCostMultiplier);
	}
	
	@Parameter(hidden = true)
	public String getTurnCostString() {
		return turnCostMap.get(TurnDirection.LEFT) + "," + turnCostMap.get(TurnDirection.RIGHT);
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
	
	@Parameter(hidden = true)
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
	
	public String getDisable() {
		return enabledOptions == null ? "" : RouteOption.setToString(EnumSet.complementOf(enabledOptions));
	}
	
	public void setEnable(String enabledOptionList) {
		setEnableCalled = true;
		enabledOptions = RouteOption.fromList(enabledOptionList);
	}

	public String getEnable() {
		return enabledOptions == null ? "" : RouteOption.setToString(enabledOptions);
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
	
	@Parameter(hidden = true)
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
	
	@Parameter(description = "A comma-separated list of attribute names (e.g. isTruckRoute,isFerry,locality,ownership) "
			+ "used to partition the route geometry and directions into route parts.")
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
	
	@Parameter(hidden = true)
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
	
	public int getMinRoutingDistance() {
		return minRoutingDistance;
	}

	public void setMinRoutingDistance(int minRoutingDistance) {
		this.minRoutingDistance = minRoutingDistance;
	}

	/**
	 * Resolves any parameters whose values are dependent on other parameters; called after all parameter setters have been called.
	 *  
	 * @param config the RouterConfig to use for defaults values and SRS
	 * @param gf the geometryFactory to use to create point geometry parameters
	 * @param gr the GeometryReprojector to use to reproject geometries as required
	 */
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
