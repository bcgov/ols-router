/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.process;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.RouterFactory;
import ca.bc.gov.ols.router.data.TurnCost;
import ca.bc.gov.ols.router.data.WeeklyTimeRange;
import ca.bc.gov.ols.router.data.enums.DayCode;
import ca.bc.gov.ols.router.data.enums.DividerType;
import ca.bc.gov.ols.router.data.enums.RoadClass;
import ca.bc.gov.ols.router.data.enums.SurfaceType;
import ca.bc.gov.ols.router.data.enums.TrafficImpactor;
import ca.bc.gov.ols.router.data.enums.TravelDirection;
import ca.bc.gov.ols.router.data.enums.TurnRestrictionType;
import ca.bc.gov.ols.router.data.enums.TurnTimeCode;
import ca.bc.gov.ols.router.datasources.CsvRowReader;
import ca.bc.gov.ols.router.datasources.JsonRowReader;
import ca.bc.gov.ols.router.datasources.JsonRowWriter;
import ca.bc.gov.ols.router.datasources.RowReader;
import ca.bc.gov.ols.router.datasources.RowWriter;
import ca.bc.gov.ols.router.datasources.XsvRowWriter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class RouterProcess {
	private final static Logger logger = LoggerFactory.getLogger(RouterProcess.class.getCanonicalName());
	private static final byte LEFT_COST = 4; 
	private static final byte RIGHT_COST = 2; 
	
	// BASE_ONLY = true to create turn_costs_base.csv for turn managment UI, ignoring custom file 
	private static final boolean BASE_ONLY = true;
	
	static int droppedTRs = 0;
	static int droppedSegs = 0;
	static int trCount = 0;
	static int ferryCount = 0;
	static int turningLaneRestrictionCount = 0;
	static int turningLaneNonRestrictionCount = 0;
	static int isolatedTurningLaneCount = 0;
	static int multiwayTurningLaneRestrictionCount = 0;
	static int multiwayTurningLaneNonRestrictionCount = 0;
	static int dividedEndRestrictionCount = 0;
	static int uTurnRestrictionCount = 0;
	static int uTurnNonRestrictionCount = 0;
	static GeometryFactory geometryFactory;
	//private XsvRowWriter trWriter;
	
	private static String dataDir = "C:/apps/router/data/";
	private RowWriter logWriter;
	private EnumMap<TrafficImpactor,byte[][]> turnCostMatrix = buildTurnCostMatrix();
	private EnumMap<RoadClass,Double> trafficMultiplierMap = buildTrafficMultiplierMap();
	
	public static void main(String[] args) throws IOException {
		if(args.length != 1) {
			logger.error("Data directory parameter is required.");
			System.exit(-1);
		}
		String dir = args[0];
		File f = new File(dir);
		if(!f.isDirectory()) {
			logger.error("Invalid data dir: '{}'", dir);
			System.exit(-1);
		}
		dataDir = dir;
		logger.info("Reading config properties");
		Properties props = RouterFactory.loadProps();
		
		// RouterProcess must always be run against the deliv data!!
		// (except in devel)
		//props.setProperty("environment", "deliv");
		RouterProcess rp = new RouterProcess(props);
		rp.process();
	}

	public RouterProcess(Properties props) {
		geometryFactory = RouterFactory.geometryFactoryFromProps(props);
	}

	public void process() {
		openLogWriter();
		
//		File trFile = new File(dataDir + "turn_restrictions_uturn_new.csv");
//		List<String> trSchema = Arrays.asList("EDGE_NODE_SET");
//		trWriter = new XsvRowWriter(trFile, ',', trSchema, false);

		TIntObjectHashMap<String> streetNameById = loadStreetNames();
		TIntIntHashMap streetNameIdBySegmentId = loadStreetNameOnSegs();

		// load street intersections into a lookup table
		logger.info("Loading Street Intersections");
		TIntObjectHashMap<RpStreetIntersection> intersectionIdMap = new TIntObjectHashMap<RpStreetIntersection>();
		
		// load street segments into a lookup table
		logger.info("Loading Street Segments");
		List<RpStreetSegment> segments = new ArrayList<RpStreetSegment>(100000);
		TIntObjectHashMap<RpStreetSegment> ferrySegments = new TIntObjectHashMap<RpStreetSegment>();
		RowReader rr = new JsonRowReader(dataDir + "street_load_street_segments.json", geometryFactory);
		int segCount = 0;
		while(rr.next()) {
			segCount++;
			int segmentId = rr.getInt("street_segment_id");
			int startIntersectionId = rr.getInt("start_intersection_id");
			int endIntersectionId = rr.getInt("end_intersection_id");
			RoadClass roadClass = RoadClass.convert(
					rr.getString("road_class"));
			boolean isVirtual = "Y".equals(rr.getString("virtual_ind")); 
			TravelDirection travelDir = TravelDirection.convert(
					rr.getString("travel_direction"));
			DividerType dividerType = DividerType.convert(rr.getString("divider_type"));
			TrafficImpactor startTrafficImpactor = TrafficImpactor.convert(rr.getString("start_traffic_impactor"));
			TrafficImpactor endTrafficImpactor = TrafficImpactor.convert(rr.getString("end_traffic_impactor"));
			short speedLimit = (short)rr.getInt("speed_limit");
			SurfaceType surfaceType = SurfaceType.convert(rr.getString("surface_type"));
			LineString centerLine = rr.getLineString();
			
			double fromMaxHeight = rr.getDouble("from_vehicle_max_height");
			double toMaxHeight = rr.getDouble("to_vehicle_max_height");
			double maxHeight = Double.NaN;
			if(!Double.isNaN(fromMaxHeight) && !Double.isNaN(toMaxHeight)) {
				maxHeight = Math.min(fromMaxHeight, toMaxHeight);
				} else if(!Double.isNaN(fromMaxHeight)) {
					maxHeight = fromMaxHeight;
				} else if(!Double.isNaN(toMaxHeight)) {
					maxHeight = toMaxHeight;
				}

			double fromMaxWidth = rr.getDouble("from_vehicle_max_width");
			double toMaxWidth = rr.getDouble("to_vehicle_max_width");
			double maxWidth = Double.NaN;
			if(!Double.isNaN(fromMaxWidth) && !Double.isNaN(toMaxWidth)) {
				maxWidth = Math.min(fromMaxWidth, toMaxWidth);
				} else if(!Double.isNaN(fromMaxWidth)) {
					maxWidth = fromMaxWidth;
				} else if(!Double.isNaN(toMaxWidth)) {
					maxWidth = toMaxWidth;
				}

			Integer fromMaxWeight = rr.getInteger("from_vehicle_max_weight");
			Integer toMaxWeight = rr.getInteger("to_vehicle_max_weight");
			Integer maxWeight = null;
			if(fromMaxWeight != null && toMaxWeight != null) {
				maxWeight = Math.min(fromMaxWeight, toMaxWeight);
				} else if(fromMaxWeight != null) {
					maxWeight = fromMaxWeight;
				} else if(toMaxWeight != null) {
					maxWeight = toMaxWeight;
				}

			boolean isTruckRoute = "Y".equals(rr.getString("truck_route_ind"));
			
			String highwayRoute1 = rr.getString("highway_route_1");
			if(highwayRoute1 != null) highwayRoute1 = highwayRoute1.intern();
			String highwayRoute2 = rr.getString("highway_route_2");
			if(highwayRoute2 != null) highwayRoute2 = highwayRoute2.intern();
			String highwayRoute3 = rr.getString("highway_route_3");
			if(highwayRoute3 != null) highwayRoute3 = highwayRoute3.intern();

			TurnTimeCode fromLeftTR = null;
			TurnTimeCode fromCentreTR = null;
			TurnTimeCode fromRightTR = null;
			TurnTimeCode toLeftTR = null;
			TurnTimeCode toCentreTR = null;
			TurnTimeCode toRightTR = null;
			if(startTrafficImpactor != TrafficImpactor.OVERPASS && startTrafficImpactor != TrafficImpactor.UNDERPASS) {
				fromLeftTR = TurnTimeCode.convert(rr.getString("from_left_turn_restriction"));
				fromCentreTR = TurnTimeCode.convert(rr.getString("from_centre_turn_restriction"));
				fromRightTR = TurnTimeCode.convert(rr.getString("from_right_turn_restriction"));
			}
			if(endTrafficImpactor != TrafficImpactor.OVERPASS && endTrafficImpactor != TrafficImpactor.UNDERPASS) {
				toLeftTR = TurnTimeCode.convert(rr.getString("to_left_turn_restriction"));
				toCentreTR = TurnTimeCode.convert(rr.getString("to_centre_turn_restriction"));
				toRightTR = TurnTimeCode.convert(rr.getString("to_right_turn_restriction"));
			}
			
			// skip virtual and unRouteable segments
			if((roadClass != RoadClass.FERRY && isVirtual) || !roadClass.isRouteable()) {
				droppedSegs++;
				continue;
			}
			
			String name = streetNameById.get(streetNameIdBySegmentId.get(segmentId));
			
			RpStreetSegment segment = new RpStreetSegment(segmentId, centerLine,
					startIntersectionId, endIntersectionId, name,
					roadClass, travelDir,
					dividerType, startTrafficImpactor, endTrafficImpactor, 
					speedLimit,  surfaceType,
					maxHeight, maxWidth, maxWeight, isTruckRoute,
					highwayRoute1, highwayRoute2, highwayRoute3,
					false, isVirtual,
					fromLeftTR, fromCentreTR, fromRightTR, toLeftTR, toCentreTR, toRightTR);
			if(RoadClass.FERRY.equals(roadClass)) {
				ferrySegments.put(segment.getSegmentId(), segment);
			} else {
				segments.add(segment);
			}
			
			RpStreetEnd startEnd = segment.getStartEnd();
			RpStreetIntersection startInt = intersectionIdMap.get(startIntersectionId);
			if(startInt == null) {
				startInt = new RpStreetIntersection(startIntersectionId, centerLine.getStartPoint());
				intersectionIdMap.put(startIntersectionId, startInt);
			}
			startInt.addEnd(startEnd);
			RpStreetEnd endEnd = segment.getEndEnd();
			RpStreetIntersection endInt = intersectionIdMap.get(endIntersectionId);
			if(endInt == null) {
				endInt = new RpStreetIntersection(endIntersectionId, centerLine.getEndPoint());
				intersectionIdMap.put(endIntersectionId, endInt);
			}
			endInt.addEnd(endEnd);

		}
		
		logger.info("Street Segments read: {}", segCount);
		if(segCount == 0) {
			logger.error("No street segments found - cannot continue!");
			return;
		}
		
		outputTraffic(segments);
		
		// loop over each intersection
		TIntObjectHashMap<List<TurnCost>> turnCosts = new TIntObjectHashMap<List<TurnCost>>();
		intersectionIdMap.forEachEntry(new TIntObjectProcedure<RpStreetIntersection>() {
			@Override
			public boolean execute(final int id, final RpStreetIntersection intersection) {
				List<RpStreetEnd> ends = intersection.getEnds();
				List<RpStreetEnd> ferryEnds = new ArrayList<RpStreetEnd>();
				List<RpStreetEnd> nonFerryEnds = new ArrayList<RpStreetEnd>();
				List<RpStreetEnd> turningLanes = new ArrayList<RpStreetEnd>();
				List<RpStreetEnd> dividedSegs = new ArrayList<RpStreetEnd>();
				// loop over all the edges adjacent to this intersection
		        for(int inIdx = 0; inIdx < ends.size(); inIdx++) {
		        	RpStreetEnd in = ends.get(inIdx);
		        	// make lists segments with particular characteristics to handle later
					if(RoadClass.FERRY.equals(in.getSegment().getRoadClass())) {
						ferryEnds.add(in);
					} else {
						nonFerryEnds.add(in);
					}
					if(in.getTravelDir().isOneWay() && nameIsTurningLaneOrRamp(in.getSegment().getName())) {
						turningLanes.add(in);
					}
					if(in.getSegment().getDividerType().isDivided()) {
						dividedSegs.add(in);
					}
					// ignore outgoing-only segments as they cannot have turn restrictions
					if(in.getTravelDir().equals(TravelDirection.FORWARD)) {
						// this is an outgoing-only stub
						continue;
					}
					// determine which outgoing stubs are lefts and rights and which is the centre
		        	ArrayList<RpStreetEnd> rights = new ArrayList<RpStreetEnd>(ends.size());
		        	ArrayList<RpStreetEnd> lefts = new ArrayList<RpStreetEnd>(ends.size());
		        	RpStreetEnd centre = null;
		        	int centerAngle = 180;
		        	for(int outIdx = (inIdx + 1) % ends.size(); outIdx != inIdx; outIdx = (outIdx + 1) % ends.size()) {
		        		RpStreetEnd out = ends.get(outIdx);
		        		int relativeAngle = (out.getAngle() + 360 - in.getAngle()) % 360;
		        		if(relativeAngle < 135) {
		        			rights.add(out);
		        		} else if(relativeAngle <= 225) {
		        			// this could be a new straight
		        			int newStraightAngle = Math.abs(relativeAngle - 180);
		        			if(newStraightAngle < centerAngle) {
			        			if(centre != null) {
			        				// the old center is now a right
			        				rights.add(centre);
			        			}
			        			centre = out;
			        			centerAngle = newStraightAngle;
		        			} else {
		        				// it must be a left
		        				lefts.add(0, out);
		        			}
		        		} else {
		        			lefts.add(0, out);
		        		}
		        	}
		        	
		        	// handle left TRs
		        	if(lefts.isEmpty()) {
		        		if(in.getLeftTR() != null) {
		        			logRestriction(in, true, "LeftTR with no left segment");
		        		}
		        	} else {
		        		if(lefts.size() > 1) {
			        		logRestriction(in, false, "LeftTR with multiple left segments");		        			
			        		for(int i=1; i < lefts.size(); i++) {
			        			TurnCost tr = buildTurnCost(in, 
			        					intersection, lefts.get(i), (byte)(getTurnCost(in, lefts.get(i)) + LEFT_COST), null);
			        			addTurnCost(turnCosts, tr);
			        		}
		        		}
	    				TurnCost tr = buildTurnCost(in, 
	    						intersection, lefts.get(0), (byte)(getTurnCost(in, lefts.get(0)) + LEFT_COST), in.getLeftTRTimeRange());
	    				addTurnCost(turnCosts, tr);
		        	}
		        	
		        	// handle centre TRs
					if(centre == null) {
			        	if(in.getCentreTR() != null) {
							logRestriction(in, true, "CentreTR with no center segment");
			        	}
					} else {
						RpStreetEnd otherEnd = centre;
						if(lefts.size() > 0) {
							otherEnd = lefts.get(0);
						} else if(rights.size() > 0) {
							otherEnd = rights.get(0);
						}
        				TurnCost tr = buildTurnCost(in, intersection, centre, getTurnCost(in, otherEnd), in.getCentreTRTimeRange());
        				addTurnCost(turnCosts, tr);
					}	        		
		        	
		        	// handle right TRs
	        		if(rights.isEmpty()) {
			        	if(in.getRightTR() != null) {
		        			logRestriction(in, true, "RightTR with no left segment");
			        	}
		        	} else {
		        		if(rights.size() > 1) {
			        		logRestriction(in, false, "RightTR with multiple left segments");
			        		for(int i=1; i< rights.size(); i++) {
			        			TurnCost tr = buildTurnCost(in, 
			        					intersection, rights.get(i), (byte)(getTurnCost(in, rights.get(i)) + RIGHT_COST), null);
			        			addTurnCost(turnCosts, tr);
			        		}
		        		}
	    				TurnCost tr = buildTurnCost(in, 
	    						intersection, rights.get(0), (byte)(getTurnCost(in, rights.get(0))+ RIGHT_COST), in.getRightTRTimeRange());
	    				addTurnCost(turnCosts, tr);
		        	}
		        } // end loop over segments incident to the intersection
		        
		        // build dead-ended trees by working up from dead-ends
		        if(ends.size() == 1) {
		        	RpStreetSegment newDeadEnd = ends.get(0).getSegment();
		        	int nextIntId = intersection.getId();
		        	while(newDeadEnd != null) {
			        	newDeadEnd.setIsDeadEnded();
			        	if(newDeadEnd.getStartIntersectionId() == nextIntId) {
			        		nextIntId = newDeadEnd.getEndIntersectionId(); 
			        	} else {
			        		nextIntId = newDeadEnd.getStartIntersectionId();
			        	}
			        	RpStreetIntersection nextIntersection = intersectionIdMap.get(nextIntId);
			        	newDeadEnd = null;
			        	for(RpStreetEnd end: nextIntersection.getEnds()) {
			        		if(!end.getSegment().isDeadEnded()) {
			        			if(newDeadEnd != null) {
				        			// there is more than one new dead-end here, we're done
			        				newDeadEnd = null; 
			        				break;
			        			}
			        			newDeadEnd = end.getSegment();
			        		}
			        	}
		        	}
		        }
		        
		        // deal with ferry terminals
		        if(ferryEnds.size() > 0 && nonFerryEnds.size() > 0) {
		        	for(RpStreetEnd ferryEnd : ferryEnds) {
		        		FerryRoute fr = buildFerryRoute(ferryEnd, null);
		        		if(fr == null) continue; // already handled this ferry route
		        		for(RpStreetSegment seg : fr.segs) {
		        			ferrySegments.remove(seg.getSegmentId());
		        		}
		        		LineString newLine = fr.buildLine();
		        		RpStreetSegment seg = fr.segs.get(0);
		        		segments.add(new RpStreetSegment(seg.getSegmentId(), newLine, fr.startEnd.getIntersectionId(),
		        				fr.endEnd.getIntersectionId(), seg.getName(), seg.getRoadClass(),
		        				seg.getTravelDirection(), seg.getDividerType(),
		        				fr.startEnd.getTrafficImpactor(), fr.endEnd.getTrafficImpactor(),
		        				seg.getSpeedLimit(), seg.getSurfaceType(), 
		        				seg.getMaxHeight(), seg.getMaxWidth(), seg.getMaxWeight(), seg.isTruckRoute(),
		        				seg.getHighwayRoute1(), seg.getHighwayRoute2(), seg.getHighwayRoute3(),
		        				seg.isDeadEnded(), seg.isVirtual(), 
		        				null, null, null, null, null, null));
		        		ferryCount++;
		        	}
		        }

		        // identify turn restrictions between turning lanes
		        if(turningLanes.size() > 1) {
		        	// separate into overpass/underpass/grade level sets
		        	List<List<RpStreetEnd>> turningLaneGroups = new ArrayList<List<RpStreetEnd>>(3);
		        	turningLaneGroups.add(new ArrayList<RpStreetEnd>(turningLanes.size()));
		        	turningLaneGroups.add(new ArrayList<RpStreetEnd>(turningLanes.size()));
		        	turningLaneGroups.add(new ArrayList<RpStreetEnd>(turningLanes.size()));
			        for(RpStreetEnd end : turningLanes) {
			        	switch(end.getTrafficImpactor()) {
			        	case OVERPASS: 
			        		turningLaneGroups.get(0).add(end);
			        		break;
			        	case UNDERPASS: 
			        		turningLaneGroups.get(1).add(end);
			        		break;
			        	default: 
			        		turningLaneGroups.get(2).add(end);
			        	}
			        }
			        for(List<RpStreetEnd> turningLaneGroup : turningLaneGroups) {
				        if(turningLaneGroup.size() == 1) {
				        	isolatedTurningLaneCount++;
				        } else if(turningLaneGroup.size() == 2 || turningLaneGroup.size() == 3) {
					        // loop over all segments (to find incoming segments)
					        for(RpStreetEnd in : turningLaneGroup) {
					        	// skip outgoing segments
					        	if(in.getTravelDir().equals(TravelDirection.FORWARD)) continue;
					        	// loop over all segments again (to find outgoing segments)
					        	for(RpStreetEnd out : turningLaneGroup) {
					        		// skip incoming segments 
					        		if(out.getTravelDir().equals(TravelDirection.REVERSE)) continue;
					        		int relativeAngle = (out.getAngle() + 360 - in.getAngle()) % 360;
					        		// tighter than 90 degrees left or right
					        		if(relativeAngle < 90 || relativeAngle > 270) {
					        			turningLaneRestrictionCount++;
					        			TurnCost tr = new TurnCost(in.getSegment().getSegmentId(), intersection.getId(), out.getSegment().getSegmentId(), 
					        					(byte)0, WeeklyTimeRange.ALWAYS, TurnRestrictionType.Y, "GENERATED: Logically implied ramp-to-ramp restriction", null);
				        				addTurnCost(turnCosts, tr);
					        			//outputTRRow(tr);
					        		} else {
					        			turningLaneNonRestrictionCount++;
					        		}
					        	}
					        }
				        } else if(turningLaneGroup.size() >= 4) {
				        	for(RpStreetEnd in : turningLaneGroup) {
					        	// skip outgoing segments
					        	if(in.getTravelDir().equals(TravelDirection.FORWARD)) continue;
					        	// loop over all segments again (to find outgoing segments)
					        	for(RpStreetEnd out : turningLaneGroup) {
					        		// skip incoming segments 
					        		if(out.getTravelDir().equals(TravelDirection.REVERSE)) continue;
					        		int relativeAngle = (out.getAngle() + 360 - in.getAngle()) % 360;
					        		// tighter than 45 degrees left or right (from straight)
					        		if(relativeAngle < 135 || relativeAngle > 225) {
					        			multiwayTurningLaneRestrictionCount++;
					        			TurnCost tr = new TurnCost(in.getSegment().getSegmentId(), intersection.getId(), out.getSegment().getSegmentId(), 
					        					(byte)0, WeeklyTimeRange.ALWAYS, TurnRestrictionType.X, "GENERATED: Logically implied ramp-to-ramp restriction (multi-way)", null);
				        				addTurnCost(turnCosts, tr);
					        			//outputTRRow(tr);
					        		} else {
					        			multiwayTurningLaneNonRestrictionCount++;
					        		}
					        	}
					        }
				        }
		        	}
		        }
		        
		        // identify turn restrictions where divided segments merge together (effectively a U-turn, more like a V-turn)
		        if(ends.size() >= 3 && dividedSegs.size() >= 2) {
		        	for(RpStreetEnd in : dividedSegs) {
			        	// skip outgoing segments
			        	if(in.getTravelDir().equals(TravelDirection.FORWARD)) continue;
			        	// loop over all segments again (to find outgoing segments)
			        	for(RpStreetEnd out : dividedSegs) {
			        		// skip incoming 1-way, height-separated, or differently named segments
			        		if(out.getTravelDir().equals(TravelDirection.REVERSE)
			        				|| (in.getTrafficImpactor().equals(TrafficImpactor.OVERPASS)
			        						!= out.getTrafficImpactor().equals(TrafficImpactor.OVERPASS))
			        				|| (in.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS)
			        						!= out.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS))
			        				|| !in.getSegment().getName().equals(out.getSegment().getName())) continue;
			        		int relativeAngle = (out.getAngle() + 360 - in.getAngle()) % 360;
			        		// hard left turn angle 
			        		if(relativeAngle >= 240) {
			        			// now look for the third segment, not divided, with the same name
			        			boolean validThird = false;
			        			for(RpStreetEnd third : ends) {
			        				// skip one-way, divided, height-separated or differently named segments
			        				if(third.getTravelDir().isOneWay()
			        						|| third.getSegment().getDividerType().isDivided()
				        					|| (in.getTrafficImpactor().equals(TrafficImpactor.OVERPASS)
				        							!= third.getTrafficImpactor().equals(TrafficImpactor.OVERPASS))
				        					|| (in.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS)
				        							!= third.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS))
			        						) continue;
		        					validThird = true;
			        			}
			        			if(validThird) {
				        			dividedEndRestrictionCount++;
				        			TurnCost tr = new TurnCost(in.getSegment().getSegmentId(), intersection.getId(), out.getSegment().getSegmentId(), 
				        					(byte)0, WeeklyTimeRange.ALWAYS, TurnRestrictionType.V, "GENERATED: Logically implied divided-end u/v-turn restriction", null);
			        				addTurnCost(turnCosts, tr);
				        			//outputTRRow(tr);
			        			}
			        		} 
			        	}
		        	}
		        }
		        
		        // identify u-turn restrictions where divided segments are bridged by a crossing segment 
		        // which should not be used to make a u-turn
	        	for(RpStreetEnd in : dividedSegs) {
		        	// skip outgoing segments and 2-node intersections
		        	if(in.getTravelDir().equals(TravelDirection.FORWARD)
		        			|| ends.size() <= 2) continue;
		        	// loop over all segments again (to find outgoing segments)
		        	for(RpStreetEnd between : ends) {
		        		Double betweenSegLength = between.getSegment().getCenterLine().getLength();
		        		 RpStreetIntersection secondInt = intersectionIdMap.get(between.getOtherEnd().getIntersectionId());
		        		// skip incoming 1-way, height-separated, or too long segments,
		        		// and 2-node intersections
		        		if(between.getTravelDir().equals(TravelDirection.REVERSE)
		        				|| (in.getTrafficImpactor().equals(TrafficImpactor.OVERPASS)
		        						!= between.getTrafficImpactor().equals(TrafficImpactor.OVERPASS))
		        				|| (in.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS)
		        						!= between.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS))
		        				|| betweenSegLength > 45
		        				|| secondInt.getEnds().size() <= 2) continue;
        				// inAngle must be a slight to steep left turn but not a hairpin
		        		int inAngle = (between.getAngle() + 360 - in.getAngle()) % 360;
        				if(inAngle < 210 || inAngle > 330) continue;
        				
	        			// now look for the third segment, on the other end of the between segment
	        			for(RpStreetEnd out : secondInt.getEnds()) {
			        		// skip incoming 1-way, undivided, or height-separated segments,
	        				// or cases where the in and out segs are not highways and the between seg is too large
	        				if(out.getTravelDir().equals(TravelDirection.REVERSE)
	        						|| !out.getSegment().getDividerType().isDivided()
		        					|| (between.getOtherEnd().getTrafficImpactor().equals(TrafficImpactor.OVERPASS)
		        							!= out.getTrafficImpactor().equals(TrafficImpactor.OVERPASS))
		        					|| (between.getOtherEnd().getTrafficImpactor().equals(TrafficImpactor.UNDERPASS)
		        							!= out.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS))
		        	        		|| ((in.getSegment().getHighwayRoute1() == null
			        						|| out.getSegment().getHighwayRoute1() == null)
			        							&& betweenSegLength > 26)
	        						) continue;
	        				// outAngle must be a slight to steep left turn but not a hairpin
			        		int outAngle = (out.getAngle() + 360 - between.getOtherEnd().getAngle()) % 360;
	        				if(outAngle < 210 || outAngle > 330) continue;
	        				
			        		// overall u-turn angle must be close to a 180 turn 
	        				int returnAngle = (out.getAngle() + 360 - in.getAngle()) % 360;
			        		if(returnAngle > 305 || returnAngle < 45) {
			        			uTurnRestrictionCount++;
			        			TurnCost tr = new TurnCost(in.getSegment().getSegmentId() + "|" 
			        					+ in.getIntersectionId() + "|" 
			        					+ between.getSegment().getSegmentId() + "|" 
			        					+ out.getIntersectionId() + "|" 
			        					+ out.getSegment().getSegmentId(),
			        					(byte)0, WeeklyTimeRange.ALWAYS, TurnRestrictionType.U, "GENERATED: Logically implied u-turn restriction", null);
		        				addTurnCost(turnCosts, tr);
			        			//outputTRRow(tr);
		        			} else {
		        				uTurnNonRestrictionCount++;
		        			}
		        		} 
		        	}
	        	}

		        
		        return true;
			}

			private FerryRoute buildFerryRoute(RpStreetEnd ferryEnd, FerryRoute fr) {
				if(fr == null) {
					fr = new FerryRoute(ferryEnd.getSegment());
				} else {
					fr.addSeg(ferryEnd.getSegment());
				}
        		if(ferrySegments.get(ferryEnd.getSegment().getSegmentId()) == null) {
        			return null; // already handled
        		}
        		RpStreetIntersection curInt = intersectionIdMap.get(ferryEnd.getOtherEnd().getIntersectionId()); 

        		// check each other end at the intersection for non-ferry
        		for(RpStreetEnd end : curInt.getEnds()) {
        			// if it is not a ferry, then this is a terminal intersection 
        			if(!RoadClass.FERRY.equals(end.getSegment().getRoadClass())) {
        				return new FerryRoute(ferryEnd.getSegment());
        			}
        		}
    			FerryRoute bestRoute = null;
        		// for each other end at the intersection
        		for(RpStreetEnd end : curInt.getEnds()) {
        			// if it is a ferry and not already in the route and has the same name
        			if(RoadClass.FERRY.equals(end.getSegment().getRoadClass()) 
        					&& !fr.segs.contains(end.getSegment()) 
        					&& end.getSegment().getName().equals(ferryEnd.getSegment().getName())) {
        				// get the route
        				FerryRoute newFr = buildFerryRoute(end, new FerryRoute(fr));
        				if(newFr != null && (bestRoute == null || newFr.length < bestRoute.length)) {
        					bestRoute = newFr;
        				}
        			}
        		}
        		if(bestRoute == null) {
        			logger.error("Error merging ferry segments for ferry named '{}' related segment Id: {}",  ferryEnd.getSegment().getName(), ferryEnd.getSegment().getSegmentId());
        			return null;
        		}
        		bestRoute.addSeg(ferryEnd.getSegment());
        		return bestRoute;
			}
		});
		if(!BASE_ONLY) { 
			readTurnCosts(turnCosts, dataDir + "turn_restrictions_custom.csv");
		}
		
		logWriter.close();
		//trWriter.close();
		logger.info("Number of ferry segments compiled: {}", ferryCount);
		logger.info("Number of restrictions dropped: {}", droppedTRs);
		logger.info("Number of non-routeable segments dropped: {}", droppedSegs);
		
		logger.info("Turn restriction generation --------------------------------");
		logger.info("Number of isolated turning lanes detected: {}", isolatedTurningLaneCount);
		logger.info("Number of turning lanes restrictions added: {}", turningLaneRestrictionCount);
		logger.info("Number of turning lanes not restricted: {}", turningLaneNonRestrictionCount);
		logger.info("Number of multi-way turning lane restrictions added: {}", multiwayTurningLaneRestrictionCount);
		logger.info("Number of multi-way turning lanes not restricted: {}", multiwayTurningLaneNonRestrictionCount);
		logger.info("Number of divided end turn restrictions added: {}", dividedEndRestrictionCount);
		logger.info("Number of u-turn restrictions added: {}", uTurnRestrictionCount);
		logger.info("Number of u-turns not restricted based on angle: {}", uTurnNonRestrictionCount);
		
		logger.info("Writing output segments...");
		writeSegments(segments);

		String fileName = "turn_costs.csv";
		if(BASE_ONLY) {
			fileName = "turn_costs_base.csv";
		}
		writeTurnCosts(turnCosts, fileName);
		//dataSource.close();
	}
	
	protected boolean nameIsTurningLaneOrRamp(String name) {
		if(name == null) return false;
		if(name.equalsIgnoreCase("turning lane") 
				|| name.equalsIgnoreCase("highway ramp")
				|| name.endsWith("Onramp")
				|| name.endsWith("Offramp")) {
			return true;
		}
		return false;
	}

