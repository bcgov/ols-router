/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.ItemBoundable;
import org.locationtech.jts.index.strtree.ItemDistance;
import org.locationtech.jts.index.strtree.STRtree;

import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.router.data.enums.TrafficImpactor;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.data.enums.XingClass;
import ca.bc.gov.ols.router.data.vis.VisLayers;
import ca.bc.gov.ols.util.IntObjectArrayMap;
import gnu.trove.map.TIntObjectMap;

public class BasicGraph {
	private static final Logger logger = LoggerFactory.getLogger(BasicGraph.class.getCanonicalName());
	
	public static final int NO_EDGE = -1;
	public static final int NO_NODE = -1;
	public static final double MAX_SNAP_DISTANCE = 1000;
	
	private int nextNodeId = 0;
	private int nextEdgeId = 0;
	
	private IntObjectArrayMap<Node> nodes;
	private IntObjectArrayMap<Edge> edges;
	private STRtree spatialIndex;
	private TurnLookup turnCostLookup;
	private EventLookup eventLookup;
	private TrafficLookup trafficLookup;
	private ScheduleLookup scheduleLookup;
	TIntObjectMap<EnumMap<VehicleType,Double>> localDistortionField;
	private VisLayers visLayers;

	public BasicGraph(int initialEdgeCapacity) {
		nodes = new IntObjectArrayMap<Node>(initialEdgeCapacity/2);
		edges = new IntObjectArrayMap<Edge>(initialEdgeCapacity);
		spatialIndex = new STRtree();
	}
	
	public void build() {
		spatialIndex.build();
	}
	
	public int addNode(Point p) {
		int nodeId = nextNodeId++;
		nodes.put(nodeId, new Node(p));
		return nodeId;
	}
	
	public int[] addEdge(int fromNodeId, int toNodeId, LineString ls, 
			boolean oneWay, short speedLimit, 
			String leftLocality, String rightLocality, String name, 
			RoadClass roadClass, TrafficImpactor fromImp, TrafficImpactor toImp,
			double maxHeight, double maxWidth, Integer fromMaxWeight, Integer toMaxWeight,
			boolean isTruckRoute, XingClass fromXingClass, XingClass toXingClass, boolean isDeadEnded) {
		EdgeData data = new EdgeData(ls, speedLimit, leftLocality, rightLocality, name, roadClass, fromImp, toImp,
				maxHeight, maxWidth, fromMaxWeight, toMaxWeight, isTruckRoute, fromXingClass, toXingClass, isDeadEnded);
		int[] edgeIds = new int[(oneWay?1:2)];
		edgeIds[0] = createEdge(fromNodeId, toNodeId, data, false);
		if(!oneWay) {
			edgeIds[1] = createEdge(toNodeId, fromNodeId, data, true);
			edges.get(edgeIds[0]).setOtherEdgeId(edgeIds[1]);
			edges.get(edgeIds[1]).setOtherEdgeId(edgeIds[0]);
		}
		return edgeIds;
	}
	
	private int createEdge(int fromNodeId, int toNodeId, EdgeData data, boolean reversed) {
		int edgeId = nextEdgeId++; 
		Edge forwardEdge = new Edge(fromNodeId, toNodeId, data, reversed);
		edges.put(edgeId, forwardEdge);
		spatialIndex.insert(data.ls.getEnvelopeInternal(), edgeId);
		insertEdge(fromNodeId, edgeId);
		//all edges are one-way, so only need to add to the from node
//		insertEdge(toNodeId, edgeId);
		return edgeId;
	}

	private void insertEdge(int nodeId, int edgeId) {
		Node node = nodes.get(nodeId);
		int lastEdgeId = nodes.get(nodeId).firstEdge;
		if(lastEdgeId == NO_EDGE) {
			node.firstEdge = edgeId;
		} else {
			while(edges.get(lastEdgeId).nextEdge(nodeId) != NO_EDGE) {
				lastEdgeId = edges.get(lastEdgeId).nextEdge(nodeId);
			}
			// don't add it if it is a loop edge
			if(lastEdgeId != edgeId) {
				edges.get(lastEdgeId).setNextEdge(nodeId, edgeId);
			}
		}		
	}
	
