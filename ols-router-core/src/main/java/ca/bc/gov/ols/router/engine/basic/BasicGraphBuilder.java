/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.strtree.ItemBoundable;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import ca.bc.gov.ols.router.RouterConfig;
import ca.bc.gov.ols.router.api.GeometryReprojector;
import ca.bc.gov.ols.router.api.TurnRestrictionVis;
import ca.bc.gov.ols.router.data.RoadClosureEvent;
import ca.bc.gov.ols.router.data.RoadDelayEvent;
import ca.bc.gov.ols.router.data.RoadEvent;
import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.TurnCost;
import ca.bc.gov.ols.router.data.WeeklyTimeRange;
import ca.bc.gov.ols.router.data.enumTypes.NavInfoType;
import ca.bc.gov.ols.router.data.enumTypes.TravelDirection;
import ca.bc.gov.ols.router.data.vis.VisFeature;
import ca.bc.gov.ols.router.data.vis.VisLayers;
import ca.bc.gov.ols.router.data.vis.VisTurnRestriction;
import ca.bc.gov.ols.router.datasources.RowReader;
import ca.bc.gov.ols.router.engine.GraphBuilder;
import ca.bc.gov.ols.router.open511.Event;
import ca.bc.gov.ols.router.open511.EventResponse;
import ca.bc.gov.ols.router.open511.RecurringSchedule;
import ca.bc.gov.ols.router.open511.enums.EventStatus;
import ca.bc.gov.ols.router.open511.enums.EventType;
import ca.bc.gov.ols.router.open511.enums.RoadState;
import ca.bc.gov.ols.router.time.DateInterval;
import ca.bc.gov.ols.router.time.TemporalSet;
import ca.bc.gov.ols.router.time.TemporalSetIntersection;
import ca.bc.gov.ols.router.time.TemporalSetUnion;
import ca.bc.gov.ols.router.time.WeeklyDateRange;
import ca.bc.gov.ols.router.util.IntObjectArrayMap;

public class BasicGraphBuilder implements GraphBuilder {
	private final static Logger logger = LoggerFactory.getLogger(BasicGraphBuilder.class.getCanonicalName());
	
	private BasicGraph graph;
	private RouterConfig config;
	private GeometryReprojector reprojector;
	private TIntIntHashMap nodeIdByIntId;
	private IntObjectArrayMap<int[]> edgeIdBySegId;
	private TurnCostLookup turnCostLookup;
	private TIntObjectMap<ArrayList<TurnRestrictionVis>> restrictionsByEdgeId;
	private EventLookup eventLookup;
	private TrafficLookupBuilder trafficLookupBuilder;
	private VisLayers layers;
	private List<Integer> ferryEdges;
	private ScheduleLookup scheduleLookup;
	
	
	public BasicGraphBuilder(RouterConfig config, GeometryReprojector reprojector) {
		graph = new BasicGraph(RouterConfig.EXPECTED_EDGES);
		this.reprojector = reprojector;
		this.config = config;
		nodeIdByIntId = new TIntIntHashMap(RouterConfig.EXPECTED_EDGES/2, 0.5f, BasicGraph.NO_NODE, BasicGraph.NO_NODE);
		edgeIdBySegId = new IntObjectArrayMap<int[]>(RouterConfig.EXPECTED_EDGES);
		restrictionsByEdgeId = new TIntObjectHashMap<ArrayList<TurnRestrictionVis>>();		
		layers = new VisLayers();
		ferryEdges = new ArrayList<Integer>();
	}
	
	@Override
	public void addEdge(StreetSegment seg) {
		LineString ls = seg.getCenterLine();
		int fromIntId = seg.getStartIntersectionId();
		int fromNodeId = getNodeId(fromIntId, ls.getStartPoint());
		int toIntId = seg.getEndIntersectionId();
		int toNodeId = getNodeId(toIntId, ls.getEndPoint());
		boolean oneWay = seg.getTravelDirection() != TravelDirection.BIDIRECTIONAL;
		int[] edgeIds = graph.addEdge(fromNodeId, toNodeId, ls, oneWay, seg.getSpeedLimit(), seg.getName().intern(), 
				seg.getStartTrafficImpactor(), seg.getEndTrafficImpactor(),
				seg.getMaxHeight(), seg.getMaxWidth(), seg.getMaxWeight(), seg.isTruckRoute(), seg.isDeadEnded());
		edgeIdBySegId.put(seg.getSegmentId(), edgeIds);
		if(seg.isFerry()) {
			for(int edgeId : edgeIds) {
				ferryEdges.add(edgeId);
			}
		}
	}

