/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.GeometryFactory;

import ca.bc.gov.ols.router.api.GeometryReprojector;

public class RouterFactory {
	static final Logger logger = LoggerFactory.getLogger(RouterFactory.class.getCanonicalName());
	
	private Properties properties = new Properties();
	private GeometryFactory geometryFactory;
	private GeometryReprojector geometryReprojector;
	
	public void setGeometryFactory(GeometryFactory gf) {
		geometryFactory = gf;
	}

	public void setGeometryReprojector(GeometryReprojector gr) {
		geometryReprojector = gr;
	}

	public Router getRouter() {
		logger.info("{}: Creating new router instance", getClass().getName());
		return new Router(properties, geometryFactory, geometryReprojector);
	}
	
	public static Router getRouterFromProperties(GeometryReprojector reprojector) {
		logger.debug("RouterFactory.getRouterFromProperties() called");
		Properties props = loadProps();
		GeometryFactory gf = geometryFactoryFromProps(props);
		return new Router(props, gf, reprojector);
	}
	
	public static GeometryFactory geometryFactoryFromProps(Properties props) {
		String baseSrsCodeStr = props.getProperty("baseSrsCode");
		try {
			int baseSrsCode = Integer.parseInt(baseSrsCodeStr);
			return new GeometryFactory(RouterConfig.BASE_PRECISION_MODEL,
					baseSrsCode);
		} catch(NumberFormatException nfe) {
			logger.error("Invalid or no baseSrsCode defined in properties file: \""
					+ baseSrsCodeStr + "\"");
			throw new RuntimeException(nfe);
		}
		
	}
	
	public static Properties loadProps() {
		Properties props = new Properties();
		try {
			props.load(RouterFactory.class.getClassLoader().getResourceAsStream("router.properties"));
			return props;
		} catch(IOException ioe) {
			logger.warn("Error locating/reading properties file: {}", ioe.getMessage());
		}
		return props;
	}
	
}
