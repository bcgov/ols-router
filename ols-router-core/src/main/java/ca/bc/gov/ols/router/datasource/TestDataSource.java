package ca.bc.gov.ols.router.datasource;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.rowreader.FlexObj;
import ca.bc.gov.ols.rowreader.FlexObjListRowReader;
import ca.bc.gov.ols.rowreader.RowReader;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestDataSource extends FileRouterDataSource {
    private final static Logger logger = LoggerFactory.getLogger(FileRouterDataSource.class.getCanonicalName());

    private TIntObjectHashMap<FlexObj> streetNames = new TIntObjectHashMap<FlexObj>();
    private Map<String, Integer> streetNameSchema = FlexObj.createSchema(new String[] {
            "street_name_id", "name_body", "street_type",
            "street_direction", "street_qualifier", "street_muni_quad",
            "street_type_is_prefix_ind", "street_direction_is_prefix_ind"});

    private List<FlexObj> streetNameOnSegments = new ArrayList<FlexObj>();
    private Map<String, Integer> streetNameOnSegmentSchema = FlexObj.createSchema(
            new String[] {"street_name_id", "street_segment_id", "is_primary_ind"});

    private List<FlexObj> turnClasses = new ArrayList<FlexObj>();
    private Map<String, Integer> turnClassesSchema = FlexObj.createSchema(
            new String[] {"EDGE_NODE_SET", "TURN_DIRECTION"});

    private List<FlexObj> turnRestrictions = new ArrayList<FlexObj>();
    private Map<String, Integer> turnRestrictionsSchema = FlexObj.createSchema(
            new String[] { "ID","EDGE_NODE_SET", "DAY_CODE", "TIME_RANGES", "TYPE", "VEHICLE_TYPES", "source_description", "custom_description"});

    private TIntObjectHashMap<FlexObj> streetSegments = new TIntObjectHashMap<FlexObj>();
    private Map<String, Integer> streetSegmentSchema = FlexObj.createSchema(
            new String[] {"street_segment_id",
                    "start_intersection_id", "end_intersection_id",
                    "road_class", "virtual_ind", "travel_direction", "divider_type",
                    "left_locality", "right_locality", "start_traffic_impactor", "end_traffic_impactor",
                    "speed_limit", "surface_type", "vehicle_max_height", "vehicle_max_width",
                    "from_vehicle_max_weight", "to_vehicle_max_weight", "highway_route_1", "highway_route_2",
                    "highway_route_3", "truck_route_ind", "start_xing_class", "end_xing_class", "dead_ended_ind", "geom"});

    private List<FlexObj> trafficHistoric = new ArrayList<FlexObj>();
    private Map<String, Integer> trafficHistoricSchema = FlexObj.createSchema(
            new String[] {"tlid", "dotw", "time", "f_speed", "r_speed"});

    private List<FlexObj> truckNotices = new ArrayList<FlexObj>();
    private Map<String, Integer> truckNoticesSchema = FlexObj.createSchema(
            new String[] {"TRUCK_NOTICE_ID", "TYPE", "DESCRIPTION", "START_DATE", "END_DATE"});

    private List<FlexObj> truckNoticeMapping = new ArrayList<FlexObj>();
    private Map<String, Integer> truckNoticeMappingSchema = FlexObj.createSchema(
            new String[] {"STREET_SEGMENT_ID", "TRUCK_NOTICE_ID"});

    private List<FlexObj> localDistortionField = new ArrayList<FlexObj>();
    private Map<String, Integer> localDistortionFieldSchema = FlexObj.createSchema(
            new String[] {"STREET_SEGMENT_ID", "VEHICLE_TYPES", "FRICTION_FACTOR", "LDF_COMMENT"});

    private List<FlexObj> gtfsMapping = new ArrayList<FlexObj>();
    private Map<String, Integer> gtfsMappingSchema = FlexObj.createSchema(
            new String[] {"route_id", "itn_name", "min_wait_time", "crossing_time", "is_scheduled", "has_fare", "truck_notice_id"});


    public TestDataSource(RouterConfig config, GeometryFactory geometryFactory) {
        super();
        logger.trace("{}() constructor called", getClass().getName());
        this.config = config;
        this.geometryFactory = geometryFactory;

        // StreetSegments IDs in 2000-range
        streetSegments.put(2001, new FlexObj(streetSegmentSchema,
                new Object[] {2001, 1001, 1002, "local", "N", "B", "N", "Victoria", "Victoria", "", "",
                        50, "P", 100.0, 100.0, 100, 100, "", "", "", "1", "SAME", "SAME", "Y",
                        geometryFactory.createLineString(new Coordinate[]
                                {new Coordinate(100, 100), new Coordinate(200, 200)})
                }));

        // StreetNames IDs in 3000-range
        streetNames.put(3001, new FlexObj(streetNameSchema,
                new Object[] {3001, "Douglas", "St", null, null, null, "N", "N"}));

        streetNameOnSegments.add(new FlexObj(streetNameOnSegmentSchema,
                new Object[] {3001, 2001, "Y"}));

        trafficHistoric.add(new FlexObj(trafficHistoricSchema,
                new Object[] {2001,"1|2|3|4|5","08:00",10,10}));

        truckNotices.add(new FlexObj(truckNoticesSchema,
                new Object[] {1,"restriction","Speed Limit of 30km/h", LocalDate.parse("1999-12-31"),LocalDate.parse("2025-12-31")}));

        truckNoticeMapping.add(new FlexObj(truckNoticeMappingSchema,
                new Object[] {2001,1}));

        gtfsMapping.add(new FlexObj(gtfsMappingSchema,
                new Object[] {"1","Tsawwassen-Swartz Bay Ferry",10,95,"y","y",48}));

        turnRestrictions.add(new FlexObj(turnRestrictionsSchema, new Object[] {14409,"9001|9002","SS","ALWAYS","","","ITN",""}));

        turnClasses.add(new FlexObj(turnClassesSchema, new Object[] {"9001|9002","LEFT"}));

        localDistortionField.add(new FlexObj(localDistortionFieldSchema, new Object[] {2001,"TRUCK",3.50,"Douglas St"}));

        streetNameById = loadStreetNames();
        streetNameIdBySegmentId = loadStreetNameOnSegs();

        segmentReader = getStreetSegments();
        turnRestrictionReader = getTurnRestrictions();
    }

    @Override
    protected RowReader getStreetNames() {
        return new FlexObjListRowReader(streetNames.valueCollection());
    }

    @Override
    protected RowReader getStreetSegments() {
        return new FlexObjListRowReader(streetSegments.valueCollection());
    }

    private RowReader getTurnRestrictions() {
        return new FlexObjListRowReader(turnRestrictions);
    }

    @Override
    protected RowReader getStreetNameOnSegments() {
        return new FlexObjListRowReader(streetNameOnSegments);
    }

    @Override
    public String getNameBySegmentId(int segmentId) {
        return streetNameById.get(streetNameIdBySegmentId.get(segmentId));
    }

    @Override
    public RowReader getTurnClassReader() {
        return new FlexObjListRowReader(turnClasses);
    }

    @Override
    public Reader getOpen511Reader() throws IOException {
        String event = "{\n" +
                "    \"events\": [" +
                "{\n" +
                "            \"jurisdiction_url\": \"http://api.open511.gov.bc.ca/jurisdiction\",\n" +
                "            \"url\": \"http://api.open511.gov.bc.ca/events/drivebc.ca/-58631\",\n" +
                "            \"id\": \"drivebc.ca/-58631\",\n" +
                "            \"headline\": \"CONSTRUCTION\",\n" +
                "            \"status\": \"ACTIVE\",\n" +
                "            \"created\": \"2016-05-30T14:14:04-07:00\",\n" +
                "            \"updated\": \"2018-09-05T15:34:42-07:00\",\n" +
                "            \"description\": \"Construction\",\n" +
                "            \"+ivr_message\": \"Construction\",\n" +
                "            \"schedule\": {\n" +
                "    \"        recurring_schedules\": [\n" +
                "        {\n" +
                "            \"days\": [\n" +
                "                1,\n" +
                "                2,\n" +
                "                3,\n" +
                "                4,\n" +
                "                5\n" +
                "            ],\n" +
                "            \"start_date\": \"2016-05-30\",\n" +
                "            \"daily_start_time\": \"21:00\",\n" +
                "            \"end_date\": \"2018-10-05\",\n" +
                "            \"daily_end_time\": \"06:00\"\n" +
                "        }\n" +
                "    ]\n" +
                "},\n" +
                "            \"event_type\": \"CONSTRUCTION\",\n" +
                "            \"event_subtypes\": [\n" +
                "                \"ROAD_MAINTENANCE\"\n" +
                "            ],\n" +
                "            \"severity\": \"MINOR\",\n" +
                "            \"geography\": {\n" +
                "                \"type\": \"Point\",\n" +
                "                \"coordinates\": [\n" +
                "                    1,\n" +
                "                    1\n" +
                "                ]\n" +
                "            },\n" +
                "            \"roads\": [\n" +
                "                {\n" +
                "                    \"name\": \"Douglas St.\",\n" +
                "                    \"from\": \"\",\n" +
                "                    \"direction\": \"BOTH\",\n" +
                "                    \"state\": \"SOME_LANES_CLOSED\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"areas\": [\n" +
                "                {\n" +
                "                    \"url\": \"http://www.geonames.org/8630138\",\n" +
                "                    \"name\": \"Victoria\",\n" +
                "                    \"id\": \"drivebc.ca/5\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "      ]" +
                "    }";

        InputStream is = new ByteArrayInputStream( event.getBytes() );
        return new InputStreamReader(is);
    }

    @Override
    public RowReader getTrafficReader() throws IOException {
        return new FlexObjListRowReader(trafficHistoric);
    }

    @Override
    public GtfsDaoImpl getGtfs() throws IOException {
        GtfsDaoImpl gtfs = new GtfsDaoImpl();
        return gtfs;
    }

    @Override
    public RowReader getGTFSMappingReader() throws IOException {
        return new FlexObjListRowReader(gtfsMapping);
    }

    @Override
    public RowReader getTruckNoticeReader() throws IOException {
        return new FlexObjListRowReader(truckNotices);
    }

    @Override
    public RowReader getTruckNoticeMappingReader() throws IOException {
        return new FlexObjListRowReader(truckNoticeMapping);
    }

    @Override
    public RowReader getLocalDistortionFieldReader() throws IOException {
        return new FlexObjListRowReader(localDistortionField);
    }
}