	@Override
	public void addTurnCost(TurnCost cost) {
		if(turnCostLookup == null) {
			turnCostLookup = new TurnCostLookup(graph);
		}
		// need to convert from external data int/seg ids to internal graph node/edge ids
		int[] oldIds = cost.getIdSeq();
		int[] newIds = new int[oldIds.length];
		int[] edgeIds = null;
		for(int i = 0; i < oldIds.length; i++) {
			if(i % 2 == 0) {
				// even indexes are edge ids
				// could be up to 2 possible edges (bidirectional segment) 
				// we save them for now and then compare them against the nodeId to find the right one  
				edgeIds = edgeIdBySegId.get(oldIds[i]);
				if(edgeIds == null) {
					logger.warn("Invalid segmentId in turn costs: " + oldIds[i] + "(turn cost/restriction ignored)");
					return;
				}
			} else {
				// odd indexes are node Ids
				Integer nodeId = nodeIdByIntId.get(oldIds[i]);
				if(nodeId == nodeIdByIntId.getNoEntryKey()) {
					logger.warn("Invalid intersectionId in turn costs: " + oldIds[i] + "(turn cost/restriction ignored)");
					return;
				}
				newIds[i] = nodeId;
				// determine which segmentId to use for the previous segment
				if(graph.getToNodeId(edgeIds[0]) == nodeId) {
					newIds[i-1] = edgeIds[0]; 
				} else if(edgeIds.length > 1 && graph.getToNodeId(edgeIds[1]) == nodeId) {
					newIds[i-1] = edgeIds[1];
				} else {
					logger.warn("Invalid segment/intersectionId sequence in turn costs: " + oldIds[i-1] + "|" + oldIds[i] + " (turn cost/restriction ignored)");
					return;
				}
			}
		}
		if(edgeIds == null) {
			// shouldn't happen unless there was bad input
			logger.warn("Invalid Id sequence in turn costs: " + oldIds + " (turn cost/restriction ignored)");
			return;			
		}
		// determine which segmentId to use for the last segment
		int lastNodeId = newIds[newIds.length-2]; 
		if(graph.getFromNodeId(edgeIds[0]) == lastNodeId) {
			newIds[newIds.length-1] = edgeIds[0]; 
		} else if(edgeIds.length > 1 && graph.getFromNodeId(edgeIds[1]) == lastNodeId) {
			newIds[newIds.length-1] = edgeIds[1];
		} else {
			logger.warn("Invalid intersectionId/segment sequence in turn costs: " + oldIds[oldIds.length-2] + "|" + oldIds[oldIds.length-1] + " (turn cost/restriction ignored)");
			return;
		}
		
		boolean result = turnCostLookup.addCost(newIds, cost.getCost(), cost.getRestriction());
		if(!result) {
			logger.warn("Failed to add turn restriction for original Ids: " + Arrays.toString(oldIds));
			return;
		}
		
		if(cost.getRestriction() != null) {			
			ArrayList<TurnRestrictionVis> restrictionList = restrictionsByEdgeId.get(newIds[0]);
			if(restrictionList == null) {
				restrictionList = new ArrayList<TurnRestrictionVis>(3);
				restrictionsByEdgeId.put(newIds[0], restrictionList);
			}
			TurnRestrictionVis tr = new TurnRestrictionVis(graph, newIds, cost.getRestriction()); 
			restrictionList.add(tr);
		}

	}	
	
