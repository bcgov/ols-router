/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.datasources;

import java.lang.reflect.Constructor;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.app.router.RouterConfig;

import com.vividsolutions.jts.geom.GeometryFactory;

public class RouterDataSourceFactory {
	private static final Logger logger = LoggerFactory.getLogger(RouterDataSourceFactory.class.getCanonicalName());
	
	public static RouterDataSource getRouterDataSource(Properties props, RouterConfig config,
			GeometryFactory geometryFactory) {
		logger.info("RouterDataSourceFactory.getRouterDataSource() called");
		String dataSourceClassName = props.getProperty("dataSource.class");
		// Super hack for now
		try {
			if(dataSourceClassName != null) {
				Class<?> cl = Class.forName(dataSourceClassName);
				Constructor<?> con = cl.getConstructor(new Class[] {RouterConfig.class,
						GeometryFactory.class});
				return (RouterDataSource)con.newInstance(config, geometryFactory);
			}
		} catch(ReflectiveOperationException roe) {
			throw new RuntimeException("Unable to load specified dataSource.class: "
					+ dataSourceClassName, roe);
		}
		throw new RuntimeException("No dataSource class specified in dataSource.class property");
	}
}
