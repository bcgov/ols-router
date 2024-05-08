package ca.bc.gov.ols.router.restrictions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthLocationMap;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;
import ca.bc.gov.ols.router.engine.basic.BasicGraphInternal;
import ca.bc.gov.ols.router.engine.basic.SegmentIdLookup;
import ca.bc.gov.ols.router.restrictions.rdm.Restriction;
import ca.bc.gov.ols.router.status.RdmStatusMessage;
import ca.bc.gov.ols.router.status.StatusMessage;
import ca.bc.gov.ols.router.util.Azimuth;
import ca.bc.gov.ols.util.MapList;
import gnu.trove.map.hash.TIntObjectHashMap;


public class RestrictionLookupBuilder {
	private static final Logger logger = LoggerFactory.getLogger(RestrictionLookupBuilder.class.getCanonicalName());
	
	// note this tolerance needs to take into account that MOT azimuths are calculated in lat/lon while we are using albers
	private static final double AZIMUTH_TOLERANCE = 10; 
	
	private SegmentIdLookup segIdLookup;
	private BasicGraphInternal internalGraph;
	private TIntObjectHashMap<ArrayList<Constraint>> restrictionMap = new TIntObjectHashMap<ArrayList<Constraint>>();
	private MapList<Integer, Restriction> laneBasedRestrictions = new MapList<Integer, Restriction>();
	private List<StatusMessage> messages = new ArrayList<StatusMessage>();

	public RestrictionLookupBuilder(SegmentIdLookup segIdLookup, BasicGraphInternal internalGraph) {
		this.segIdLookup = segIdLookup;
		this.internalGraph = internalGraph;
	}
	
	private void addRestrictionInternal(int edgeId, Constraint r) {
		ArrayList<Constraint> constraints = restrictionMap.get(edgeId);
		if(constraints == null) {
			constraints = new ArrayList<Constraint>(1);
			restrictionMap.put(edgeId, constraints);
		}
		constraints.add(r);		
	}
	
	public void addRestriction(int edgeId, Restriction r) {
		if(r == null) return;
		if(r.laneNumber > 0 ) {
			if(internalGraph.getNumLanes(edgeId) >= r.laneNumber) {
				laneBasedRestrictions.add(edgeId, r);
			} else {
				logger.warn("Restriction on non-existent lane {} of {}; restrictionId: {}, segmentId: {}", r.laneNumber, internalGraph.getNumLanes(edgeId), r.id, internalGraph.getSegmentId(edgeId));
				messages.add(new RdmStatusMessage(r.id, "Restriction on non-existent lane " + r.laneNumber + " of " + internalGraph.getNumLanes(edgeId) + "; restrictionId: " + r.id + ", segmentId: " + internalGraph.getSegmentId(edgeId)));
			}
		} else {
			addRestrictionInternal(edgeId, r);
		}
	}

	public void addRestrictions(List<Restriction> restrictions) {
		for(Restriction r : restrictions) {
			addRestriction(r);
		}
	}

	public void addRestriction(Restriction r) {
		if(r == null) return;
		int[] edgeIds = segIdLookup.getEdgesForSegment(r.segmentId);
		if(edgeIds == null) {
			logger.warn("RDM Restriction on unknown segmentID: {}", r.segmentId);
			messages.add(new RdmStatusMessage(r.id, "RDM Restriction on unknown segmentID: " + r.segmentId));
			return;
		}
		// negative azimuth means both directions
		if(r.azimuth < 0) {
			// both directions
			for(int edgeId : edgeIds) {
				addRestriction(edgeId, r);
			}
		} else {
			// determine the direction of the restriction
			LineString ls = internalGraph.getLineString(edgeIds[0]);
			double azimuthDiff = Azimuth.compareAzimuth(ls, r.azimuth);
			if(azimuthDiff > AZIMUTH_TOLERANCE && azimuthDiff < 180 - AZIMUTH_TOLERANCE) {
				// azimuth difference is out of tolerance
				logger.warn("RDM Restriction with out-of tolerance azimuth (diff: {}) on segmentId: {}", String.format("%.0f", azimuthDiff), r.segmentId);
				messages.add(new RdmStatusMessage(r.id, "RDM Restriction with out-of tolerance azimuth (diff: " + String.format("%.0f", azimuthDiff) + ") on segmentId: " + r.segmentId));
			} else if(azimuthDiff <= AZIMUTH_TOLERANCE) {
				// azimuth in same direction as linestring
				addRestriction(edgeIds[0], r);
			} else if(edgeIds.length == 2) {
				// azimuth in reverse direction of linestring
				addRestriction(edgeIds[1], r);
			} else {
				logger.warn("RDM Restriction on reverse of one-way segmentId: {}", r.segmentId);
				messages.add(new RdmStatusMessage(r.id, "RDM Restriction on reverse of one-way segmentId: " + r.segmentId));
			}
		}
	}
	
	private void buildLaneRestrictions() {
		// loop over the edges with lane-based restrictions
		for(Entry<Integer, List<Restriction>> entry : laneBasedRestrictions.entrySet()) {
			int edgeId = entry.getKey();
			// group the restrictions by location
			MapList<Integer,Restriction> locationMap = new MapList<Integer,Restriction>();
			for(Restriction r : entry.getValue()) {
				locationMap.add(r.locationId, r);
			}
			// loop over the locations
			for(List<Restriction> restrictions : locationMap.values()) {
				Restriction r0 = restrictions.get(0);
				int segmentId = r0.segmentId;
				int numLanes = internalGraph.getNumLanes(edgeId);
				Point location = r0.location;
				int locationId = r0.locationId;
				RestrictionSource source = r0.source;
				RestrictionType type = r0.type;
				
				// calculate the distance along the segment to the restriction location
				LineString seg = internalGraph.getLineString(edgeId);
				LocationIndexedLine locationIndexedLine = new LocationIndexedLine(seg);
			    LinearLocation ref = locationIndexedLine.project(location.getCoordinate());
				double dist = new LengthLocationMap(seg).getLength(ref);
			    
				int[] id = new int[numLanes];
				Arrays.fill(id, 0);
				double[] permitableValue = new double[numLanes];
				Arrays.fill(permitableValue, Double.POSITIVE_INFINITY);
				for(Restriction r : restrictions) {
					if(r.laneNumber <= numLanes) {
						id[r.laneNumber-1] = r.id;
						permitableValue[r.laneNumber-1] = r.permitableValue;
					}
					if(r.segmentId != segmentId) {
						logger.info("Restriction at same location with different segmentId");
						messages.add(new RdmStatusMessage(r.id, "Restriction at same location with different segmentId"));
					}
					if(r.source != source) {
						logger.info("Restriction at same location with different source");
						messages.add(new RdmStatusMessage(r.id, "Restriction at same location with different source"));
					}
					if(r.type != type) {
						logger.info("Restriction at same location with different type");
						messages.add(new RdmStatusMessage(r.id, "Restriction at same location with different type"));
					}
				}
				LaneBasedRestriction lbr = new LaneBasedRestriction(id, source, type, permitableValue, location, locationId, dist);
				addRestrictionInternal(edgeId, lbr);
			}
			
		}
	}

	public RestrictionLookup build() {
		buildLaneRestrictions();
		return new RestrictionLookup(restrictionMap, messages);
	}
		
}