	@Override
	public void addEvents(EventResponse eventResponse) {
		if(eventLookup == null) {
			eventLookup = new EventLookup(graph);
			graph.build();
		}
		for(Event evt : eventResponse.getEvents()) {
			// skip over events that aren't easy to deal with, for now
			if(!EventStatus.ACTIVE.equals(evt.getStatus()) 
					|| (!EventType.CONSTRUCTION.equals(evt.getEventType())
							&& !EventType.INCIDENT.equals(evt.getEventType()))
					|| evt.getGeography() == null
					|| evt.getGeography().getGeometryType() != "Point") {
				continue;
			}
			if(evt.getRoads().get(0).getDelay() != null
					|| RoadState.CLOSED.equals(evt.getRoads().get(0).getState())) {
				Geometry geom = reprojector.reproject(evt.getGeography(), config.getBaseSrsCode());
				int edgeId = graph.findClosestEdge((Point)geom);
				if(edgeId != BasicGraph.NO_EDGE) {
					// determine the schedule
					TemporalSet schedule = null;
					if(evt.getSchedule().getIntervals() != null) {
						schedule = new TemporalSetUnion(evt.getSchedule().getIntervals());
					} else if(evt.getSchedule().getRecurringSchedules() != null) {
						List<RecurringSchedule> scheds = evt.getSchedule().getRecurringSchedules();
						List<TemporalSet> schedList = new ArrayList<TemporalSet>(scheds.size());
						for(RecurringSchedule sched : scheds) {
							schedList.add(new TemporalSetIntersection(
									new WeeklyTimeRange(sched.getDays(), Arrays.asList(sched.getDailyStartTime(), sched.getDailyEndTime())),
									new DateInterval(sched.getStartDate(), sched.getEndDate())
							));
						}
						schedule = new TemporalSetUnion(schedList);
					} else {
						logger.warn("Invalid event schedule has neither recurring nor intervals: " + evt.getUrl());
						continue;
					}
					// determine the event type
					RoadEvent roadEvent = null;
					if(RoadState.CLOSED.equals(evt.getRoads().get(0).getState())) {
						roadEvent = new RoadClosureEvent(schedule);
					} else if(evt.getRoads().get(0).getDelay() != 0) {
						roadEvent = new RoadDelayEvent(schedule, evt.getRoads().get(0).getDelay()*60);
					}
					List<Integer> edgeIds;
					int otherEdgeId = graph.getOtherEdgeId(edgeId);
					if(otherEdgeId == BasicGraph.NO_EDGE) {
						edgeIds = Arrays.asList(edgeId); 
					} else { 
						edgeIds = Arrays.asList(edgeId, otherEdgeId);
					}
					eventLookup.addEvent(edgeIds, roadEvent);
					layers.addFeature(new VisFeature(geom, NavInfoType.EV, evt.getEventType().toString() + ": " + evt.getDescription()));
				}
			}
		}
	}

	@Override
	public void addTraffic(RowReader trafficReader) {
		trafficLookupBuilder = new TrafficLookupBuilder(graph);
		while(trafficReader.next()) {
			int tlid = trafficReader.getInt("tlid");
			String days = trafficReader.getString("dotw");
			LocalTime time = LocalTime.parse(trafficReader.getString("time"));
			Integer forwardSpeed = trafficReader.getInteger("f_speed");
			Integer reverseSpeed = trafficReader.getInteger("r_speed");
			
			int[] edgeIds = edgeIdBySegId.get(tlid);
			if(edgeIds == null) {
				logger.warn("Traffic Info provided for a non existent segment, id: " + tlid);
			} else {
				for(int edgeId : edgeIds) {
					if(graph.getReversed(edgeId)) {
						trafficLookupBuilder.addTraffic(edgeId, days, time, reverseSpeed);
					} else {
						trafficLookupBuilder.addTraffic(edgeId, days, time, forwardSpeed);
					}
				}
			}
		}
	}
	