//	private void outputTRRow(TurnCost turnCost) {
//		Map<String,Object> row = new THashMap<String,Object>();
//		row.put("EDGE_NODE_SET", turnCost.getIdSeqString());
//		trWriter.writeRow(row);
//	}

	private void outputTraffic(List<RpStreetSegment> segments) {
		File htFile = new File(dataDir + "traffic_historic.csv");
		List<String> htSchema = Arrays.asList("tlid","dotw","time","f_speed","r_speed");
		XsvRowWriter htWriter = new XsvRowWriter(htFile, ',', htSchema, false);
		int htCount = 0;
				
		for(RpStreetSegment segment : segments) {
			RoadClass roadClass = segment.getRoadClass();
			Double trafficMultiplier = trafficMultiplierMap.get(roadClass);
			if(trafficMultiplier != null) {
				Map<String,Object> row = new THashMap<String,Object>();
				row.put("tlid", segment.getSegmentId());
				row.put("dotw", "1|2|3|4|5");
				row.put("time", "08:00");
				row.put("f_speed", Math.round(segment.getSpeedLimit() * trafficMultiplier));
				row.put("r_speed", Math.round(segment.getSpeedLimit() * trafficMultiplier));
				htWriter.writeRow(row);
				htCount++;
				row.put("time", "16:00");
				htWriter.writeRow(row);
				htCount++;
			}
		}
		
		htWriter.close();
		logger.info("Historic Traffic Entries written: {}", htCount);

	}

	private TIntObjectHashMap<String> loadStreetNames() {
		// build a map from the StreetNameId to the Name String
		RowReader reader = new JsonRowReader(dataDir + "street_load_street_names.json", geometryFactory);
		TIntObjectHashMap<String> nameIdMap = new TIntObjectHashMap<String>(60000);
		int count = 0;
		while(reader.next()) {
			count++;
			int id = reader.getInt("street_name_id");
			String nameBody = reader.getString("name_body");
			String streetType = reader.getString("street_type");
			String streetTypeIsPrefix = reader.getString("street_type_is_prefix_ind");
			String streetDir = reader.getString("street_dir");
			String streetDirIsPrefix = reader.getString("street_dir_is_prefix_ind");
			String streetQualifier = reader.getString("street_qualifier");
			
			String fullName = null;
			if(nameBody == null || nameBody.isEmpty()) {
				fullName = "Unnamed Street";
			} else {
				boolean typeIsPrefix = "Y".equals(streetTypeIsPrefix);
				boolean dirIsPrefix = "Y".equals(streetDirIsPrefix);
				fullName = (streetDir != null && dirIsPrefix ? streetDir + " " : "")
						+ (streetType != null && typeIsPrefix ? streetType + " " : "")
						+ nameBody
						+ (streetType != null && !typeIsPrefix ? " " + streetType : "")
						+ (streetDir != null && !dirIsPrefix ? " " + streetDir : "")
						+ (streetQualifier != null ? " " + streetQualifier : "");
			}
			nameIdMap.put(id, fullName);
		}
		reader.close();
		logger.debug("Street Name count: {}", count);
		return nameIdMap;
	}

	private TIntIntHashMap loadStreetNameOnSegs() {
		// build a map from the StreetSegmentId to primary StreetNameId
		RowReader reader = new JsonRowReader(dataDir + "street_load_street_name_on_seg_xref.json", geometryFactory);
		TIntIntHashMap nameIdBySegmentIdMap = new TIntIntHashMap(300000);
		int count = 0;
		// loop over array of features
		while(reader.next()) {
			count++;
			int segmentId = reader.getInt("street_segment_id");
			int nameId = reader.getInt("street_name_id");
			String isPrimary = reader.getString("is_primary_ind");
			
			if(nameId >= 0 && segmentId >= 0 && "Y".equals(isPrimary)) {
				nameIdBySegmentIdMap.put(segmentId, nameId);
			}
		}
		reader.close();
		logger.debug("Street Name On Segment count: {}", count);
		return nameIdBySegmentIdMap;
	}
	
	private void addTurnCost(TIntObjectHashMap<List<TurnCost>> turnCosts, TurnCost newTurnCost) {
		if(newTurnCost == null) return;
		List<TurnCost> tcl = turnCosts.get(newTurnCost.getIdSeq()[0]);
		if(tcl == null) {
			tcl = new ArrayList<TurnCost>(4);
			turnCosts.put(newTurnCost.getIdSeq()[0], tcl);
		}
		for(TurnCost oldTurnCost : tcl) {
			if(oldTurnCost.getIdSeqString().equals(newTurnCost.getIdSeqString())) {
				// we are overwriting an existing cost 
				if(newTurnCost.getCost() >= 0) {
					oldTurnCost.setCost(newTurnCost.getCost());
				}
				oldTurnCost.setRestriction(newTurnCost.getRestriction());
				oldTurnCost.setType(newTurnCost.getType());
				if(newTurnCost.getSourceDescription() != null && !"".equals(newTurnCost.getSourceDescription())) {
					if(oldTurnCost.getSourceDescription() == null || "".equals(oldTurnCost.getSourceDescription())) {
						oldTurnCost.setSourceDescription(newTurnCost.getSourceDescription());
					} else {
						oldTurnCost.setSourceDescription(oldTurnCost.getSourceDescription() + " OVERRIDDEN BY " + newTurnCost.getSourceDescription());
					}
				}
				oldTurnCost.setCustomDescription(newTurnCost.getCustomDescription());
				return;
			}
		}
		// we didn't find a matching turn cost, so this one is new
		// if there is no cost and no restriction, we don't need to add it
		if(newTurnCost.getCost() == 0 && newTurnCost.getRestriction() == null) {
			return;
		}
		tcl.add(newTurnCost);
	}
	
	// groups the stubs at each intersection by trafficImpactor and counts them 
	// to characterize the intersection
	// then counts the number intersections with the same characterization
	private static void analyzeStubs(TIntObjectHashMap<RpStreetIntersection> intersectionIdMap) {
		HashMap<String,Integer> counts = new HashMap<String, Integer>();
		for(TIntObjectIterator<RpStreetIntersection> mapIt = intersectionIdMap.iterator(); mapIt.hasNext(); ) {
			mapIt.advance();
			RpStreetIntersection intersection = mapIt.value();
			EnumMap<TrafficImpactor,Integer> imps = new EnumMap<TrafficImpactor,Integer>(TrafficImpactor.class);
			for(RpStreetEnd end : intersection.getEnds()) {
				Integer impCount = imps.get(end.getTrafficImpactor());
				if(impCount == null) {
					imps.put(end.getTrafficImpactor(), 1);
				} else {
					imps.put(end.getTrafficImpactor(), impCount+1);
				}
			}
			String allImps = intersection.getEnds().size() + ":" + imps.toString();
			Integer impCounts = counts.get(allImps);
			if(impCounts == null) {
				counts.put(allImps, 1);
			} else {
				counts.put(allImps, impCounts + 1);
			}
		}
		logger.debug(counts.toString());	
	}
	
	private void readTurnCosts(TIntObjectHashMap<List<TurnCost>> turnCosts, String filename) {
		RowReader turnCostReader = new CsvRowReader(filename, geometryFactory);
		while(turnCostReader.next()) {
			String idSeq = turnCostReader.getString("EDGE_NODE_SET");
			Integer cost = turnCostReader.getInteger("TRAVERSAL_COST");
			if(cost == null) {
				cost = -1;
			}
			String dayCodeStr = turnCostReader.getString("DAY_CODE");
			String timeRangeStr = turnCostReader.getString("TIME_RANGES");
			WeeklyTimeRange restriction = WeeklyTimeRange.create(dayCodeStr, timeRangeStr);
			TurnRestrictionType type = TurnRestrictionType.convert(turnCostReader.getString("TYPE"));
			String description = turnCostReader.getString("DESCRIPTION");
			addTurnCost(turnCosts, new TurnCost(idSeq, (byte)(int)cost, restriction, type, "", description));
		}
	}
	private void writeSegments(List<RpStreetSegment> segments) {
		File streetsFile = new File(dataDir + "street_load_street_segments_router.json");
		RowWriter streetWriter = new JsonRowWriter(streetsFile, "bgeo_street_segments");
		int segCount = 0;
		for(RpStreetSegment seg : segments) {
			Map<String,Object> row = new THashMap<String,Object>();
			row.put("STREET_SEGMENT_ID", seg.getSegmentId());
			row.put("START_INTERSECTION_ID", seg.getStartIntersectionId());
			row.put("END_INTERSECTION_ID", seg.getEndIntersectionId());
			row.put("ROAD_CLASS", seg.getRoadClass());
			row.put("TRAVEL_DIRECTION", seg.getTravelDirection());
			row.put("DIVIDER_TYPE", seg.getDividerType().toString());
			row.put("START_TRAFFIC_IMPACTOR", seg.getStartTrafficImpactor());
			row.put("END_TRAFFIC_IMPACTOR", seg.getEndTrafficImpactor());
			row.put("SPEED_LIMIT", (int)seg.getSpeedLimit());
			row.put("VIRTUAL_IND", seg.isVirtual() ? "Y" : "N");
			row.put("SURFACE_TYPE", seg.getSurfaceType());
			row.put("VEHICLE_MAX_HEIGHT", Double.isNaN(seg.getMaxHeight()) ? null : seg.getMaxHeight());
			row.put("VEHICLE_MAX_WEIGHT", seg.getMaxWeight());
			if(seg.getSegmentId() == 460188 || seg.getSegmentId() == 460195) {
				row.put("VEHICLE_MAX_WIDTH", 10);
			} else {
				row.put("VEHICLE_MAX_WIDTH", Double.isNaN(seg.getMaxWidth()) ? null : seg.getMaxWidth());
			}
			if(seg.isDeadEnded()) {
				row.put("DEAD_ENDED_IND", "Y");
			}
			row.put("TRUCK_ROUTE_IND", seg.isTruckRoute() ? "Y" : "N");
			row.put("geom", seg.getCenterLine());
			streetWriter.writeRow(row);
			segCount++;
		}
		streetWriter.close();
		logger.info("Segments written: {}", segCount);
	}
	
	private void writeTurnCosts(TIntObjectHashMap<List<TurnCost>> turnCosts, String fileName) {
		File trFile = new File(dataDir + fileName);
		List<String> trSchema = Arrays.asList("ID","EDGE_NODE_SET","DAY_CODE","TIME_RANGES","TRAVERSAL_COST","TYPE","SOURCE_DESCRIPTION","CUSTOM_DESCRIPTION");
		XsvRowWriter trWriter = new XsvRowWriter(trFile, ',', trSchema, false);
		
		turnCosts.forEachValue(new TObjectProcedure<List<TurnCost>>() {
			@Override
			public boolean execute(List<TurnCost> tcl) {
				for(TurnCost tc : tcl) {
					trCount++;
					Map<String,Object> row = new THashMap<String,Object>();
					row.put("ID", trCount);
					row.put("EDGE_NODE_SET", tc.getIdSeqString());
					row.put("DAY_CODE", tc.getRestriction() == null ? "" : DayCode.of(tc.getRestriction().getDaySet()));
					row.put("TIME_RANGES", tc.getRestriction() == null ? "" : tc.getRestriction().getTimeRangeString());
					row.put("TRAVERSAL_COST", tc.getCost());
					row.put("TYPE", tc.getType());
					row.put("SOURCE_DESCRIPTION", tc.getSourceDescription());
					row.put("CUSTOM_DESCRIPTION", tc.getCustomDescription());
					trWriter.writeRow(row);
				}
				return true;
			}
		});
		trWriter.close();
		logger.info("Turn Costs written: {}", trCount);
	}

	private TurnCost buildTurnCost(RpStreetEnd inSegEnd, RpStreetIntersection intersection, 
			RpStreetEnd outSegEnd, byte cost, WeeklyTimeRange restriction) {
		if(outSegEnd.getTravelDir() == TravelDirection.REVERSE) {
			// don't need to add a cost to an impossible turn onto the wrong way of a one-way street
			if(restriction != null) {
				logRestriction(inSegEnd, true, "Ignoring TR onto wrong way of one-way street");
			}
			return null;
		}
		// no costs on over/underpasses
		if(inSegEnd.getTrafficImpactor().equals(TrafficImpactor.OVERPASS) || inSegEnd.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS)
				||outSegEnd.getTrafficImpactor().equals(TrafficImpactor.OVERPASS) || outSegEnd.getTrafficImpactor().equals(TrafficImpactor.UNDERPASS)) {
			// don't need to add a cost to an impossible turn off/onto an overpass
			if(restriction != null) {
				logRestriction(inSegEnd, true, "Ignoring impossible TR off/onto overpass");
			}
			return null;
			
		}
		if(inSegEnd.getSegment().isFerry() || outSegEnd.getSegment().isFerry()) {
			// don't need to add a cost to ferry turn
			if(restriction != null) {
				logRestriction(inSegEnd, true, "Ignoring TR involving ferry segment");
			}
			return null;			
		}
		return new TurnCost(inSegEnd.getSegment().getSegmentId(), intersection.getId(), outSegEnd.getSegment().getSegmentId(), 
				cost, restriction, null, restriction == null ? "" : "ITN", null);
	}	

	private static EnumMap<RoadClass,Double> buildTrafficMultiplierMap() {
		EnumMap<RoadClass,Double> map = new EnumMap<RoadClass,Double>(RoadClass.class);
		map.put(RoadClass.COLLECTOR_MAJOR, 0.6);
		map.put(RoadClass.COLLECTOR_MINOR, 0.6);
		map.put(RoadClass.ARTERIAL_MAJOR, 0.5);
		map.put(RoadClass.ARTERIAL_MINOR, 0.5);
		map.put(RoadClass.HIGHWAY_MAJOR, 0.4);
		map.put(RoadClass.HIGHWAY_MINOR, 0.4);
		map.put(RoadClass.FREEWAY, 0.4);
		return map;
	}
	
	private EnumMap<TrafficImpactor,byte[][]> buildTurnCostMatrix() {
		byte[][] lightMatrix = {{5,5,5},{4,7,5},{10,10,15}};
		byte[][] stopMatrix = {{5,5,10},{5,7,5},{10,10,15}};
		byte[][] yieldMatrix = {{5,5,5},{4,7,5},{10,10,15}};
		EnumMap<TrafficImpactor,byte[][]> matrix = new EnumMap<TrafficImpactor,byte[][]>(TrafficImpactor.class);
		matrix.put(TrafficImpactor.LIGHT, lightMatrix);
		matrix.put(TrafficImpactor.STOPSIGN, stopMatrix);
		matrix.put(TrafficImpactor.YIELD, yieldMatrix);
		matrix.put(TrafficImpactor.ROUNDABOUT, yieldMatrix);
		return matrix;
	}
	
	private byte getTurnCost(RpStreetEnd thisEnd, RpStreetEnd otherEnd) {
		int onGroup = thisEnd.getSegment().getRoadClass().getGroup();
		int acrossGroup = otherEnd.getSegment().getRoadClass().getGroup();
		if(onGroup == 0 || acrossGroup == 0) return 0;
		byte[][] matrix = turnCostMatrix.get(thisEnd.getTrafficImpactor());
		if(matrix == null) return 0;
		return matrix[onGroup-1][acrossGroup-1];
	}
	
	private void openLogWriter() {
		File logFile = new File(dataDir + "router_process_log.json");
		logWriter = new JsonRowWriter(logFile, "router_process_log");
	}
	
	private void logRestriction(RpStreetEnd se, boolean handled, String message) {
		Map<String, Object> row = new HashMap<String,Object>();
		row.put("SEGMENT_ID", se.getSegment().getSegmentId());
		row.put("END", se.getEnd());
		row.put("HANDLED", handled);
		row.put("MESSAGE", message);
		row.put("geom", se.getSegment().getCenterLine());
		logWriter.writeRow(row);
	}

}

