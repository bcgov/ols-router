/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import java.io.IOException;
import java.io.Reader;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.TurnClass;
import ca.bc.gov.ols.router.data.TurnRestriction;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.engine.GraphBuilder;
import ca.bc.gov.ols.router.open511.parser.Open511Parser;
import ca.bc.gov.ols.router.rdm.Restriction;
import ca.bc.gov.ols.router.rdm.RestrictionBuilder;
import ca.bc.gov.ols.rowreader.JsonRowReader;
import ca.bc.gov.ols.rowreader.RowReader;

public class RouterDataLoader {
	private final static Logger logger = LoggerFactory.getLogger(RouterDataLoader.class.getCanonicalName());

	private RouterConfig config;
	private RouterDataSource dataSource;
	private GraphBuilder graphBuilder;
	
	public RouterDataLoader(RouterConfig config, RouterDataSource dataSource, GraphBuilder graphBuilder) {
		this.config = config;
		this.dataSource = dataSource;
		this.graphBuilder = graphBuilder;
	}
	
	public void loadData() throws IOException {
		logger.info("Starting loading Router data structure...");
		long startTime = System.currentTimeMillis();
		logger.debug("Max memory:{}", (Runtime.getRuntime().maxMemory() / 1000000));
		
		StreetSegment seg;
		while((seg = dataSource.getNextSegment()) != null) {
			graphBuilder.addEdge(seg);
		}
		
		TurnRestriction cost;
		while((cost = dataSource.getNextTurnRestriction()) != null) {
			graphBuilder.addTurnRestriction(cost);
		}
		
		loadTurnClasses(dataSource.getTurnClassReader());
		
		Open511Parser parser = new Open511Parser(new GeometryFactory(new PrecisionModel(),4326));
		graphBuilder.addEvents(parser.parseEventResponse(dataSource.getOpen511Reader()));
		
		loadRestrictions(dataSource.getRestrictionReader(), new GeometryFactory(RouterConfig.BASE_PRECISION_MODEL, 3005));
		
		graphBuilder.addTraffic(dataSource.getTrafficReader());
		
		graphBuilder.addTruckNotices(dataSource.getTruckNoticeReader(), dataSource.getTruckNoticeMappingReader());
		
		graphBuilder.addLocalDistortionField(dataSource.getLocalDistortionFieldReader());

		graphBuilder.addSchedules(dataSource.getGtfs(), dataSource.getGTFSMappingReader());

		graphBuilder.setDates(dataSource.getDates());
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("Finished loading Router data structure ({} secs).", elapsedTime / 1000);
		logger.debug("Memory in use after loading(Megs): {}",
				((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000));
	}

	private void loadTurnClasses(RowReader tcReader) {
		while(tcReader.next()) {
			String idSeq = tcReader.getString("EDGE_NODE_SET");
			TurnDirection turnDirection = TurnDirection.valueOf(tcReader.getString("TURN_DIRECTION"));
			graphBuilder.addTurnClass(new TurnClass(idSeq, turnDirection));
		}
	}
	
	private void loadRestrictions(Reader reader, GeometryFactory gf) throws IOException{
		JsonReader jr = new JsonReader(reader);
		int count = 0;
		int invalidCount = 0;
		jr.beginArray();
		while(jr.hasNext()) {
			if(!parseRestriction(jr, gf)) invalidCount++;
			count++;
		}
		jr.close();
		logger.info("Read {} RDM restrictions from file.", count);
		logger.info("Invalid (negative) restriction values: {}", invalidCount);
	}
	
	private boolean parseRestriction(JsonReader jr, GeometryFactory gf) throws IOException {
		jr.beginObject();
		RestrictionBuilder rb = Restriction.builder();
		double azimuth = -1;
		while(jr.hasNext()) {			
			String name = jr.nextName();
			switch(name) {
			case "RESTRICTION_ID":
				rb.id(jr.nextInt());
				break;
			case "RESTRICTION_TYPE":
				rb.type(RestrictionType.convert(jr.nextString()));
				break;
			case "PERMITABLE_VALUE":
				rb.permitableValue(jr.nextDouble());
				break;
			case "RESTRICTION_AZIMUTH":
				if(jr.peek() == JsonToken.NUMBER) {
					azimuth = jr.nextDouble();
				} else {
					jr.skipValue();
				}
				break;
			case "NETWORK_SEGMENT_ID":
				rb.segmentId(jr.nextInt());
				break;
			case "LANE_NUMBER":
				if(jr.peek() == JsonToken.NUMBER) {
					rb.laneNumber(jr.nextInt());
				} else {
					jr.skipValue();
				}
				break;
			case "FEATURE_SOURCE_SYSTEM":
				JsonToken next = jr.peek();
				if(next == JsonToken.STRING) {
					rb.featureSource(jr.nextString());
				} else {
					jr.skipValue();
				}
				break;
			case "GEOMETRY":
				rb.location((Point)JsonRowReader.parseJsonGeometry(jr, gf));
				break;
			default:
				jr.skipValue();
			}
		}
		jr.endObject();
		rb.source(RestrictionSource.RDM);
		Restriction r = rb.build();
		if(rb != null) {
			graphBuilder.addRestriction(r, azimuth);
		}
		return true;
	}
	
}