	@Override
	public void addSchedules(GtfsDaoImpl gtfs, RowReader mappingReader) throws IOException {
		scheduleLookup = new ScheduleLookup(graph);
		
		// read the ITN_to_BCF file
		// build a map from ferry route name to ferryInfo
		Map<String,FerryInfo> ferryInfoByName = new HashMap<String,FerryInfo>();
		while(mappingReader.next()) {
			// use the seg ids and route name to identify the route and create it
			String routeId = mappingReader.getString("route_id");
			String name = mappingReader.getString("itn_name");
			int minWaitTime = mappingReader.getInt("min_wait_time");
			int travelTime = mappingReader.getInt("crossing_time");
			boolean isScheduled = "y".equalsIgnoreCase(mappingReader.getString("is_scheduled"));
			FerryInfo info = new FerryInfo(minWaitTime*60, travelTime*60, isScheduled);
			ferryInfoByName.put(name, info);
		}
		
		// read the stops into a spatial index
		STRtree spatialIndex = new STRtree();
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
		for(Stop stop : gtfs.getAllStops()) {
			Point p = gf.createPoint(new Coordinate(stop.getLon(), stop.getLat()));
			p = reprojector.reproject(p, config.getBaseSrsCode());
			spatialIndex.insert(p.getEnvelopeInternal(), stop);
		}
		spatialIndex.build();

		// find the stops associated with each ferry edge and build a lookup table
		Map<String,Integer> edgeIdByStopIds = new HashMap<String,Integer>();
		ItemDistance dist = new ItemDistance() {
			@Override
			public double distance(ItemBoundable item1, ItemBoundable item2) {
				return ((Envelope)item1.getBounds()).distance((Envelope)item2.getBounds());
			}
		};
		
		Map<Point,List<Integer>> ferryEdgesByStartPoint = new HashMap<Point,List<Integer>>();
		Map<Point,String> terminalNamesByPoint = new HashMap<Point,String>();
		for(int edgeId : ferryEdges) {
			LineString ls = graph.getLineString(edgeId);
			Point startPoint, endPoint;
			if(graph.getReversed(edgeId)) {
				startPoint = ls.getEndPoint();
				endPoint = ls.getStartPoint();
			} else {
				startPoint = ls.getStartPoint();
				endPoint = ls.getEndPoint();
			}
			Stop firstStop = (Stop)spatialIndex.nearestNeighbour(startPoint.getEnvelopeInternal(), startPoint, dist);
			Stop lastStop = (Stop)spatialIndex.nearestNeighbour(endPoint.getEnvelopeInternal(), endPoint, dist);
			String name = graph.getName(edgeId);
			if(checkFerryStopDistance(gf, startPoint, firstStop) && checkFerryStopDistance(gf, endPoint, lastStop)) {
				edgeIdByStopIds.put(firstStop.getId().getId() + "_" + lastStop.getId().getId(), edgeId);
				terminalNamesByPoint.put(startPoint, firstStop.getName());
			} else {
				logger.warn("No stop associated with ferry segment named: " + name);
			}
			FerryInfo info = ferryInfoByName.get(name);
			if(info != null) {
				scheduleLookup.addFerryInfo(edgeId, info);
				graph.setSpeedLimit(edgeId, (short)(ls.getLength()*3.6/info.getTravelTime()));
			} else {
				logger.warn("No ferry information assocated with ferry segment named: " + name);
			}
			LineString ferryLine = graph.getLineString(edgeId);
			if(graph.getReversed(edgeId)) {
				ferryLine = (LineString) ferryLine.reverse();
			} 
			layers.addFeature(new VisFeature(ferryLine, NavInfoType.SC, name));
			Point p = ferryLine.getStartPoint();
			List<Integer> edgeIdList = ferryEdgesByStartPoint.get(p);
			if(edgeIdList == null) {
				edgeIdList = new ArrayList<Integer>();
				ferryEdgesByStartPoint.put(p, edgeIdList);
			}
			edgeIdList.add(edgeId);
		}

		for(Entry<Point, List<Integer>> entry: ferryEdgesByStartPoint.entrySet()) {
			String terminalName = terminalNamesByPoint.get(entry.getKey());
			if(terminalName == null) {
				terminalName = "Ferry Terminal";
			} else {
				terminalName += " Ferry Terminal";
			}
			layers.addFeature(new VisFeature(entry.getKey(), NavInfoType.SC, terminalName, StringUtils.join(entry.getValue(), ","), 0));
		}
		
		// read calendar
		TIntObjectHashMap<WeeklyDateRange> calendarByServiceId = new TIntObjectHashMap<WeeklyDateRange>(); // map from service Id to calendar
		for(ServiceCalendar cal : gtfs.getAllCalendars()) {
			DateInterval dateRange = new DateInterval(cal.getStartDate().getAsDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), 
					cal.getEndDate().getAsDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			EnumSet<DayOfWeek> daySet = EnumSet.noneOf(DayOfWeek.class);
			if(cal.getSunday() == 1) daySet.add(DayOfWeek.SUNDAY);
			if(cal.getMonday() == 1) daySet.add(DayOfWeek.MONDAY);
			if(cal.getTuesday() == 1) daySet.add(DayOfWeek.TUESDAY);
			if(cal.getWednesday() == 1) daySet.add(DayOfWeek.WEDNESDAY);
			if(cal.getThursday() == 1) daySet.add(DayOfWeek.THURSDAY);
			if(cal.getFriday() == 1) daySet.add(DayOfWeek.FRIDAY);
			if(cal.getSaturday() == 1) daySet.add(DayOfWeek.SATURDAY);
			calendarByServiceId.put(cal.getId(), new WeeklyDateRange(daySet, dateRange));
		}
		
		// read the stop times
		Map<String,List<StopTime>> stopTimesByTripId = new HashMap<String,List<StopTime>>(); // map from trip Id to list of stop times
		for(StopTime stopTime : gtfs.getAllStopTimes()) {
			String tripId = stopTime.getTrip().getId().getId();
			List<StopTime> stopTimes = stopTimesByTripId.get(tripId);
			if(stopTimes == null) {
				stopTimes = new ArrayList<StopTime>();
				stopTimesByTripId.put(tripId, stopTimes);
			}
			stopTimes.add(stopTime);
		}
		
		// read the trips
		for(Trip trip : gtfs.getAllTrips()) {
			// for each trip, get the list of stop times
			// loop over list in order starting at second one
			List<StopTime> stopTimes = stopTimesByTripId.get(trip.getId().getId());
			Collections.sort(stopTimes, Comparator.comparing(StopTime::getStopSequence));
			for(int i = 1; i < stopTimes.size(); i++) {
				// take the departure time from previous stop time and arrival time from this stop time
				StopTime begin = stopTimes.get(i-1);
				StopTime end = stopTimes.get(i);
				
				// get the stops and associate the trip-leg with the ferry edge(s)
				Integer edgeId = edgeIdByStopIds.get(begin.getStop().getId().getId() + "_" + end.getStop().getId().getId());
				if(edgeId == null) {
					logger.info("No edge found for trip: " + trip.getRoute().getShortName());
					continue;
				} 
				scheduleLookup.addSchedule(edgeId, calendarByServiceId.get(Integer.parseInt(trip.getServiceId().getId())), 
						LocalTime.ofSecondOfDay(begin.getDepartureTime()));
			}
		}
		scheduleLookup.sort();
	}

