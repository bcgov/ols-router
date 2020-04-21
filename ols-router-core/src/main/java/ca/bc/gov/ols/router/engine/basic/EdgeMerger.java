/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.RoadEvent;
import ca.bc.gov.ols.router.data.RoadTruckNoticeEvent;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.directions.AbstractTravelDirection;
import ca.bc.gov.ols.router.directions.CardinalDirection;
import ca.bc.gov.ols.router.directions.Direction;
import ca.bc.gov.ols.router.directions.FerryDirection;
import ca.bc.gov.ols.router.directions.FinishDirection;
import ca.bc.gov.ols.router.directions.Partition;
import ca.bc.gov.ols.router.directions.StartDirection;
import ca.bc.gov.ols.router.directions.StopoverDirection;
import ca.bc.gov.ols.router.directions.StreetDirection;
import ca.bc.gov.ols.router.directions.StreetDirectionType;
import ca.bc.gov.ols.router.notifications.EventWaitNotification;
import ca.bc.gov.ols.router.notifications.FerryWaitNotification;
import ca.bc.gov.ols.router.notifications.Notification;
import ca.bc.gov.ols.router.notifications.OversizeNotification;
import ca.bc.gov.ols.router.notifications.TruckNotification;

public class EdgeMerger {
	private static final Logger logger = LoggerFactory.getLogger(EdgeMerger.class.getCanonicalName());
	
	private final BasicGraph graph;
	private final EdgeList[] edgeLists;
	private final RoutingParameters params;
	private boolean calcRoute = false;
	private boolean calcDirections = false;
	private EnumSet<Attribute> partitionAttributes = null;
	private double dist = 0;
	private double time = 0;
	private LineString route;
	private List<Direction> directions;
	private Set<Notification> notifications;
	private List<Partition> partitions;
	private EnumMap<Attribute,Object> partitionValues = null;
	
	public EdgeMerger(EdgeList[] edgeLists, BasicGraph graph, RoutingParameters params) {
		this.edgeLists = edgeLists;
		this.graph = graph;
		partitionAttributes = params.getPartition();
		this.params = params;
	}

