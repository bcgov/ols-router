/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.datasources;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.app.router.RouterConfig;
import ca.bc.gov.app.router.data.StreetSegment;
import ca.bc.gov.app.router.data.TurnCost;
import ca.bc.gov.app.router.engine.GraphBuilder;
import ca.bc.gov.app.router.open511.parser.Open511Parser;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class RouterDataLoader {
	private final static Logger logger = LoggerFactory.getLogger(RouterDataLoader.class.getCanonicalName());

	public static void loadData(RouterConfig config, RouterDataSource dataSource, GraphBuilder graphBuilder) throws IOException {
		logger.info("Starting loading Router data structure...");
		long startTime = System.currentTimeMillis();
		logger.debug("Max memory:{}", (Runtime.getRuntime().maxMemory() / 1000000));
		
		StreetSegment seg;
		while((seg = dataSource.getNextSegment()) != null) {
			graphBuilder.addEdge(seg);
		}
		
		TurnCost cost;
		while((cost = dataSource.getNextTurnCost()) != null) {
			graphBuilder.addTurnCost(cost);
		}
		
		Open511Parser parser = new Open511Parser(new GeometryFactory(new PrecisionModel(),4326));
		graphBuilder.addEvents(parser.parseEventResponse(dataSource.getOpen511Reader()));
		
		graphBuilder.addTraffic(dataSource.getTrafficReader());
		
		graphBuilder.addSchedules(dataSource.getGtfs(), dataSource.getGTFSMappingReader());
		

		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("Finished loading Router data structure ({} secs).", elapsedTime / 1000);
		logger.debug("Memory in use after loading(Megs): {}",
				((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000));
	}
	
}