	private boolean checkFerryStopDistance(GeometryFactory gf, Point startPoint, Stop firstStop) {
		Point stopPoint = gf.createPoint(new Coordinate(firstStop.getLon(), firstStop.getLat()));
		double distance = startPoint.distance(reprojector.reproject(stopPoint, config.getBaseSrsCode()));
		if(distance > 100) {
			return false;
		}
		return true;
	}
	
	private int addNode(int intId, Point point) {
		int nodeId = graph.addNode(point);
		nodeIdByIntId.put(intId, nodeId);
		return nodeId;
	}

	private int getNodeId(int intId, Point p) {
		int nodeId = nodeIdByIntId.get(intId);  
		if(nodeId == BasicGraph.NO_NODE) {
			return addNode(intId, p);
		}
		return nodeId;
	}
	
	public BasicGraph build() {
		graph.build();
		graph.setTurnCostLookup(turnCostLookup);
		graph.setEventLookup(eventLookup);
		graph.setTrafficLookup(trafficLookupBuilder.build());
		buildTurnRestrictionLayer();
		layers.buildIndexes();
		graph.setVisLayers(layers);
		graph.setScheduleLookup(scheduleLookup);
		return graph;
	}

	private void buildTurnRestrictionLayer() {
		restrictionsByEdgeId.forEachEntry(new TIntObjectProcedure<List<TurnRestrictionVis>>() {
			@Override
			public boolean execute(int edgeId, List<TurnRestrictionVis> restrictions) {
				String detail = restrictionListToString(restrictions);
				if(detail != null) {
					LineString ls = graph.getLineString(edgeId);
					LengthIndexedLine lil = new LengthIndexedLine(ls);
					double offset = 10;
					double maxOffset = lil.getEndIndex() / 3;
					if(offset > maxOffset) {
						offset = maxOffset; 
					}
					Coordinate c = lil.extractPoint((graph.getReversed(edgeId)?1:-1) * offset/2);
					layers.addFeature(new VisTurnRestriction(ls.getFactory().createPoint(c), NavInfoType.TR, null, detail, restrictions.get(0).getAngle(), 
							restrictions.get(0).getFromFragment(), makeToFragmentList(restrictions)));
				}
				return true;
			}
		});
	}
	
	private String restrictionListToString(List<TurnRestrictionVis> restrictions) {
		if(restrictions == null || restrictions.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(TurnRestrictionVis tr : restrictions) {
			sb.append(tr.toString() + "\n");
		}
		return sb.toString();
	}

	private List<LineString> makeToFragmentList(List<TurnRestrictionVis> restrictions) {
		ArrayList<LineString> toFrags = new ArrayList<LineString>(restrictions.size());
		for(TurnRestrictionVis tr : restrictions) {
			toFrags.add(tr.getToFragment());
		}
		return toFrags;
	}

}