	private void mergeEdges(GeometryFactory gf) {
		List<Coordinate> coords = new ArrayList<Coordinate>();
		AbstractTravelDirection curDir = null;
		if(calcDirections) {
			directions = new ArrayList<Direction>();
			notifications = new HashSet<Notification>();
		}
		if(partitionAttributes != null) {
			this.partitionValues = new EnumMap<Attribute,Object>(Attribute.class);
			partitions = new ArrayList<Partition>();
		}
		
		// for each edgeList (ie. each leg of the route)
		for(int edgeListIdx = 0; edgeListIdx < edgeLists.length; edgeListIdx++) {
			EdgeList edges = edgeLists[edgeListIdx];
			// for each edge
			for(int edgeIdx = edges.size()-1; edgeIdx >= 0; edgeIdx--) {
				int edgeId = edges.edgeId(edgeIdx);
				LineString ls;
				String curName = null;
				if(edgeIdx == edges.size()-1) {
					// start edge
					if(graph.getReversed(edgeId)) {
						ls = edges.getStartEdge().getFromSplit();
					} else {
						ls = edges.getStartEdge().getToSplit();
					}
				} else if(edgeIdx == 0) {
					// end edge
					if(graph.getReversed(edgeId)) {
						ls = edges.getEndEdge().getToSplit();
					} else {
						ls = edges.getEndEdge().getFromSplit();
					}
				} else {
					ls = graph.getLineString(edgeId);
				}
	
				CoordinateSequence curCoords = ls.getCoordinateSequence();
				CardinalDirection heading = null;
				int firstOffset = 1;
				// if this is the very first edge, add the first coord
				if(edgeListIdx == 0 && edgeIdx == edges.size()-1) {
					firstOffset = 0;
				}
				
				if(partitionAttributes != null) {
					boolean changed = false;
					for(Attribute attr : partitionAttributes) {
						Object val = attr.get(graph, edgeId);
						if(!Objects.equals(val, partitionValues.get(attr))) {
							changed = true;
							partitionValues.put(attr, val);
						}
					}
					if(changed) {
						partitions.add(new Partition(Math.max(0, coords.size()-1), partitionAttributes, graph, edgeId));
					}
				}
				
				// if we traversed the edge forward
				if(!graph.getReversed(edgeId)) {
					// we add the coordinates in forward order
					heading = CardinalDirection.getHeading(curCoords.getCoordinate(0), curCoords.getCoordinate(1));
					// skip the first coordinate as it will be the last coordinate of the previous linestring
					for(int coordIdx = firstOffset; coordIdx < curCoords.size(); coordIdx++) {
						coords.add(curCoords.getCoordinate(coordIdx));
					}				
				} else {
					// we add the coordinates in reverse order
					heading = CardinalDirection.getHeading(curCoords.getCoordinate(curCoords.size()-1), curCoords.getCoordinate(curCoords.size()-2));
					// skip the first (last) coordinate as it will be the last coordinate of the previous linestring
					for(int coordIdx = curCoords.size() - (1 + firstOffset); coordIdx >= 0; coordIdx--) {
						coords.add(curCoords.getCoordinate(coordIdx));
					}				
				}
				if(calcDirections) {
					curName = graph.getName(edgeId);
					if(curDir == null || curDir.getStreetName() != curName) {
						int lastIntCsIdx = coords.size() - (curCoords.size());
						// determine the next direction
						if(graph.getScheduleLookup().getFerryInfo(edgeId) != null) {
							curDir = new FerryDirection(gf.createPoint(coords.get(lastIntCsIdx)), curName);
						} else if(curDir == null) {
							curDir = new StartDirection(gf.createPoint(coords.get(lastIntCsIdx)), curName, heading);
						} else {
							// calculate angle and use correct DirectionType
							StreetDirectionType type = StreetDirectionType.CONTINUE;
							double angle = Angle.angleBetweenOriented(coords.get(lastIntCsIdx-1), coords.get(lastIntCsIdx), coords.get(lastIntCsIdx+1));
							if(Math.abs(angle) > Math.PI * 5/6) {
								type = StreetDirectionType.CONTINUE;
							} else if(angle < Math.PI * - 2/3) {
								type = StreetDirectionType.TURN_SLIGHT_LEFT;
							} else if(angle < Math.PI * - 1/3) {
								type = StreetDirectionType.TURN_LEFT;
							} else if(angle < 0) {
								type = StreetDirectionType.TURN_SHARP_LEFT;
							} else if(angle < Math.PI * 1/3) {
								type = StreetDirectionType.TURN_SHARP_RIGHT;
							} else if(angle < Math.PI * 2/3) {
								type = StreetDirectionType.TURN_RIGHT;
							} else if(angle < Math.PI * 5/6) {
								type = StreetDirectionType.TURN_SLIGHT_RIGHT;
							}
							curDir = new StreetDirection(gf.createPoint(coords.get(lastIntCsIdx)), type, curName);
						} 
						directions.add(curDir);
					}
				}
				
				int waitTime = edges.waitTime(edgeIdx);
				double edgeDist;
				double edgeTime;
				if(edgeIdx == edges.size()-1) {
					// first edge, use the time/dist as is
					edgeDist = edges.dist(edgeIdx);
					edgeTime = edges.time(edgeIdx);
				} else {
					// subtract the cumulative time/dist of the previous edge from this edge
					edgeDist = edges.dist(edgeIdx) - edges.dist(edgeIdx+1);
					edgeTime = edges.time(edgeIdx) - edges.time(edgeIdx+1);
				}
				dist += edgeDist;
				time += edgeTime;
				edgeTime = edgeTime - waitTime;
				
				if(dist == Double.MAX_VALUE) {
					dist = -1;
					time = -1;
					return;
				}

				if(calcDirections) {
					curDir.addDistance(edgeDist);
					curDir.addTime(edgeTime);
					if(waitTime > 0) {
						if(graph.getScheduleLookup().getFerryInfo(edgeId) != null) {
							curDir.addNotification(new FerryWaitNotification(curName, waitTime));
						} else {
							curDir.addNotification(new EventWaitNotification(waitTime));
						}
					}
					// find and add truck notifications
					if(params.getVehicleType() == VehicleType.TRUCK) {
						LocalDateTime currentDateTime = LocalDateTime.ofInstant(params.getDeparture().plusSeconds(Math.round(time)), RouterConfig.DEFAULT_TIME_ZONE);
						List<RoadEvent> events = graph.getEventLookup().lookup(edgeId, currentDateTime); 
						events.stream().filter(e -> e instanceof RoadTruckNoticeEvent).map(e -> new TruckNotification((RoadTruckNoticeEvent)e)).forEach(curDir::addNotification);
					}
					// TODO take note of other interesting properties of the segment and add them as notifications
				}
			}
		
			if(calcDirections) {
				if(edgeListIdx != edgeLists.length-1) {
					directions.add(new StopoverDirection(gf.createPoint(coords.get(coords.size()-1)), edgeListIdx+1));
					curDir = null;
				}
			}
			
		}
		if(calcDirections) {
			// add final finish direction
			directions.add(new FinishDirection(gf.createPoint(coords.get(coords.size()-1))));
			
			// TODO make maximum standard vehicle size into config parameters
			if((params.getHeight() != null && params.getHeight() > 4.15)
					|| (params.getWidth() != null && params.getWidth() > 2.6)
					|| (params.getLength() != null && params.getLength() > 12.5)) {
				notifications.add(new OversizeNotification());
			}
		}
		
		if(calcRoute || calcDirections) {
			route = gf.createLineString(coords.toArray(new Coordinate[coords.size()]));
		}
		
	}

	public double getDist() {
		return dist;
	}

	public double getTime() {
		return time;
	}
	
	public LineString getRoute() {
		return route;
	}

	public List<Direction> getDirections() {
		return directions;
	}

	public Set<Notification> getNotifications() {
		return notifications;
	}

	public void setPartition(EnumSet<Attribute> partitionAttributes) {
		this.partitionAttributes = partitionAttributes;
	}

	public List<Partition> getPartitions() {
		return partitions;
	}

	public void calcDistance() {
		mergeEdges(null);
	}
	
	public void calcRoute(GeometryFactory gf) {
		calcRoute = true;
		mergeEdges(gf);
	}

	public void calcDirections(GeometryFactory gf) {
		calcRoute = true;
		calcDirections  = true;
		mergeEdges(gf);
	}

}
