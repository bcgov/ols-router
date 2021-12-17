/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Set;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.enums.DividerType;
import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.enums.TravelDirection;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.TurnRestriction;
import ca.bc.gov.ols.router.data.WeeklyTimeRange;
import ca.bc.gov.ols.router.data.enums.SurfaceType;
import ca.bc.gov.ols.router.data.enums.TrafficImpactor;
import ca.bc.gov.ols.router.data.enums.TurnRestrictionType;
import ca.bc.gov.ols.router.data.enums.VehicleType;
import ca.bc.gov.ols.router.data.enums.XingClass;
import ca.bc.gov.ols.router.util.UrlCsvInputSource;
import ca.bc.gov.ols.rowreader.CsvRowReader;
import ca.bc.gov.ols.rowreader.JsonRowReader;
import ca.bc.gov.ols.rowreader.RowReader;
import ca.bc.gov.ols.rowreader.TsvRowReader;

public class FileRouterDataSource implements RouterDataSource {
	private final static Logger logger = LoggerFactory.getLogger(FileRouterDataSource.class.getCanonicalName());
	
	private RouterConfig config;
	private GeometryFactory geometryFactory;
	private TIntObjectHashMap<String> streetNameById;
	private TIntIntHashMap streetNameIdBySegmentId;
	private RowReader segmentReader;
	private int segmentCount = 0;
	private RowReader turnRestrictionReader;
	//private List<StreetSegment> ferrySegs;
	//private int nextFerrySeg = -1;
	//private TIntObjectHashMap<List<StreetSegment>> ferrySegsByIntId;

	public FileRouterDataSource(RouterConfig config, GeometryFactory geometryFactory) {
		logger.trace("{}() constructor called", getClass().getName());
		this.config = config;
		this.geometryFactory = geometryFactory;
		
		streetNameById = loadStreetNames();
		streetNameIdBySegmentId = loadStreetNameOnSegs();

		segmentReader = getStreetSegments();
		turnRestrictionReader = getTurnRestrictions();
		
		// save all the ferry segments for last
		//ferrySegs = new ArrayList<StreetSegment>(300);
		//ferrySegsByIntId = new TIntObjectHashMap<List<StreetSegment>>(600);
	}

	@Override
	public String getNameBySegmentId(int segmentId) {
		return streetNameById.get(streetNameIdBySegmentId.get(segmentId));
	}

