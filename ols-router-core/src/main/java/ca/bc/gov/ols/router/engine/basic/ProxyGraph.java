package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TrafficImpactor;
import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.RoadEvent;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.data.enums.XingClass;
import ca.bc.gov.ols.router.restrictions.Constraint;
import ca.bc.gov.ols.util.LineStringSplitter;
import ca.bc.gov.ols.util.MapList;


record SplitEdge(
		int newEdgeId, 
		int baseEdgeId,
		int otherEdgeId,
		int fromNodeId, 
		int toNodeId,
		LineString lineString,
		MapList<RestrictionSource,Constraint> restrictions) {}

/**
 * Used to model a "virtual" graph that behaves like regular graph, 
 * but has edges split where start/stop points are located.
 * This handles all of the special case logic for splitting start/end segments,
 * and allows the graph algorithms to ignore the details of the splits.
 * The "virtual" split edges are given negative edgeIds to make it easy to identify them.
 * The split points are also give negative nodeIds for the same reason.
 */
public class ProxyGraph implements iBasicGraph {

	int firstSplitEdgeId;
	int nextEdgeId;
	BasicGraph baseGraph;
	ArrayList<SplitEdge> splitEdges;
	HashMap<Point,Integer> nodeIdByPoint;
	MapList<Integer, Integer> baseEdgeToNewEdgeLookup;
	MapList<Integer, SplitEdge> splitNodes;
	
	public ProxyGraph(BasicGraph baseGraph, RoutingParameters params) {
		this.baseGraph = baseGraph;
		firstSplitEdgeId = baseGraph.numEdges();
		// make up new edgeIds
		nextEdgeId = firstSplitEdgeId;
		buildSplitEdges(params);
	}
	
	private void buildSplitEdges(RoutingParameters params) {
		splitEdges = new ArrayList<>();
		nodeIdByPoint = new HashMap<>();
		baseEdgeToNewEdgeLookup = new MapList<>();
		splitNodes = new MapList<>();
		// loop over points, build a map from edgeId to points on that edge
		List<Point> points = params.getPoints();
		MapList<Integer,Integer> pointIdxByEdgeId = new MapList<>();
		for(int pointIdx = 0; pointIdx < points.size(); pointIdx++) {
			int edgeId = baseGraph.findClosestEdge(points.get(pointIdx), params.getSnapDistance());
			if(edgeId != BasicGraphInternal.NO_EDGE) {
				pointIdxByEdgeId.add(edgeId, pointIdx);
			} 
		}
		
		// make up new nodeIds, starting at -1 and going further negative
		int nextNodeId = -1;
		
		// loop over the map and split the edge based on the points
		for(Entry<Integer, List<Integer>> entry : pointIdxByEdgeId.entrySet()) {
			// split the edges into as many pieces as needed based on the points
			int baseEdgeId = entry.getKey();
			List<Integer> pointIndexesForEdge = entry.getValue();
			
			// if this is not a 1-way segment, there is a reverse edge as well
			int otherBaseEdgeId = baseGraph.getOtherEdgeId(baseEdgeId);
			
			// make sure that edgeId is the forward edge and otherEdgeId is the reversed edge
			if(baseGraph.getReversed(baseEdgeId)) {
				int saveEdgeId = otherBaseEdgeId;
				otherBaseEdgeId = baseEdgeId;
				baseEdgeId = saveEdgeId;
			}

			List<Point> pointsForEdge = pointIndexesForEdge.stream().map(idx -> points.get(idx)).toList();
			List<Point> projectedPoints = new ArrayList<>(pointIndexesForEdge.size()); 
			List<LineString> splitLines = LineStringSplitter.split(baseGraph.getLineString(baseEdgeId), pointsForEdge, projectedPoints);
			
			// divide up any restrictions to the appropriate splitEdge
			List<MapList<RestrictionSource, Constraint>> splitRestrictions = splitRestrictions(baseEdgeId, splitLines);
			List<MapList<RestrictionSource, Constraint>> otherSplitRestrictions = null;
			if(otherBaseEdgeId != BasicGraphInternal.NO_EDGE) {
				otherSplitRestrictions = splitRestrictions(otherBaseEdgeId, splitLines);
			}
			
			// create records to keep track of the new edgeIds, nodeIds, and lineStrings
			for(int splitLineIndex = 0; splitLineIndex < splitLines.size(); splitLineIndex++) {
				LineString splitLine = splitLines.get(splitLineIndex);
				int splitEdgeId = nextEdgeId++;
				
				// for the first split section in a lineString, use the existing fromNode 
				// otherwise use the toNode from the previous split
				int fromNodeId = splitLineIndex == 0 ? baseGraph.getFromNodeId(baseEdgeId) : nextNodeId + 1;
				
				// for the last split section in a linestring use the existing toNode 
				// otherwise allocate a new nodeId
				int toNodeId = splitLineIndex == splitLines.size() - 1 ? baseGraph.getToNodeId(baseEdgeId) : nextNodeId--;
				
				int otherSplitEdgeId = BasicGraphInternal.NO_EDGE;
				
				// create the reverse direction edge if needed
				if(otherBaseEdgeId != BasicGraphInternal.NO_EDGE) {
					otherSplitEdgeId = nextEdgeId++;
					SplitEdge otherNewEdge = new SplitEdge(otherSplitEdgeId, otherBaseEdgeId, splitEdgeId, toNodeId, fromNodeId, splitLine, splitRestrictions.get(splitLineIndex));
					addSplitEdge(otherNewEdge);					
				}
				SplitEdge newEdge = new SplitEdge(splitEdgeId, baseEdgeId, otherSplitEdgeId, fromNodeId, toNodeId, splitLine, otherSplitRestrictions.get(splitLineIndex));
				addSplitEdge(newEdge);
				
				// figure out which points are associated with which NodeId
				// and put them in the nodeIdByPoint map
				for(int p = 0; p < projectedPoints.size(); p++) {
					if(splitLine.getStartPoint().equals(projectedPoints.get(p))) {
						nodeIdByPoint.put(points.get(pointIndexesForEdge.get(p)), fromNodeId);
						break;
					}
					if(splitLine.getEndPoint().equals(projectedPoints.get(p))) {
						nodeIdByPoint.put(points.get(pointIndexesForEdge.get(p)), toNodeId);
					}
				}
			}
		}
	}