	public int findClosestEdge(Point point) {
		int closestEdgeId = (Integer)spatialIndex.nearestNeighbour(point.getEnvelopeInternal(), NO_EDGE, new ItemDistance() {
			@Override
			public double distance(ItemBoundable item1, ItemBoundable item2) {
				Geometry geom1;
				Geometry geom2;
				int edge1 = (Integer)item1.getItem();
				if(edge1 == NO_EDGE) {
					geom1 = point;
				} else {
					geom1 = getLineString(edge1);
				}
				int edge2 = (Integer)item2.getItem();
				if(edge2 == NO_EDGE) {
					geom2 = point;
				} else {
					geom2 = getLineString(edge2);
				}
				return geom1.distance(geom2);
			}
		});
		if(point.distance(getLineString(closestEdgeId)) > MAX_SNAP_DISTANCE) {
			return NO_EDGE;
		}
		return closestEdgeId;
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findEdgesWithin(Envelope env) {
		return spatialIndex.query(env);
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
	
	public double getLength(int edgeId) {
		return edges.get(edgeId).data.length;
	}

	public float getFromAngleRads(int edgeId) {
		if(edges.get(edgeId).reversed) {
			return edges.get(edgeId).data.toAngle;			
		} else {
			return edges.get(edgeId).data.fromAngle;			
		}
	}

	public float getToAngleRads(int edgeId) {
		if(edges.get(edgeId).reversed) {
			return edges.get(edgeId).data.fromAngle;			
		} else {
			return edges.get(edgeId).data.toAngle;			
		}
	}

	public LineString getLineString(int edgeId) {
		return edges.get(edgeId).data.ls;
	}

	public int getFromNodeId(int edgeId) {
		return edges.get(edgeId).fromNodeId;
	}

	public int getToNodeId(int edgeId) {
		return edges.get(edgeId).toNodeId;
	}

	public int getOtherNodeId(int edgeId, int nodeId) {
		return edges.get(edgeId).otherNodeId(nodeId);
	}

	public int getOtherEdgeId(int edgeId) {
		return edges.get(edgeId).otherEdgeId;
	}
	
	public boolean getReversed(int edgeId) {
		return edges.get(edgeId).reversed;
	}
	
	public short getSpeedLimit(int edgeId) {
		return edges.get(edgeId).data.speedLimit;
	}
	
	public short getEffectiveSpeed(int edgeId, LocalDateTime dateTime) {
		if(dateTime != null) {
			short speed = trafficLookup.lookup(edgeId, dateTime);
			if(speed != 0) return speed;
		}
		return edges.get(edgeId).data.speedLimit;
	}

	public String getName(int edgeId) {
		return edges.get(edgeId).data.name;
	}

	public String getLocality(int edgeId) {
		Edge edge = edges.get(edgeId);
		if(edge.reversed) {
			return edges.get(edgeId).data.leftLocality;
		}
		return edge.data.rightLocality;
	}

	public TrafficImpactor getFromImpactor(int edgeId) {
		Edge edge = edges.get(edgeId);
		if(edge.reversed) {
			return edge.data.toImp;
		}
		return edge.data.fromImp;
	}

	public TrafficImpactor getToImpactor(int edgeId) {
		Edge edge = edges.get(edgeId);
		if(edge.reversed) {
			return edge.data.fromImp;
		}
		return edge.data.toImp;
	}

	public double getMaxHeight(int edgeId) {
		return edges.get(edgeId).data.maxHeight;
	}

	public double getMaxWidth(int edgeId) {
		return edges.get(edgeId).data.maxWidth;
	}

	public Integer getFromMaxWeight(int edgeId) {
		Edge edge = edges.get(edgeId);
		if(edge.reversed) {
			return edges.get(edgeId).data.toMaxWeight;
		}
		return edges.get(edgeId).data.fromMaxWeight;
	}

	public Integer getToMaxWeight(int edgeId) {
		Edge edge = edges.get(edgeId);
		if(edge.reversed) {
			return edges.get(edgeId).data.fromMaxWeight;
		}
		return edges.get(edgeId).data.toMaxWeight;
	}

	public boolean isTruckRoute(int edgeId) {
		return edges.get(edgeId).data.isTruckRoute;
	}

	public XingClass getXingClass(int edgeId) {
		Edge edge = edges.get(edgeId);
		if(edge.reversed) {
			return edges.get(edgeId).data.fromXingClass;
		}
		return edges.get(edgeId).data.toXingClass;
	}

	public boolean isDeadEnded(int edgeId) {
		return edges.get(edgeId).data.isDeadEnded;
	}

	public int numEdges() {
		return nextEdgeId;
	}
	
	public int numNodes() {
		return nextNodeId;
	}

	public int nextEdge(int nodeId, int edgeId) {
		if(edgeId == BasicGraph.NO_EDGE) {
			return nodes.get(nodeId).firstEdge;
		} else {
			return edges.get(edgeId).nextEdge(nodeId);
		}
	}

	void setSpeedLimit(int edgeId, short speedLimit) {
		if(speedLimit <= 0) {
			logger.error("Illegal attempt to set speed to 0.");
			return;
		}
		edges.get(edgeId).data.speedLimit = speedLimit;
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

	public RoadClass getRoadClass(int edgeId) {
		return edges.get(edgeId).data.roadClass;
	}

}

class Node {
	Point p;
	int firstEdge;
	
	public Node(Point p) {
		this.p = p;
		firstEdge = BasicGraph.NO_EDGE;
	}
}

class EdgeData {
	final LineString ls;
	final double length;
	final float fromAngle;
	final float toAngle;
	short speedLimit;
	final String leftLocality;
	final String rightLocality;
	final String name;
	final RoadClass roadClass;
	final TrafficImpactor fromImp;
	final TrafficImpactor toImp;
	final double maxHeight;
	final double maxWidth;
	final Integer fromMaxWeight;
	final Integer toMaxWeight;
	final boolean isTruckRoute;
	final XingClass fromXingClass;
	final XingClass toXingClass;
	final boolean isDeadEnded;
	
	public EdgeData(LineString ls, short speedLimit, 
			String leftLocality, String rightLocality, String name, 
			RoadClass roadClass, TrafficImpactor fromImp, TrafficImpactor toImp,
			double maxHeight, double maxWidth, Integer fromMaxWeight, Integer toMaxWeight,
			boolean isTruckRoute, XingClass fromXingClass, XingClass toXingClass, boolean isDeadEnded) {
		this.ls = ls;
		this.length = ls.getLength();
		this.fromAngle = (float) Angle.angle(ls.getCoordinateN(0), ls.getCoordinateN(1));
		int numCoords = ls.getNumPoints();
		this.toAngle = (float) Angle.angle(ls.getCoordinateN(numCoords-1), ls.getCoordinateN(numCoords-2));
		this.speedLimit = speedLimit;
		this.leftLocality = leftLocality;
		this.rightLocality = rightLocality;
		this.name = name;
		this.roadClass = roadClass;
		this.fromImp = fromImp;
		this.toImp = toImp;
		this.maxHeight = maxHeight;     
		this.maxWidth = maxWidth;      
		this.fromMaxWeight = fromMaxWeight;    
		this.toMaxWeight = toMaxWeight;    
		this.isTruckRoute = isTruckRoute;
		this.fromXingClass = fromXingClass;
		this.toXingClass = toXingClass;
		this.isDeadEnded = isDeadEnded;
	}
}

class Edge {
	final int fromNodeId;
	final int toNodeId;
	int nextFromEdgeId = BasicGraph.NO_EDGE;
	int nextToEdgeId = BasicGraph.NO_EDGE;
	final EdgeData data;
	final boolean reversed;
	int otherEdgeId = BasicGraph.NO_EDGE;

	public Edge(int fromNodeId, int toNodeId, EdgeData data, boolean reversed) {
		this.fromNodeId = fromNodeId;
		this.toNodeId = toNodeId;
		this.data = data;
		this.reversed = reversed;
	}
	
	int otherNodeId(int nodeId) {
		if(fromNodeId == nodeId) {
			return toNodeId;
		} else if(toNodeId == nodeId) {
			return fromNodeId;
		}
		throw new RuntimeException("Cannot ask for other node of a Node Id that this edge doesn't connect to.");

	}

	int nextEdge(int nodeId) {
		if(nodeId == fromNodeId) {
			return nextFromEdgeId;
		} else if(nodeId == toNodeId) {
			return nextToEdgeId;
		} 
		throw new RuntimeException("Cannot ask for next of a Node Id that this edge doesn't connect to.");
	}
	
	void setNextEdge(int nodeId, int edgeId) {
		if(nodeId == fromNodeId) {
			assert(nextFromEdgeId == BasicGraph.NO_EDGE);
			nextFromEdgeId = edgeId;
		} else if(nodeId == toNodeId) {
			assert(nextToEdgeId == BasicGraph.NO_EDGE);
			nextToEdgeId = edgeId;
		} else { 
			throw new RuntimeException("Cannot set the next edge of a Node Id that this edge doesn't connect to.");
		}
	}
	
	void setOtherEdgeId(int otherEdgeId) {
		this.otherEdgeId = otherEdgeId;
	}

}



