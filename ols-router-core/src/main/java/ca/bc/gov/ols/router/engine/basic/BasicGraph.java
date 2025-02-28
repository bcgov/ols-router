/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TrafficImpactor;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.RoadEvent;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.data.enums.XingClass;
import ca.bc.gov.ols.router.data.vis.VisLayers;
import ca.bc.gov.ols.router.restrictions.Constraint;
import ca.bc.gov.ols.router.restrictions.RestrictionLookup;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.util.IntObjectArrayMap;
import gnu.trove.map.TIntObjectMap;

/**
 * BasicGraph encapsulates all of the data required to route using the BasicGraphEngine, 
 * including the internal graph structure itself, as well as the various auxiliary data lookups
 * such as restrictions, event, schedules, etc.
 */
public class BasicGraph implements SegmentIdLookup {
	private static final Logger logger = LoggerFactory.getLogger(BasicGraph.class.getCanonicalName());
	
	private BasicGraphInternal internalGraph;
	private RouterConfig config;
	private IntObjectArrayMap<int[]> edgeIdBySegId;
	private TurnLookup turnCostLookup;
	private EventLookup eventLookup;
	private EnumMap<RestrictionSource,RestrictionLookup> restrictionLookupMap = new EnumMap<>(RestrictionSource.class);
	private TrafficLookup trafficLookup;
	private ScheduleLookup scheduleLookup;
	TIntObjectMap<EnumMap<VehicleType,Double>> localDistortionField;
	private VisLayers visLayers;
	private Map<DateType, ZonedDateTime> dates;


	public BasicGraph() {
	}

	/**
	 * Copy constructor for updating
	 * @param g the BasicGraph to copy
	 */
	public BasicGraph(BasicGraph g) {
		internalGraph = g.internalGraph;
		config = g.config;
		edgeIdBySegId = g.edgeIdBySegId;
		turnCostLookup = g.turnCostLookup;
		eventLookup = g.eventLookup;
		restrictionLookupMap = new EnumMap<RestrictionSource, RestrictionLookup>(g.restrictionLookupMap);
		trafficLookup = g.trafficLookup;
		scheduleLookup = g.scheduleLookup;
		localDistortionField = g.localDistortionField;
		visLayers = g.visLayers;
		dates = g.dates;
	}
	
	public void setInternalGraph(BasicGraphInternal internalGraph) {
		this.internalGraph = internalGraph;
	}

	BasicGraphInternal getInternalGraph() {
		return internalGraph;
	}

	public int[] getEdgesForSegment(int segmentId) {
		return edgeIdBySegId.get(segmentId);
	}
		
	public void setTurnCostLookup(TurnLookup lookup) {
		turnCostLookup = lookup;
	}
	
	public TurnLookup getTurnLookup() {
		return turnCostLookup;
	}
	
	public EventLookup getEventLookup() {
		return eventLookup;
	}

	public void setEventLookup(EventLookup eventLookup) {
		this.eventLookup = eventLookup;
	}
	
	public void setRestrictionLookup(RestrictionSource restrictionSource, RestrictionLookup restrictionLookup) {
		restrictionLookupMap.put(restrictionSource, restrictionLookup);
	}

	public RestrictionLookup getRestrictionLookup(RestrictionSource restrictionSource) {
		return restrictionLookupMap.get(restrictionSource);
	}
	
	public void setTrafficLookup(TrafficLookup trafficLookup) {
		this.trafficLookup = trafficLookup;
	}
	
	public TrafficLookup getTrafficLookup() {
		return trafficLookup;
	}

	public ScheduleLookup getScheduleLookup() {
		return scheduleLookup;
	}
	
	public void setScheduleLookup(ScheduleLookup scheduleLookup) {
		this.scheduleLookup = scheduleLookup;
	}

	public VisLayers getVisLayers() {
		return visLayers;
	}

	public void setVisLayers(VisLayers visLayers) {
		this.visLayers = visLayers;
	}

	public void setLocalDistortionField(final TIntObjectMap<EnumMap<VehicleType, Double>> localDistortionField) {
		this.localDistortionField = localDistortionField; 
	}

	public double getLocalDistortion(int edgeId, VehicleType vehicleType) {
		EnumMap<VehicleType, Double> frictionFactors = localDistortionField.get(edgeId);
		if(frictionFactors == null) return 1;
		Double frictionFactor = frictionFactors.get(vehicleType);
		if(frictionFactor == null) return 1;
		return frictionFactor;
	}