	/**
	 * Gets the next available segment. Deals with filtering and buffering as necessary.
	 */
	@Override
	public StreetSegment getNextSegment() throws IOException {
		// we need to loop until we find a segment that we can return
		// ferry segments get buffered until the end;
		// non-routeable segments get ignored
		while(segmentReader.next()) {
//			if(segmentReader.next()) {
			segmentCount++;
			int segmentId = segmentReader.getInt("street_segment_id");
			int startIntersectionId = segmentReader.getInt("start_intersection_id");
			int endIntersectionId = segmentReader.getInt("end_intersection_id");
			String leftLocality = segmentReader.getString("left_locality");
			String rightLocality = segmentReader.getString("right_locality");
			RoadClass roadClass = RoadClass.convert(segmentReader.getString("road_class"));
			boolean isVirtual = "Y".equals(segmentReader.getString("virtual_ind")); 
			TravelDirection travelDir = TravelDirection.convert(segmentReader.getString("travel_direction"));
			DividerType dividerType = DividerType.convert(segmentReader.getString("divider_type"));
			TrafficImpactor startTrafficImpactor = TrafficImpactor.convert(segmentReader.getString("start_traffic_impactor"));
			TrafficImpactor endTrafficImpactor = TrafficImpactor.convert(segmentReader.getString("end_traffic_impactor"));
			short speedLimit = adjustSpeedLimit((short)segmentReader.getInt("speed_limit"), roadClass);
			SurfaceType surfaceType = SurfaceType.convert(segmentReader.getString("surface_type"));
			
			double maxHeight = segmentReader.getDouble("vehicle_max_height");
			double maxWidth = segmentReader.getDouble("vehicle_max_width");
			Integer fromMaxWeight = segmentReader.getInteger("from_vehicle_max_weight");
			Integer toMaxWeight = segmentReader.getInteger("to_vehicle_max_weight");

			String highwayRoute1 = segmentReader.getString("highway_route_1");
			if(highwayRoute1 != null) highwayRoute1 = highwayRoute1.intern();
			String highwayRoute2 = segmentReader.getString("highway_route_2");
			if(highwayRoute2 != null) highwayRoute2 = highwayRoute2.intern();
			String highwayRoute3 = segmentReader.getString("highway_route_3");
			if(highwayRoute3 != null) highwayRoute3 = highwayRoute3.intern();
			
			boolean isTruckRoute = "Y".equals(segmentReader.getString("truck_route_ind"));
			XingClass startXingClass = XingClass.convert(segmentReader.getString("start_xing_class"));
			XingClass endXingClass = XingClass.convert(segmentReader.getString("end_xing_class"));
			boolean isDeadEnded = "Y".equals(segmentReader.getString("dead_ended_ind"));
				
//			LaneRestriction laneRestriction = LaneRestriction.convert(segmentReader.getString("lane_restriction"));
//			AccessRestriction accessRestriction = AccessRestriction.convert(segmentReader.getString("access_restriction_ind"));
				
			LineString centerLine = segmentReader.getLineString();
				
			// switch intersection Ids to negative for overpasses to make them effectively a separate intersection
			if(TrafficImpactor.OVERPASS.equals(startTrafficImpactor)) {
				startIntersectionId = -startIntersectionId;
			}
			if(TrafficImpactor.OVERPASS.equals(endTrafficImpactor)) {
				endIntersectionId = -endIntersectionId;
			}
			if(travelDir.equals(TravelDirection.REVERSE)) {
				//TODO reverse the segment and flip all end attributes
				// there aren't actually any reverse segs in ITN so we are probably safe to ignore
				logger.warn("SegmentId {} with Travel Direction = reverse; ignoring!", segmentId);
				continue;
			}
			String fullName = streetNameById.get(streetNameIdBySegmentId.get(segmentId));
			if(fullName == null) {
				fullName = "unnamed";
				logger.warn("SegmentId {} has no name; possible data error?", segmentId);
			}
			StreetSegment segment = new StreetSegment(segmentId, centerLine,  
					startIntersectionId, endIntersectionId, 
					leftLocality, rightLocality, fullName, roadClass,
					travelDir, dividerType, startTrafficImpactor, endTrafficImpactor, 
					speedLimit, surfaceType, maxHeight, maxWidth, 
					fromMaxWeight, toMaxWeight,	isTruckRoute,
					highwayRoute1, highwayRoute2, highwayRoute3,
					startXingClass, endXingClass, isDeadEnded); //, laneRestriction, accessRestriction
//				if(segment.isFerry()) {
//					// save the ferry segs for last so we can prevent ferry-ferry intersections
//					ferrySegs.add(segment);
//					CollectionsHelper.addToTIntObjectMapList(ferrySegsByIntId, startIntersectionId, segment, 2);
//					CollectionsHelper.addToTIntObjectMapList(ferrySegsByIntId, endIntersectionId, segment, 2);
//				} else 
			if(segment.isFerry() ||(!isVirtual && roadClass.isRouteable())) {
				// we don't route on virtual segments or trails, or passenger ferries
				return segment;
			}
//			} else {
//				// no more segments to read, now return the ferry Segs
//				if(nextFerrySeg == -1) {
//					// handle ferry seg processing
//					for(TIntObjectIterator<List<StreetSegment>> it = ferrySegsByIntId.iterator(); it.hasNext();) {
//						it.advance();
//						int intId = it.key();
//						List<StreetSegment> segs = it.value();
//						if(segs.size() == 4) {
//							// i will be set to the segment that matches 0, if there are two pairs of matching segments
//							int i = 0;
//							int j = 0;
//							if(segs.get(0).getName().equals(segs.get(1).getName())
//									&& segs.get(2).getName().equals(segs.get(3).getName())) {
//								i = 1;
//								j = 2;
//							} else if(segs.get(0).getName().equals(segs.get(2).getName())
//									&& segs.get(1).getName().equals(segs.get(3).getName())) {
//								i = 2;
//								j = 1;
//							} else if(segs.get(0).getName().equals(segs.get(3).getName())
//									&& segs.get(1).getName().equals(segs.get(2).getName())) {
//								i = 3;
//								j = 1;
//							}
//							// if there were two pairs of matching segments
//							//Point p = null;
//							if(i != 0) {
//								// swap the intersections to -ve ids on 0 and i
//								if(segs.get(0).getStartIntersectionId() == intId) {
//									segs.get(0).setStartIntersectionId(-intId);
//									//p = segs.get(0).getCenterLine().getStartPoint();
//								} else {
//									segs.get(0).setEndIntersectionId(-intId);
//									//p = segs.get(0).getCenterLine().getEndPoint();
//								}
//								if(segs.get(i).getStartIntersectionId() == intId) {
//									segs.get(i).setStartIntersectionId(-intId);
//								} else {
//									segs.get(i).setEndIntersectionId(-intId);
//								}
//								logger.debug("Removed ferry intersection between '" + segs.get(0).getName()
//										+ "' and '" + segs.get(j).getName() + "'");
//							}
//						}
//					}
//					logger.debug(ferrySegs.size() + " Ferry Segments handled.");
//					nextFerrySeg = 0;
//				}
//				if(nextFerrySeg < ferrySegs.size()) {
//					return ferrySegs.get(nextFerrySeg++);
//				}
		}
		// we've returned everything!
		logger.debug("Street Segment count: {}", segmentCount);
		segmentReader.close();
		return null;
	}

