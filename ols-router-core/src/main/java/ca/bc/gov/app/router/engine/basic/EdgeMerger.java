/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.engine.basic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.app.router.api.RoutingParameters;
import ca.bc.gov.app.router.datasources.StartDirection;
import ca.bc.gov.app.router.directions.CardinalDirection;
import ca.bc.gov.app.router.directions.Direction;
import ca.bc.gov.app.router.directions.EventWaitNotification;
import ca.bc.gov.app.router.directions.FerryDirection;
import ca.bc.gov.app.router.directions.FerryWaitNotification;
import ca.bc.gov.app.router.directions.FinishDirection;
import ca.bc.gov.app.router.directions.Notification;
import ca.bc.gov.app.router.directions.OversizeNotification;
import ca.bc.gov.app.router.directions.StopoverDirection;
import ca.bc.gov.app.router.directions.StreetDirection;
import ca.bc.gov.app.router.directions.StreetDirectionType;
import ca.bc.gov.app.router.directions.AbstractTravelDirection;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class EdgeMerger {
	private static final Logger logger = LoggerFactory.getLogger(EdgeMerger.class.getCanonicalName());
	
	private final BasicGraph graph;
	private final GeometryFactory gf;
	private final RoutingParameters params;
	private boolean calcRoute = false;
	private boolean calcDirections = false;
	private double dist = 0;
	private double time = 0;
	private LineString route;
	private List<Direction> directions;
	private List<Notification> notifications;
	
	public EdgeMerger(BasicGraph graph, GeometryFactory gf, RoutingParameters params) {
		this.graph = graph;
		this.gf = gf;
		this.params = params;
	}

	public void mergeEdges(EdgeList[] edgeLists) {
		List<Coordinate> coords = new ArrayList<Coordinate>();
		AbstractTravelDirection curDir = null;
		if(calcDirections) {
			directions = new ArrayList<Direction>();
			notifications = new ArrayList<Notification>();
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
				double edgeDist, edgeTime;
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
		if(dist == Double.MAX_VALUE) {
			dist = -1;
			time = -1;
			return;
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

	public List<Notification> getNotifications() {
		return notifications;
	}

	public void calcRoute() {
		calcRoute  = true;
	}

	public void calcDirections() {
		calcDirections  = true;
	}

	

}
