package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class QueryGraph {

	private int firstSplitEdgeId;
	private int nextEdgeId;
	private BasicGraph baseGraph;
	private ArrayList<SplitEdge> splitEdges;
	private HashMap<Point,Integer> nodeIdByPoint;
	private MapList<Integer, Integer> baseEdgeToNewEdgeLookup;
	private MapList<Integer, SplitEdge> splitNodes;
	
	public QueryGraph(BasicGraph baseGraph, RoutingParameters params) {
		this.baseGraph = baseGraph;
		firstSplitEdgeId = baseGraph.numEdges();
		// make up new edgeIds
		nextEdgeId = firstSplitEdgeId;
		List<Point> points = params.getPoints();
		if(points != null && !points.isEmpty()) {
			buildSplitEdges(params, points);
		} else {
			List<Point> allPoints = Stream.concat(params.getFromPoints().stream(), params.getToPoints().stream())
                    .collect(Collectors.toList());
			buildSplitEdges(params, allPoints);
		}
	}
	
	private void buildSplitEdges(RoutingParameters params, List<Point> points) {
		splitEdges = new ArrayList<>();
		nodeIdByPoint = new HashMap<>();
		baseEdgeToNewEdgeLookup = new MapList<>();
		splitNodes = new MapList<>();
		// loop over points, build a map from edgeId to points on that edge
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
				SplitEdge otherNewEdge = null;
				if(otherBaseEdgeId != BasicGraphInternal.NO_EDGE) {
					otherSplitEdgeId = nextEdgeId++;
					otherNewEdge = new SplitEdge(otherSplitEdgeId, otherBaseEdgeId, splitEdgeId, toNodeId, fromNodeId, splitLine, otherSplitRestrictions.get(splitLineIndex));				
				}
				SplitEdge newEdge = new SplitEdge(splitEdgeId, baseEdgeId, otherSplitEdgeId, fromNodeId, toNodeId, splitLine, splitRestrictions.get(splitLineIndex));
				// they have to be added in order or they won't be put at the index that matches their Id
				addSplitEdge(newEdge);
				if(otherNewEdge != null) {
					addSplitEdge(otherNewEdge);
				}
				
				// figure out which points are associated with which NodeId
				// and put them in the nodeIdByPoint map
				for(int p = 0; p < projectedPoints.size(); p++) {
					if(splitLine.getStartPoint().equals(projectedPoints.get(p))) {
						nodeIdByPoint.put(points.get(pointIndexesForEdge.get(p)), fromNodeId);
						continue;
					}
					if(splitLine.getEndPoint().equals(projectedPoints.get(p))) {
						nodeIdByPoint.put(points.get(pointIndexesForEdge.get(p)), toNodeId);
						continue;
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
						closestSplitDistance = dist;
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
	public int getBaseEdgeId(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).baseEdgeId();
		}
		return edgeId;
	}
	
	public int getFromNodeId(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).fromNodeId();
		}
		return baseGraph.getFromNodeId(getBaseEdgeId(edgeId));
	}

	public int getToNodeId(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).toNodeId();
		}
		return baseGraph.getToNodeId(getBaseEdgeId(edgeId));
	}

	public int getOtherNodeId(int edgeId, int nodeId) {
		if(edgeId >= firstSplitEdgeId) {
			SplitEdge edge = getSplitEdge(edgeId);
			return edge.fromNodeId() == nodeId ? edge.toNodeId() : edge.fromNodeId();
		}
		return baseGraph.getOtherNodeId(getBaseEdgeId(edgeId), nodeId);
	}

	/**
	 * If the edgeId is a splitEdge, it returns the other part of the splitEdge 
	 * on the other side of the given nodeId. 
	 * If the given edgeId is not splitEdge, or the given nodeId is not splitNode,
	 * return NO_EDGE;
	 * @param nodeId
	 * @param edgeId
	 * @return the other half of the splitEdge that is on the other side of the node, or NO_EDGE
	 */
	public int otherSplitEdge(int nodeId, int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			List<SplitEdge> splitEdges = splitNodes.get(nodeId);
			int baseEdgeId = getSplitEdge(edgeId).baseEdgeId();
			for(SplitEdge splitEdge : splitEdges) {
				if(splitEdge.newEdgeId() != edgeId && splitEdge.baseEdgeId() == baseEdgeId) {
					return splitEdge.newEdgeId();
				}
			}
		}
		return BasicGraphInternal.NO_EDGE;
	}
	
	public int nextEdge(int nodeId, int edgeId) {
		List<SplitEdge> splitEdges = splitNodes.get(nodeId);
		if(splitEdges != null) {
			// if the nodeId < 0 it is a split node, there are no more than two outgoing edges 
			if(nodeId < 0) {
				// identify the up to two outgoing edges
				int firstEdgeId = BasicGraphInternal.NO_EDGE;
				int lastEdgeId = BasicGraphInternal.NO_EDGE;
				for(SplitEdge edge : splitEdges) {
					if(edge.fromNodeId() == nodeId) {
						if (firstEdgeId == BasicGraphInternal.NO_EDGE || edge.newEdgeId() < firstEdgeId) {
							firstEdgeId = edge.newEdgeId();
						} else if (lastEdgeId == BasicGraphInternal.NO_EDGE || edge.newEdgeId() > lastEdgeId){
							lastEdgeId = edge.newEdgeId();
						}
					}
				}
				// return the appropriate edgeId
				if(edgeId == BasicGraphInternal.NO_EDGE) {
					return firstEdgeId;
				}
				if(edgeId == firstEdgeId && firstEdgeId != lastEdgeId) {
					return lastEdgeId;
				}
				return BasicGraphInternal.NO_EDGE;
			} else {
				// this is a regular node that is at the end of a split segment
				// check what the next edge would normally be
				int nextEdgeId = baseGraph.nextEdge(nodeId, getBaseEdgeId(edgeId));
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
		return baseGraph.nextEdge(nodeId, getBaseEdgeId(edgeId));
	}

	public int getOtherEdgeId(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).otherEdgeId();
		}
		return baseGraph.getOtherEdgeId(getBaseEdgeId(edgeId));
	}

	public LineString getLineString(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).lineString();
		}
		return baseGraph.getLineString(edgeId);
	}

	public LineString getBaseLineString(int edgeId) {
		return baseGraph.getLineString(getBaseEdgeId(edgeId));
	}

	public double getLength(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			return getSplitEdge(edgeId).lineString().getLength();
		}
		return baseGraph.getLength(edgeId);
	}

	public TrafficImpactor getToImpactor(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			SplitEdge splitEdge = getSplitEdge(edgeId);
			if(baseGraph.getReversed(splitEdge.baseEdgeId())) {
				if(splitEdge.fromNodeId() < 0) return TrafficImpactor.NONE;
			} else {
				if(splitEdge.toNodeId() < 0) return TrafficImpactor.NONE;
			}
		}
		return baseGraph.getToImpactor(getBaseEdgeId(edgeId));
	}

	public int findNodeId(Point p) {
		Integer nodeId = nodeIdByPoint.get(p);
		if(nodeId == null) {
			return BasicGraphInternal.NO_NODE;
		}
		return nodeId;
	}

	public TurnDirection lookupTurn(int nextEdgeId, DijkstraWalker walker, LocalDateTime currentDateTime,
			VehicleType vehicleType, boolean enabled) {
		// no turn costs or restrictions on internal splits
		if(walker.nodeId() < 0) {
			return TurnDirection.CENTER;
		}
		return baseGraph.getTurnLookup().lookupTurn(this, nextEdgeId, walker, currentDateTime, vehicleType, enabled);
	}

	public List<Constraint> lookupRestriction(RestrictionSource restrictionSource, int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			getSplitEdge(edgeId).restrictions().get(restrictionSource);
		}
		return baseGraph.lookupRestriction(restrictionSource, getBaseEdgeId(edgeId));
	}

	public List<RoadEvent> lookupEvent(int edgeId, LocalDateTime currentDateTime) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO how can we still apply an event if we split the seg?
		}
		return baseGraph.lookupEvent(getBaseEdgeId(edgeId), currentDateTime);
	}

	public int[] lookupSchedule(int edgeId, LocalDateTime currentDateTime) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO how can we still apply a schedule if we split the seg?
		}
		return baseGraph.lookupSchedule(getBaseEdgeId(edgeId), currentDateTime);
	}

	public FerryInfo getFerryInfo(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO how can we still apply ferryInfo if we split the ferry seg?
		}
		return baseGraph.getFerryInfo(getBaseEdgeId(edgeId));
	}

	public double getToAngleRads(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO internal split angles need to be calculated
		}
		return baseGraph.getToAngleRads(getBaseEdgeId(edgeId));
	}

	public double getFromAngleRads(int edgeId) {
		if(edgeId >= firstSplitEdgeId) {
			// TODO internal split angles need to be calculated
		}
		return baseGraph.getFromAngleRads(getBaseEdgeId(edgeId));
	}

	public boolean isMidRestriction(int edgeId) {
		return baseGraph.isMidRestriction(getBaseEdgeId(edgeId));
	}

	public XingClass getXingClass(int edgeId) {
		return baseGraph.getXingClass(getBaseEdgeId(edgeId));
	}

	public String getLocality(int edgeId) {
		return baseGraph.getLocality(getBaseEdgeId(edgeId));
	}

	public String getOwnership(int edgeId) {
		return baseGraph.getOwnership(getBaseEdgeId(edgeId));
	}

	public String getName(int edgeId) {
		return baseGraph.getName(getBaseEdgeId(edgeId));
	}

	public int getSegmentId(int edgeId) {
		return baseGraph.getSegmentId(getBaseEdgeId(edgeId));
	}

	public boolean isTruckRoute(int edgeId) {
		return baseGraph.isTruckRoute(getBaseEdgeId(edgeId));
	}

	public double getLocalDistortion(int edgeId, VehicleType vehicleType) {
		return baseGraph.getLocalDistortion(getBaseEdgeId(edgeId), vehicleType);
	}

	public RoadClass getRoadClass(int edgeId) {
		return baseGraph.getRoadClass(getBaseEdgeId(edgeId));
	}

	public short getSpeedLimit(int edgeId) {
		return baseGraph.getSpeedLimit(getBaseEdgeId(edgeId));
	}

	public short getEffectiveSpeed(int edgeId, LocalDateTime dateTime) {
		return baseGraph.getEffectiveSpeed(getBaseEdgeId(edgeId), dateTime);
	}

	public boolean getReversed(int edgeId) {
		return baseGraph.getReversed(getBaseEdgeId(edgeId));
	}

	public boolean isDeadEnded(int edgeId) {
		return baseGraph.isDeadEnded(getBaseEdgeId(edgeId));
	}

	public int numEdges() {
		return nextEdgeId;
	}

}