	private List<MapList<RestrictionSource, Constraint>> splitRestrictions(int baseEdgeId,
			List<LineString> splitLines) {
		List<MapList<RestrictionSource,Constraint>> splitRestrictions = new ArrayList<>(splitLines.size());
		for(int i = 0; i < splitLines.size(); i++) {
			splitRestrictions.add(new MapList<>());
		}
		for(RestrictionSource source : RestrictionSource.values()) {
			List<Constraint> constraints = baseGraph.lookupRestriction(source, baseEdgeId);
			for(Constraint c : constraints) {
				int closestSplitIndex = 0;
				double closestSplitDistance = Double.POSITIVE_INFINITY;
				for(int splitLineIndex = 0; splitLineIndex < splitLines.size(); splitLineIndex++) {
					double dist = c.getLocation().distance(splitLines.get(splitLineIndex));
					if(dist < closestSplitDistance) {
						closestSplitIndex = splitLineIndex;
					}
				}
				splitRestrictions.get(closestSplitIndex).add(source, c);
			}
		}
		return splitRestrictions;
	}
	
	private void addSplitEdge(SplitEdge splitEdge) {
		splitEdges.add(splitEdge);
		baseEdgeToNewEdgeLookup.add(splitEdge.baseEdgeId(), splitEdge.newEdgeId());
		splitNodes.add(splitEdge.fromNodeId(), splitEdge);
		splitNodes.add(splitEdge.toNodeId(), splitEdge);
	}
	
	/**
	 * Takes a splitEdgeId and returns the associated splitEdge record.
	 * 
	 * @param edgeId a splitEdgeId >= firstSplitEdgeId
	 * @return the associated splitEdge record
	 */
	private SplitEdge getSplitEdge(int edgeId) {
		return splitEdges.get(edgeId - firstSplitEdgeId);
	}

