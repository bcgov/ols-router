/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router;

import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.GeometryFactory;

import ca.bc.gov.ols.router.api.GeometryReprojector;

public class RouterFactory {
	static final Logger logger = LoggerFactory.getLogger(RouterFactory.class.getCanonicalName());
	
	private Properties bootstrapConfig = getBootstrapConfigFromEnvironment();
	private GeometryFactory geometryFactory;
	private GeometryReprojector geometryReprojector;
	
	public RouterFactory() {
	}
	
	public void setCassandraContactPoint(String contactPoint) {
		bootstrapConfig.setProperty("OLS_CASSANDRA_CONTACT_POINT", contactPoint);
	}

	public void setCassandraLocalDatacenter(String datacenter) {
		bootstrapConfig.setProperty("OLS_CASSANDRA_LOCAL_DATACENTER", datacenter);
	}

	public void setCassandraKeyspace(String keyspace) {
		bootstrapConfig.setProperty("OLS_CASSANDRA_KEYSPACE", keyspace);
	}

	public void setCassandraReplicationFactor(String replFactor) {
		bootstrapConfig.setProperty("OLS_CASSANDRA_REPL_FACTOR", replFactor);
	}

	public void setConfigurationStore(String configStore) {
		bootstrapConfig.setProperty("OLS_GEOCODER_CONFIGURATION_STORE", configStore);
	}

	public void setFileConfigurationUrl(String fileConfigUrl) {
		bootstrapConfig.setProperty("OLS_FILE_CONFIGURATION_URL", fileConfigUrl);
	}

	public void setGeometryFactory(GeometryFactory gf) {
		geometryFactory = gf;
	}

	public void setGeometryReprojector(GeometryReprojector gr) {
		geometryReprojector = gr;
	}

	public Router getRouter() {
		logger.info("{}: Creating new router instance", getClass().getName());
		return new Router(bootstrapConfig, geometryFactory, geometryReprojector);
	}
	
//	public static Router getRouterFromProperties(GeometryReprojector reprojector) {
//		logger.debug("RouterFactory.getRouterFromProperties() called");
//		Properties props = loadProps();
//		GeometryFactory gf = geometryFactoryFromProps(props);
//		return new Router(props, gf, reprojector);
//	}
	
//	public static GeometryFactory geometryFactoryFromProps(Properties props) {
//		String baseSrsCodeStr = props.getProperty("baseSrsCode");
//		try {
//			int baseSrsCode = Integer.parseInt(baseSrsCodeStr);
//			return new GeometryFactory(RouterConfig.BASE_PRECISION_MODEL,
//					baseSrsCode);
//		} catch(NumberFormatException nfe) {
//			logger.error("Invalid or no baseSrsCode defined in properties file: \""
//					+ baseSrsCodeStr + "\"");
//			throw new RuntimeException(nfe);
//		}
//		
//	}
	
	public static Properties getBootstrapConfigFromEnvironment() {
		Properties bootstrapConfig = new Properties();
		bootstrapConfig.setProperty("OLS_CASSANDRA_APP_ID", "ROUTER");
		Optional.ofNullable(System.getenv("OLS_FILE_CONFIGURATION_URL")).ifPresent(e -> bootstrapConfig.setProperty("OLS_FILE_CONFIGURATION_URL", e));
		bootstrapConfig.setProperty("OLS_CASSANDRA_CONTACT_POINT", Optional.ofNullable(System.getenv("OLS_CASSANDRA_CONTACT_POINT")).orElse("cassandra"));
		bootstrapConfig.setProperty("OLS_CASSANDRA_LOCAL_DATACENTER", Optional.ofNullable(System.getenv("OLS_CASSANDRA_LOCAL_DATACENTER")).orElse("datacenter1"));
		bootstrapConfig.setProperty("OLS_CASSANDRA_KEYSPACE", Optional.ofNullable(System.getenv("OLS_CASSANDRA_KEYSPACE")).orElse("bgeo"));
		bootstrapConfig.setProperty("OLS_CASSANDRA_REPL_FACTOR", Optional.ofNullable(System.getenv("OLS_CASSANDRA_REPL_FACTOR")).orElse("2"));
		bootstrapConfig.setProperty("OLS_ROUTER_CONFIGURATION_STORE", Optional.ofNullable(System.getenv("OLS_ROUTER_CONFIGURATION_STORE"))
				.orElse("ca.bc.gov.ols.config.CassandraConfigurationStore"));
		return bootstrapConfig;
	}
	
	
}
