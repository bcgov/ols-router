/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import java.io.IOException;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.TurnClass;
import ca.bc.gov.ols.router.data.TurnRestriction;
import ca.bc.gov.ols.router.data.enums.TurnDirection;
import ca.bc.gov.ols.router.engine.GraphBuilder;
import ca.bc.gov.ols.router.open511.parser.Open511Parser;
import ca.bc.gov.ols.rowreader.RowReader;

public class RouterDataLoader {
	private final static Logger logger = LoggerFactory.getLogger(RouterDataLoader.class.getCanonicalName());

	private RouterConfig config;
	private RouterDataSource dataSource;
	private GraphBuilder graphBuilder;
	private DataUpdateManager dum;
	
	public RouterDataLoader(RouterConfig config, RouterDataSource dataSource, GraphBuilder graphBuilder, DataUpdateManager dum) {
		this.config = config;
		this.dataSource = dataSource;
		this.graphBuilder = graphBuilder;
		this.dum = dum;
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
		
		Open511Parser open511parser = new Open511Parser(new GeometryFactory(new PrecisionModel(),4326));
		graphBuilder.addEvents(open511parser.parseEventResponse(dataSource.getOpen511Reader()));
		graphBuilder.addRestrictions(dum.loadRdmRestrictions(dataSource.getRestrictionReader()));
		
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
	

}
