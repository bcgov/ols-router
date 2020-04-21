/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.graphhopper;

import java.io.IOException;

import gnu.trove.map.hash.TIntIntHashMap;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.TurnCostExtension;
import com.graphhopper.util.PointList;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.router.api.GeometryReprojector;
import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.TurnClass;
import ca.bc.gov.ols.router.data.TurnRestriction;
import ca.bc.gov.ols.router.engine.GraphBuilder;
import ca.bc.gov.ols.router.open511.EventResponse;
import ca.bc.gov.ols.rowreader.RowReader;

public class GraphHopperGraphBuilder implements GraphBuilder {
	private static final Logger logger = LoggerFactory.getLogger(GraphHopperGraphBuilder.class.getCanonicalName());
	
	public static final int GH_SRS = 4326;

//	private static final int STOP_COST = 1;
//	private static final int LIGHT_COST = 3;
//	private static final int LEFT_COST = 3;

	private EncodingManager encodingManager = new EncodingManager(new CarFlagEncoder(5, 5, 63));
	private TurnCostExtension turnCostExt = new TurnCostExtension();
	
	private GraphHopperStorage ghStorage;
	private TIntIntHashMap intIdToNodeIdMap;
	private int nextNodeId = 1;
	private GeometryReprojector reprojector;
	// save stubs of all segments to handle turn costs at the end
	//Map<Integer,List<StreetSegmentStub>> stubsByNodeId;

	@SuppressWarnings("resource")
	public GraphHopperGraphBuilder(GeometryReprojector reprojector) {
		this.reprojector = reprojector;
		intIdToNodeIdMap = new TIntIntHashMap(400000);
		//stubsByNodeId = new HashMap<Integer, List<StreetSegmentStub>>();

		ghStorage = new GraphHopperStorage(new RAMDirectory(), encodingManager, false, turnCostExt).create(100);
	}
	
	public GraphHopperStorage getGHStorage() {
		return ghStorage;
	}
	
	/* (non-Javadoc)
	 * @see ca.bc.gov.app.router.routing.GraphBuilder#addEdge(ca.bc.gov.app.router.data.StreetSegment, int, int)
	 */
	@Override
	public void addEdge(StreetSegment seg) {
		int fromIntId = seg.getStartIntersectionId();
		int toIntId = seg.getEndIntersectionId();
		LineString way = reprojector.reproject(seg.getCenterLine(), GH_SRS);
		TravelDirection td = seg.getTravelDirection();
		long flags = encodingManager.flagsDefault(td.forwardAllowed(), td.reverseAllowed());
		CarFlagEncoder carFlagEncoder = (CarFlagEncoder)encodingManager.getEncoder("car");
		flags = carFlagEncoder.setSpeed(flags, seg.getSpeedLimit());
		
		int fromNodeId = getNodeId(fromIntId, way.getStartPoint());
		int toNodeId = getNodeId(toIntId, way.getEndPoint());
		int edgeId = ghStorage.edge(fromNodeId, toNodeId)
				.setWayGeometry(lineStringToPointList(way))
				.setDistance(seg.getCenterLine().getLength())
				.setFlags(flags)
				.setName(seg.getName())
				.getEdge();
		//CollectionsHelper.addToMapList(stubsByNodeId, fromNodeId, seg.getStartStub(edgeId), 4);
		//CollectionsHelper.addToMapList(stubsByNodeId, toNodeId, seg.getEndStub(edgeId), 4);

	}
	
	@Override
	public void addTurnRestriction(TurnRestriction cost) {
		// TODO Auto-generated method stub
		
	}

	private int addNode(int intId, Point point) {
		int nodeId = nextNodeId++;
		intIdToNodeIdMap.put(intId, nodeId);
		ghStorage.getNodeAccess().setNode(nodeId, point.getY(), point.getX());
		return nodeId;
	}

	private int getNodeId(int intId, Point p) {
		int nodeId = intIdToNodeIdMap.get(intId);
		if(nodeId == intIdToNodeIdMap.getNoEntryValue()) {
			return addNode(intId, p);
		}
		return nodeId;
	}
	