class FerryRoute {
	double length;
	List<RpStreetSegment> segs;
	RpStreetEnd startEnd = null;
	RpStreetEnd endEnd = null;
	
	private FerryRoute(double length, List<RpStreetSegment> segs) {
		this.length = length;
		this.segs = segs;
	}	

	public FerryRoute(FerryRoute base) {
		this(base.length, new ArrayList<RpStreetSegment>(base.segs));
	}

	public FerryRoute(RpStreetSegment seg) {
		length = seg.getCenterLine().getLength();
		segs = new ArrayList<RpStreetSegment>();
		segs.add(seg);
	}
	
	public void addSeg(RpStreetSegment seg) {
		length += seg.getCenterLine().getLength();
		segs.add(seg);
	}
	
	
	public LineString buildLine() {
		if(segs.size() == 1) {
			startEnd = segs.get(0).getStartEnd();
			endEnd = segs.get(0).getEndEnd();
			return segs.get(0).getCenterLine();
		}
		List<Coordinate> coords = new ArrayList<Coordinate>();
		for(int segIdx = 0; segIdx < segs.size(); segIdx++) {
			RpStreetSegment seg = segs.get(segIdx);
			LineString ls = seg.getCenterLine(); 
			boolean reverse = false;
			if(segIdx == 0) {
				if(seg.getStartIntersectionId() == segs.get(1).getStartIntersectionId()
						|| seg.getStartIntersectionId() == segs.get(1).getEndIntersectionId()) {
					reverse = true;
					startEnd = segs.get(0).getEndEnd();
				} else {
					startEnd = segs.get(0).getStartEnd();
				}
			} else {
				if(seg.getEndIntersectionId() == segs.get(segIdx-1).getStartIntersectionId()
						|| seg.getEndIntersectionId() == segs.get(segIdx-1).getEndIntersectionId()) {
					reverse = true;
				}
				if(segIdx == segs.size()-1) {
					if(reverse) {
						endEnd = segs.get(segIdx).getStartEnd();
					} else {
						endEnd = segs.get(segIdx).getEndEnd();
					}
				}
			}
			if(reverse) {
				for(int coordIdx = ls.getNumPoints() - (segIdx == 0 ? 1 : 2); coordIdx >= 0; coordIdx--) {
					coords.add(ls.getCoordinateN(coordIdx));
				}
			} else {
				for(int coordIdx = (segIdx == 0 ? 0 : 1); coordIdx < ls.getNumPoints(); coordIdx++) {
					coords.add(ls.getCoordinateN(coordIdx));
				}
			}
		}
		return segs.get(0).getCenterLine().getFactory().createLineString(coords.toArray(new Coordinate[coords.size()]));
	}
	
}