	/**
	 * For any given edgeId, return the edgeId from baseGraph on which it is based. 
	 * For SplitEdgeId, ie. >= firstSplitEdgeId, maps to the baseEdgeId. Otherwise returns the same Id as input.
	 * 
	 * @param edgeId a valid edgeId, split or base
	 * @return the base edgeId that can be looked up in the baseGraph
	 */
	private int mapEdgeId(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).baseEdgeId();
		}
		return edgeId;
	}
	
	@Override
	public int getFromNodeId(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).fromNodeId();
		}
		return baseGraph.getFromNodeId(mapEdgeId(edgeId));
	}

	@Override
	public int getToNodeId(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).toNodeId();
		}
		return baseGraph.getToNodeId(mapEdgeId(edgeId));
	}

	@Override
	public int getOtherNodeId(int edgeId, int nodeId) {
		if(edgeId >= firstSplitEdgeId) {
			SplitEdge edge = getSplitEdge(edgeId);
			return edge.fromNodeId() == nodeId ? edge.toNodeId() : edge.fromNodeId();
		}
		return baseGraph.getOtherNodeId(mapEdgeId(edgeId), nodeId);
	}

	@Override
	public int nextEdge(int nodeId, int edgeId) {
		List<SplitEdge> edges = splitNodes.get(nodeId);
		if(edges != null) {
			// if the nodeId < 0 it is a split node, there is only one other outgoing edge 
			if(nodeId < 0) {
				for(SplitEdge edge : edges) {
					if(edge.fromNodeId() == nodeId && edge.newEdgeId() != edgeId) {
						return edge.newEdgeId();
					}
				}
			} else {
				// this is a regular node that is at the end of a split segment
				// check what the next edge would normally be
				int nextEdgeId = baseGraph.nextEdge(nodeId, mapEdgeId(edgeId));
				List<Integer> newEdgeIds = baseEdgeToNewEdgeLookup.get(nextEdgeId);
				if(newEdgeIds != null) {
					// the next edge is a split edge, find the right split
					for(int newEdgeId : newEdgeIds) {
						if(getSplitEdge(newEdgeId).fromNodeId() == nodeId) {
							return newEdgeId;
						}
					}
				}
			}
		}
		return baseGraph.nextEdge(nodeId, mapEdgeId(edgeId));
	}

	@Override
	public int getOtherEdgeId(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).otherEdgeId();
		}
		return baseGraph.getOtherEdgeId(mapEdgeId(edgeId));
	}

	@Override
	public LineString getLineString(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).lineString();
		}
		return baseGraph.getLineString(edgeId);
	}

	@Override
	public double getLength(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).lineString().getLength();
		}
		return baseGraph.getLength(edgeId);
	}

	@Override
	public TrafficImpactor getToImpactor(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			SplitEdge splitEdge = getSplitEdge(edgeId);
			if(baseGraph.getReversed(splitEdge.baseEdgeId())) {
				if(splitEdge.fromNodeId() < 0) return null;
			} else {
				if(splitEdge.toNodeId() < 0) return null;
			}
		}
		return baseGraph.getToImpactor(mapEdgeId(edgeId));
	}

	public int findNodeId(Point p) {
		return nodeIdByPoint.get(p);
	}

	@Override
	public TurnDirection lookupTurn(int edgeId, DijkstraWalker walker, LocalDateTime currentDateTime,
			VehicleType vehicleType, boolean enabled) {
		// no turn costs or restrictions on internal splits
		if(walker.getNodeId() < 0) {
			return TurnDirection.CENTER;
		}
		// for external ends fallback to the baseGraph
		return baseGraph.lookupTurn(mapEdgeId(edgeId), walker, currentDateTime, vehicleType, enabled);
	}

	@Override
	public List<Constraint> lookupRestriction(RestrictionSource restrictionSource, int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			getSplitEdge(edgeId).restrictions().get(restrictionSource);
		}
		return baseGraph.lookupRestriction(restrictionSource, mapEdgeId(edgeId));
	}

	@Override
	public List<RoadEvent> lookupEvent(int edgeId, LocalDateTime currentDateTime) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO how can we still apply a schedule if we split the seg?
		}
		return baseGraph.lookupEvent(mapEdgeId(edgeId), currentDateTime);
	}

	@Override
	public int[] lookupSchedule(int edgeId, LocalDateTime currentDateTime) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO how can we still apply a schedule if we split the seg?
		}
		return baseGraph.lookupSchedule(mapEdgeId(edgeId), currentDateTime);
	}

	@Override
	public FerryInfo getFerryInfo(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO how can we still apply ferryInfo if we split the ferry seg?
		}
		return baseGraph.getFerryInfo(mapEdgeId(edgeId));
	}

	@Override
	public double getToAngleRads(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO internal split angles need to be calculated
		}
		return baseGraph.getToAngleRads(mapEdgeId(edgeId));
	}

	@Override
	public double getFromAngleRads(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO internal split angles need to be calculated
		}
		return baseGraph.getFromAngleRads(mapEdgeId(edgeId));
	}

	@Override
	public boolean isMidRestriction(int edgeId) {
		return baseGraph.isMidRestriction(mapEdgeId(edgeId));
	}

	@Override
	public XingClass getXingClass(int edgeId) {
		return baseGraph.getXingClass(mapEdgeId(edgeId));
	}

	@Override
	public String getLocality(int edgeId) {
		return baseGraph.getLocality(mapEdgeId(edgeId));
	}

	@Override
	public String getOwnership(int edgeId) {
		return baseGraph.getOwnership(mapEdgeId(edgeId));
	}

	@Override
	public String getName(int edgeId) {
		return baseGraph.getName(mapEdgeId(edgeId));
	}

	@Override
	public int getSegmentId(int edgeId) {
		return baseGraph.getSegmentId(mapEdgeId(edgeId));
	}

	@Override
	public boolean isTruckRoute(int edgeId) {
		return baseGraph.isTruckRoute(mapEdgeId(edgeId));
	}

	@Override
	public double getLocalDistortion(int edgeId, VehicleType vehicleType) {
		return baseGraph.getLocalDistortion(mapEdgeId(edgeId), vehicleType);
	}

	@Override
	public RoadClass getRoadClass(int edgeId) {
		return baseGraph.getRoadClass(mapEdgeId(edgeId));
	}

	@Override
	public short getSpeedLimit(int edgeId) {
		return baseGraph.getSpeedLimit(mapEdgeId(edgeId));
	}

	@Override
	public short getEffectiveSpeed(int edgeId, LocalDateTime dateTime) {
		return baseGraph.getEffectiveSpeed(mapEdgeId(edgeId), dateTime);
	}

	@Override
	public boolean getReversed(int edgeId) {
		return baseGraph.getReversed(mapEdgeId(edgeId));
	}

	@Override
	public boolean isDeadEnded(int edgeId) {
		return baseGraph.isDeadEnded(mapEdgeId(edgeId));
	}

	@Override
	public int numEdges() {
		return nextEdgeId;
	}

}


