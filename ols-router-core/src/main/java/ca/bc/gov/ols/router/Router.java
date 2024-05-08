/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.config.ConfigurationStore;
import ca.bc.gov.ols.router.api.GeometryReprojector;
import ca.bc.gov.ols.router.api.IsochroneResponse;
import ca.bc.gov.ols.router.api.NavInfoParameters;
import ca.bc.gov.ols.router.api.NavInfoResponse;
import ca.bc.gov.ols.router.api.RouterDirectionsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceBetweenPairsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceResponse;
import ca.bc.gov.ols.router.api.RouterOptimalDirectionsResponse;
import ca.bc.gov.ols.router.api.RouterOptimalRouteResponse;
import ca.bc.gov.ols.router.api.RouterRouteResponse;
import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.config.RouterConfigurationStoreFactory;
import ca.bc.gov.ols.router.datasource.DataUpdateManager;
import ca.bc.gov.ols.router.datasource.RouterDataLoader;
import ca.bc.gov.ols.router.datasource.RouterDataSource;
import ca.bc.gov.ols.router.datasource.RouterDataSourceFactory;
import ca.bc.gov.ols.router.engine.basic.BasicGraph;
import ca.bc.gov.ols.router.engine.basic.BasicGraphBuilder;
import ca.bc.gov.ols.router.engine.basic.BasicGraphRoutingEngine;
import ca.bc.gov.ols.router.status.StatusMessage;
import ca.bc.gov.ols.router.status.StatusMessage.Type;

public class Router {
	private static final Logger logger = LoggerFactory.getLogger(Router.class.getCanonicalName());

	private RouterConfig config;

	/* This is the global geometry factory, all geometries are created using it */
	private GeometryFactory geometryFactory;

	GeometryReprojector reprojector;

	private RoutingEngine engine;
	private DataUpdateManager dum;
	

	public Router(Properties bootstrapConfig, GeometryFactory gf,
			GeometryReprojector reprojector) {
		logger.debug("{} constructor called", getClass().getName());
		this.geometryFactory = gf;
		this.reprojector = reprojector;

		ConfigurationStore configStore = RouterConfigurationStoreFactory.getConfigurationStore(bootstrapConfig);
		if(this.geometryFactory == null) {
			this.geometryFactory = new GeometryFactory(RouterConfig.BASE_PRECISION_MODEL, Integer.parseInt(configStore.getConfigParam("baseSrsCode").get()));
		}
		config = new RouterConfig(configStore, geometryFactory);

		RouterDataSource dataSource = RouterDataSourceFactory.getRouterDataSource(config, geometryFactory);

		try {
			//config.baseSrsCode = GraphHopperRoutingEngine.GH_SRS;
			//engine = new GraphHopperRoutingEngine(config, dataSource, geometryFactory, reprojector);
			
			BasicGraphBuilder graphBuilder = new BasicGraphBuilder(config, reprojector);
			dum = new DataUpdateManager(config);
			RouterDataLoader loader = new RouterDataLoader(config, dataSource, graphBuilder, dum);
			loader.loadData();
			BasicGraph graph = graphBuilder.build();
			engine = new BasicGraphRoutingEngine(config, graph, geometryFactory, reprojector);
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public void update() {
		engine = engine.getUpdatedEngine(dum);
	}
	
	public RouterConfig getConfig() {
		return config;
	}

	public GeometryFactory getGeometryFactory() {
		return geometryFactory;
	}

	public GeometryReprojector getGeometryReprojector() {
		return reprojector;
	}

	public void close() {
		// nothing to do here yet
	}

	public RouterDistanceResponse distance(RoutingParameters params) {
		return engine.distance(params);
	}

	public RouterRouteResponse route(RoutingParameters params) {
		return engine.route(params);
	}

	public RouterDirectionsResponse directions(RoutingParameters params) {
		return engine.directions(params);
	}

	public RouterDistanceBetweenPairsResponse distanceBetweenPairs(RoutingParameters params) {
		return engine.distanceBetweenPairs(params);
	}

	public RouterOptimalRouteResponse optimalRoute(RoutingParameters params) {
		return engine.optimalRoute(params);
	}

	public RouterOptimalDirectionsResponse optimalDirections(RoutingParameters params) {
		return engine.optimalDirections(params);
	}

	public IsochroneResponse isochrones(RoutingParameters params) {
		return engine.isochrones(params);
	}

	public IsochroneResponse loop(RoutingParameters params) {
		return engine.loop(params);
	}

	public NavInfoResponse navInfo(NavInfoParameters params) {
		return engine.navInfo(params);
	}

	public List<StatusMessage> getMessages(Type type) {
		return engine.getMessages(type);
	}

}