	public short getEffectiveSpeed(int edgeId, LocalDateTime dateTime) {
		if(dateTime != null) {
			short speed = trafficLookup.lookup(edgeId, dateTime);
			if(speed != 0) return speed;
		}
		return internalGraph.getSpeedLimit(edgeId);
	}

	public void setDates(Map<DateType, ZonedDateTime> dates) {
		this.dates = dates;
	}
	
	public Map<DateType, ZonedDateTime> getDates() {
		return dates;
	}

	// InternalGraph facade functions
	public boolean isTruckRoute(int edgeId) {
		return internalGraph.isTruckRoute(edgeId);
	}

	public RoadClass getRoadClass(int edgeId) {
		return internalGraph.getRoadClass(edgeId);
	}

	public short getSpeedLimit(int edgeId) {
		return internalGraph.getSpeedLimit(edgeId);
	}

	public boolean getReversed(int edgeId) {
		return internalGraph.getReversed(edgeId);
	}

	public int getFromNodeId(int edgeId) {
		return internalGraph.getFromNodeId(edgeId);
	}

	public int getToNodeId(int edgeId) {
		return internalGraph.getToNodeId(edgeId);
	}

	public int numEdges() {
		return internalGraph.numEdges();
	}

	public TrafficImpactor getToImpactor(int edgeId) {
		return internalGraph.getToImpactor(edgeId);
	}

	public TrafficImpactor getFromImpactor(int edgeId) {
		return internalGraph.getFromImpactor(edgeId);
	}

	public XingClass getXingClass(int edgeId) {
		return internalGraph.getXingClass(edgeId);
	}

	public int nextEdge(int nodeId, int edgeId) {
		return internalGraph.nextEdge(nodeId, edgeId);
	}

	public int getOtherEdgeId(int edgeId) {
		return internalGraph.getOtherEdgeId(edgeId);
	}

	public int getOtherNodeId(int edgeId, int nodeId) {
		return internalGraph.getOtherNodeId(edgeId, nodeId);
	}

	public double getLength(int edgeId) {
		return internalGraph.getLength(edgeId);
	}

	public double getToAngleRads(int edgeId) {
		return internalGraph.getToAngleRads(edgeId);
	}

	public double getFromAngleRads(int edgeId) {
		return internalGraph.getFromAngleRads(edgeId);
	}

	public LineString getLineString(int edgeId) {
		return internalGraph.getLineString(edgeId);
	}

	public int findClosestEdge(Point p, int snapDistance) {
		return internalGraph.findClosestEdge(p, snapDistance);
	}

	public boolean isDeadEnded(int edgeId) {
		return internalGraph.isDeadEnded(edgeId);
	}

	public List<Integer> findEdgesWithin(Envelope envelope) {
		return internalGraph.findEdgesWithin(envelope);
	}

	public int getSegmentId(int edgeId) {
		return internalGraph.getSegmentId(edgeId);
	}

	public String getName(int edgeId) {
		return internalGraph.getName(edgeId);
	}

	public String getLocality(int edgeId) {
		return internalGraph.getLocality(edgeId);
	}

	public String getOwnership(int edgeId) {
		return internalGraph.getOwnership(edgeId);
	}

	public void setSegIdLookup(IntObjectArrayMap<int[]> edgeIdBySegId) {
		this.edgeIdBySegId = edgeIdBySegId;
	}

	public boolean isMidRestriction(int edgeId) {
		return turnCostLookup.isMidRestriction(edgeId);
	}

	public List<Constraint> lookupRestriction(RestrictionSource restrictionSource, int edgeId) {
		return restrictionLookupMap.get(restrictionSource).lookup(edgeId);
	}

	public List<RoadEvent> lookupEvent(int edgeId, LocalDateTime currentDateTime) {
		return eventLookup.lookup(edgeId, currentDateTime);
	}

	public int[] lookupSchedule(int edgeId, LocalDateTime currentDateTime) {
		return scheduleLookup.lookup(edgeId, currentDateTime);
	}

	public FerryInfo getFerryInfo(int edgeId) {
		return scheduleLookup.getFerryInfo(edgeId);
	}

	public TurnDirection lookupTurn(QueryGraph queryGraph, int edgeId, DijkstraWalker walker, LocalDateTime currentDateTime,
			VehicleType vehicleType, boolean enabled) {
		return turnCostLookup.lookupTurn(queryGraph, edgeId, walker, currentDateTime, vehicleType, enabled);
	}

}

