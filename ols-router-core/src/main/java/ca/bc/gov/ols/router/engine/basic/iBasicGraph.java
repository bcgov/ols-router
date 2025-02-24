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

public interface iBasicGraph {

	boolean isTruckRoute(int edgeId);

	double getLocalDistortion(int edgeId, VehicleType vehicleType);

	RoadClass getRoadClass(int edgeId);

	short getSpeedLimit(int edgeId);
	
	short getEffectiveSpeed(int edgeId, LocalDateTime dateTime);

	int getFromNodeId(int startEdgeId);

	boolean getReversed(int startEdgeId);

	int getToNodeId(int startEdgeId);

	int numEdges();

	TrafficImpactor getToImpactor(int edgeId);

	XingClass getXingClass(int edgeId);

	int nextEdge(int nodeId, int noEdge);

	boolean isMidRestriction(int edgeId);

	List<Constraint> lookupRestriction(RestrictionSource restrictionSource, int edgeId);

	List<RoadEvent> lookupEvent(int edgeId, LocalDateTime currentDateTime);

	int[] lookupSchedule(int edgeId, LocalDateTime currentDateTime);

	FerryInfo getFerryInfo(int edgeId);

	TurnDirection lookupTurn(int edgeId, DijkstraWalker walker, LocalDateTime currentDateTime, VehicleType vehicleType,
			boolean enabled);

	int getOtherEdgeId(int edgeId);

	double getLength(int edgeId);

	int getOtherNodeId(int edgeId, int nodeId);

	double getToAngleRads(int edgeId);

	double getFromAngleRads(int edgeId);

	LineString getLineString(int edgeId);

	String getLocality(int edgeId);

	String getOwnership(int edgeId);

	String getName(int edgeId);

	int getSegmentId(int edgeId);

	boolean isDeadEnded(int edgeId);

}