	@Override
	public void addEvents(EventResponse eventResponse) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void addTraffic(RowReader trafficReader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSchedules(GtfsDaoImpl gtfs, RowReader mappingReader)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	// add turn cost info to otherwise complete graph
//	public void calcTurnCosts() {
//		logger.debug("Calculating turn costs...");
//		int barricadeCount = 0;
//		int stopCount = 0;
//		int lightCount = 0;
//		int leftCount = 0;
//		int noneCount = 0;
//		int trCount = 0;
//		CarFlagEncoder carFlagEncoder = (CarFlagEncoder)encodingManager.getEncoder("car");
//		long lightFlags = carFlagEncoder.getTurnFlags(false, LIGHT_COST);
//		long stopFlags = carFlagEncoder.getTurnFlags(false, STOP_COST);
//		long leftFlags = carFlagEncoder.getTurnFlags(false, LEFT_COST);
//		long barricadeFlags = carFlagEncoder.getTurnFlags(true, 0);
//		for(Entry<Integer, List<StreetSegmentStub>> entry : stubsByNodeId.entrySet()) {
//			int nodeId = entry.getKey();
//			List<StreetSegmentStub> stubs = entry.getValue();
//			// order the stubs by angle
//			Collections.sort(stubs);
//			// loop over all incoming stubs
//			for(int in = 0; in < stubs.size(); in++) {
//				StreetSegmentStub inStub = stubs.get(in);
//				if(inStub.getTravelDir().equals(TravelDirection.FORWARD)) {
//					// this is an outgoing-only stub
//					continue;
//				}
//				
//				if(inStub.getLeftTR() == TurnTimeCode.SS_24) {
//	        		StreetSegmentStub outStub = stubs.get((in + stubs.size() - 1) % stubs.size());
//	        		if(outStub != inStub) {
//		        		turnCostExt.addTurnInfo(inStub.getEdgeId(), nodeId, outStub.getEdgeId(), barricadeFlags);
//		        		trCount++;
//	        		}
//	        	}
//				if(inStub.getCentreTR() == TurnTimeCode.SS_24) {
//					int idealAngle = (inStub.getAngle() + 180) % 360;
//					int bestDiff = 360;
//					int possibleStubs = 0;
//					StreetSegmentStub bestOutStub = null;
//					for(int out = 0; out < stubs.size(); out++) {
//						if(out == in) continue;
//						StreetSegmentStub outStub = stubs.get(out);
//						int diffAngle = Math.abs(idealAngle - outStub.getAngle());
//						if(diffAngle > 180) diffAngle = 360 - diffAngle;
//						if(diffAngle < 45) {
//							possibleStubs++;
//						}
//						if(diffAngle < bestDiff) {
//							bestDiff = diffAngle;
//							bestOutStub = outStub;
//						}
//					}
//					if(bestDiff > 45) {
//						logger.warn("Possible turn restriction issue: center is off by more than 45 degrees.");
//					}
//					if(possibleStubs > 1) {
//						logger.warn("Possible turn restriction issue: more than one possible center within 45 degrees.");
//					}
//					if(bestOutStub == null) {
//						logger.warn("Probable turn restriction issue; no centre found.");
//					} else {
//						turnCostExt.addTurnInfo(inStub.getEdgeId(), nodeId, bestOutStub.getEdgeId(), barricadeFlags);
//						trCount++;
//					}
//	        	}
//	        	if(inStub.getRightTR() == TurnTimeCode.SS_24) {
//	        		StreetSegmentStub outStub = stubs.get((in + 1) % stubs.size());
//	        		if(outStub != inStub) {
//	        			turnCostExt.addTurnInfo(inStub.getEdgeId(), nodeId, outStub.getEdgeId(), barricadeFlags);
//	        			trCount++;
//	        		}
//	        	}
//	        	
//				// iterate from right-most to left-most turn
//				// once you pass a street that doesn't have to stop for you, you are making a left
//				boolean isLeft = false;
//				// loop over all other outgoing stubs, going around the intersection counterclockwise
//				for(int out = (in + 1) % stubs.size(); out != in; out = (out + 1) % stubs.size()) {
//					StreetSegmentStub outStub = stubs.get(out);
//					if(outStub.getTravelDir().equals(TravelDirection.REVERSE)) {
//						// this is an incoming-only stub, skip it
//					} else if(outStub.getTrafficImpactor().equals(TrafficImpactor.BARRICADE)) {
//						// the outgoing stub is barricaded, set this turn as restricted
//						turnCostExt.addTurnInfo(inStub.getEdgeId(), nodeId, outStub.getEdgeId(), barricadeFlags);						
//					} else {
//						switch(inStub.getTrafficImpactor()) {
//						case BARRICADE:
//							turnCostExt.addTurnInfo(inStub.getEdgeId(), nodeId, outStub.getEdgeId(), barricadeFlags);
//							barricadeCount++;
//							break;
//						case LIGHT:
//							//turnCostExt.addTurnInfo(inStub.getEdgeId(), nodeId, outStub.getEdgeId(), lightFlags);
//							//lightCount++;
//							break;
//						case STOPSIGN:
//							//turnCostExt.addTurnInfo(inStub.getEdgeId(), nodeId, outStub.getEdgeId(), stopFlags);
//							//stopCount++;
//							break;
//						case CULDESAC:
//						case GATE:
//						case ROUNDABOUT:
//						case UNDERPASS:
//						case OVERPASS:
//						case YIELD:
//						case NONE:
//						default:
//							if(isLeft) {
//								//turnCostExt.addTurnInfo(inStub.getEdgeId(), nodeId, outStub.getEdgeId(), leftFlags);
//								//leftCount++;
//							} else {
//								//noneCount++;
//							}
//						}
//					}
//					// if this outStub is not an exit-only,
//					// and the incoming traffic doesn't have priority over outgoing stub's incoming traffic
//					if(!outStub.getTravelDir().equals(TravelDirection.FORWARD) 
//							&& !inStub.getTrafficImpactor().hasPriority(outStub.getTrafficImpactor())) {
//						// any further outStubs represent left turns
//						isLeft = true;
//					}
//				}
//				
//			}
//		}
//		logger.info("Turn Restrictions added: {}", trCount);
//		logger.debug("Turn costs calculated: barricades: " + barricadeCount
//				+ " stopSigns: " + stopCount 
//				+ " lights: " + lightCount
//				+ " lefts: " + leftCount
//				+ " noCost: " + noneCount);
//	}
	
	/*
	 * Note: The pointList required by GraphHopper doesn't include the start and end points.
	 */
	private PointList lineStringToPointList(LineString line) {
		PointList pointList = new PointList(line.getNumPoints()-2, false);
		// skip start and end points
		for(int i = 1; i < line.getNumPoints() - 1; i++) {
			pointList.add(line.getPointN(i).getY(), line.getPointN(i).getX());
		}
		return pointList;
	}

	public EncodingManager getEncodingManager() {
		return encodingManager;
	}

	@Override
	public void addTruckNotices(RowReader truckNoticeReader, RowReader truckNoticeMappingReader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTurnClass(TurnClass turnClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addLocalDistortionField(RowReader localDistortionFieldReader) {
		// TODO Auto-generated method stub
		
	}

}