	@Override
	public TurnRestriction getNextTurnRestriction() throws IOException {
		if(turnRestrictionReader.next()) {
			String idSeq = turnRestrictionReader.getString("EDGE_NODE_SET");
			String dayCodeStr = turnRestrictionReader.getString("DAY_CODE");
			String timeRangeStr = turnRestrictionReader.getString("TIME_RANGES");
			WeeklyTimeRange restriction = WeeklyTimeRange.create(dayCodeStr, timeRangeStr);
			TurnRestrictionType type = TurnRestrictionType.convert(turnRestrictionReader.getString("TYPE"));
			Set<VehicleType> vehicleTypes = VehicleType.fromList(turnRestrictionReader.getString("VEHICLE_TYPES"));
			return new TurnRestriction(idSeq, restriction, type, vehicleTypes, null, null);
		}
		return null;
	}
	
	@Override
	public RowReader getTurnClassReader() {
		return getXsvRowReader("turn_classes");
	}

	private TIntObjectHashMap<String> loadStreetNames() {
		// build a map from the StreetNameId to the Name String
		RowReader reader = getStreetNames();
		TIntObjectHashMap<String> nameIdMap = new TIntObjectHashMap<String>(60000);
		int count = 0;
		while(reader.next()) {
			count++;
			int id = reader.getInt("street_name_id");
			String nameBody = reader.getString("name_body");
			String streetType = reader.getString("street_type");
			String streetTypeIsPrefix = reader.getString("street_type_is_prefix_ind");
			String streetDir = reader.getString("street_direction");
			String streetDirIsPrefix = reader.getString("street_direction_is_prefix_ind");
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
		RowReader reader = getStreetNameOnSegments();
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
	
	private static short adjustSpeedLimit(short speedLimit, RoadClass roadClass) {
		// all road classes are listed just for completeness
		switch(roadClass) {
		case ALLEYWAY:
		case LANE:
		case RECREATION:
		case RESOURCE:
		case SERVICE:
		case RESTRICTED:
		case STRATA:
		case TRAIL: // trails are not loaded 
		case LOCAL:
		case RAMP:
			return (short)Math.round((float)speedLimit * 0.75f);
		case ARTERIAL_MAJOR:
		case ARTERIAL_MINOR:
		case COLLECTOR_MAJOR:
		case COLLECTOR_MINOR:
		case FERRY:
		case FREEWAY:
		case HIGHWAY_MAJOR:
		case HIGHWAY_MINOR:
		default: 
			return speedLimit;
		}
	}

	private RowReader getStreetSegments() {
		return getJsonRowReader("street_load_street_segments_router");
	}

	private RowReader getStreetNames() {
		return getJsonRowReader("street_load_street_names");
	}
	
	private RowReader getStreetNameOnSegments() {
		return getJsonRowReader("street_load_street_name_on_seg_xref");
	}
	
	private RowReader getTurnRestrictions() {
		return getXsvRowReader("turn_restrictions");
	}
	
	@Override
	public Reader getOpen511Reader() throws IOException {
		return new InputStreamReader(getInputStream("active_events.json"));
	}

	@Override
	public RowReader getTrafficReader() throws IOException {
		return getXsvRowReader("traffic_historic");
	}

	@Override
	public RowReader getTruckNoticeReader() throws IOException {
		return getXsvRowReader("truck_notices");
	}

	@Override
	public RowReader getTruckNoticeMappingReader() throws IOException {
		return getXsvRowReader("truck_notice_mappings");
	}

	@Override
	public RowReader getLocalDistortionFieldReader() throws IOException {
		return getXsvRowReader("local_distortion_fields");
	}

	@Override
	public GtfsDaoImpl getGtfs() throws IOException {
		GtfsReader reader = new GtfsReader();
	    reader.setInputSource(new UrlCsvInputSource(config.getDataSourceBaseFileUrl() + "gtfs/"));
	    GtfsDaoImpl gtfs = new GtfsDaoImpl();
	    reader.setEntityStore(gtfs);
	    reader.run();
	    return gtfs;
	}

	@Override
	public RowReader getGTFSMappingReader() throws IOException {
		InputStream is = getInputStream("gtfs/ITN_to_BCF.txt");
		return new CsvRowReader(new BufferedInputStream(is), geometryFactory);
	}

	private RowReader getJsonRowReader(String name) {
		try {
			InputStream is = getInputStream(name + ".json");
			return new JsonRowReader(is, geometryFactory);
		} catch(IOException ioe) {
			logger.error("Error opening stream for {} file.", name, ioe);
			throw new RuntimeException(ioe);
		}			
	}
	
	private RowReader getXsvRowReader(String name) {
		try {
			InputStream is = getInputStream(name + ".tsv");
			return new TsvRowReader(new BufferedInputStream(is), geometryFactory);
		} catch(IOException ioe) {
			try {
				logger.warn("Unable to open stream for file {}.tsv, falling back to .csv", name);
				InputStream is = getInputStream(name + ".csv");
				return new CsvRowReader(new BufferedInputStream(is), geometryFactory);
			} catch(IOException ioe2) {
				logger.error("Error opening stream for file {}.csv, assuming no data; exception message: {}", name, ioe2.getMessage());
				throw new RuntimeException(ioe);
			}
		}
	}
		
	private InputStream getInputStream(String name) throws IOException {
		String fileUrlString = config.getDataSourceBaseFileUrl() + name;
		logger.info("Reading from file: {}", fileUrlString);
		if(fileUrlString.startsWith("file:")) {
			return new FileInputStream(new File(fileUrlString.substring(5)));
		}
		URL fileUrl = new URL(fileUrlString);
		return fileUrl.openStream();		
	}

}
