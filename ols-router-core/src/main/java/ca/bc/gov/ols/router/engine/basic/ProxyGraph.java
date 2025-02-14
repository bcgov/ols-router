package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;
import java.util.List;

import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TrafficImpactor;
import ca.bc.gov.ols.router.data.RoadEvent;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.data.enums.XingClass;
import ca.bc.gov.ols.router.restrictions.Constraint;

public class ProxyGraph implements iBasicGraph {

	BasicGraph baseGraph;
	
	public ProxyGraph(BasicGraph baseGraph) {
		this.baseGraph = baseGraph;
		
	}

	@Override
	public boolean isTruckRoute(int edgeId) {
		return baseGraph.isTruckRoute(edgeId);
	}

	@Override
	public double getLocalDistortion(int edgeId, VehicleType vehicleType) {
		return baseGraph.getLocalDistortion(edgeId, vehicleType);
	}

	@Override
	public RoadClass getRoadClass(int edgeId) {
		return baseGraph.getRoadClass(edgeId);
	}

	@Override
	public short getSpeedLimit(int edgeId) {
		return baseGraph.getSpeedLimit(edgeId);
	}

	@Override
	public short getEffectiveSpeed(int edgeId, LocalDateTime dateTime) {
		return baseGraph.getEffectiveSpeed(edgeId, dateTime);
	}

	@Override
	public int getFromNodeId(int edgeId) {
		return baseGraph.getFromNodeId(edgeId);
	}

	@Override
	public boolean getReversed(int edgeId) {
		return baseGraph.getReversed(edgeId);
	}

	@Override
	public int getToNodeId(int edgeId) {
		return baseGraph.getToNodeId(edgeId);
	}

	@Override
	public int numEdges() {
		return baseGraph.numEdges();
	}

	@Override
	public TrafficImpactor getToImpactor(int edgeId) {
		return baseGraph.getToImpactor(edgeId);
	}

	@Override
	public XingClass getXingClass(int edgeId) {
		return baseGraph.getXingClass(edgeId);
	}

	@Override
	public int nextEdge(int nodeId, int edgeId) {
		return baseGraph.nextEdge(nodeId, edgeId);
	}

	@Override
	public boolean isMidRestriction(int edgeId) {
		return baseGraph.isMidRestriction(edgeId);
	}

	@Override
	public List<Constraint> lookupRestriction(RestrictionSource restrictionSource, int edgeId) {
		return baseGraph.lookupRestriction(restrictionSource, edgeId);
	}

	@Override
	public List<RoadEvent> lookupEvent(int edgeId, LocalDateTime currentDateTime) {
		return baseGraph.lookupEvent(edgeId, currentDateTime);
	}

	@Override
	public int[] lookupSchedule(int edgeId, LocalDateTime currentDateTime) {
		return baseGraph.lookupSchedule(edgeId, currentDateTime);
	}

	@Override
	public FerryInfo getFerryInfo(int edgeId) {
		return baseGraph.getFerryInfo(edgeId);
	}

	@Override
	public TurnDirection lookupTurn(int edgeId, DijkstraWalker walker, LocalDateTime currentDateTime,
			VehicleType vehicleType, boolean enabled) {
		return baseGraph.lookupTurn(edgeId, walker, currentDateTime, vehicleType, enabled);
	}

	@Override
	public int getOtherEdgeId(int edgeId) {
		return baseGraph.getOtherEdgeId(edgeId);
	}

	@Override
	public double getLength(int edgeId) {
		return baseGraph.getLength(edgeId);
	}

	@Override
	public int getOtherNodeId(int edgeId, int nodeId) {
		return baseGraph.getOtherNodeId(edgeId, nodeId);
	}

	@Override
	public double getToAngleRads(int edgeId) {
		return baseGraph.getToAngleRads(edgeId);
	}

	@Override
	public double getFromAngleRads(int edgeId) {
		return baseGraph.getFromAngleRads(edgeId);
	}

	@Override
	public LineString getLineString(int edgeId) {
		return baseGraph.getLineString(edgeId);
	}

	@Override
	public String getLocality(int edgeId) {
		return baseGraph.getLocality(edgeId);
	}

	@Override
	public String getOwnership(int edgeId) {
		return baseGraph.getOwnership(edgeId);
	}

	@Override
	public String getName(int edgeId) {
		return baseGraph.getName(edgeId);
	}

	@Override
	public int getSegmentId(int edgeId) {
		return baseGraph.getSegmentId(edgeId);
	}
	
}
