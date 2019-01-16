/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.vividsolutions.jts.geom.GeometryFactory;

import ca.bc.gov.ols.router.data.enumTypes.Environment;

public class CassandraRouterConfig extends RouterConfig {
	private static final Logger logger = LoggerFactory.getLogger(CassandraRouterConfig.class.getCanonicalName());

	private Cluster cluster;
	private Session session;
	private String keyspace;
	private Environment environment;
	
	public CassandraRouterConfig(Properties props, GeometryFactory geometryFactory) {
		super(props, geometryFactory);
		
		cluster = Cluster.builder().addContactPoint(props.getProperty("config.cassandraContactPoint")).build();
		session = cluster.connect();
		keyspace = props.getProperty("config.cassandraKeyspace");
		environment = Environment.convert(props.getProperty("environment"));
		if(environment == null) {
			environment = Environment.DEVEL;
		}
		
		ResultSet results = session.execute("SELECT config_param_name, config_param_value FROM " + keyspace + ".bgeo_configuration_parameters "
				+ "WHERE app_id ='" + APP_ID + "'");
		for(Row row : results) {
			String name = row.getString("config_param_name");
			String value = row.getString("config_param_value");
			try {
				if(name.startsWith("dataSource.baseFileUrl")) {
					if((name.equals("dataSource.baseFileUrl") && dataSourceBaseFileUrl == null)
							|| name.equals("dataSource.baseFileUrl." + environment.toString().toLowerCase())) {
						dataSourceBaseFileUrl = value;
						if(dataSourceBaseFileUrl.endsWith("street_load")) {
							dataSourceBaseFileUrl = dataSourceBaseFileUrl.substring(0, dataSourceBaseFileUrl.lastIndexOf("street_load"));
						}
						if(!dataSourceBaseFileUrl.endsWith("/")) {
							dataSourceBaseFileUrl = dataSourceBaseFileUrl + "/";
						}
					} // we ignore values for other environments
				} else {
					switch(name) {
					case "copyrightLicenseURI":
							copyrightLicenseURI = value;
							break;
					case "copyrightNotice":
						copyrightNotice = value; 
						break;
					case "disclaimer":
						disclaimer = value; 
						break;
					case "glossaryBaseUrl":
						glossaryBaseUrl = value; 
						break;
					case "kmlStylesUrl":
						kmlStylesUrl = value; 
						break;
					case "moreInfoUrl":
						moreInfoUrl = value; 
						break;
					case "privacyStatement":
						privacyStatement = value; 
						break;
					case "maxPairs":
						maxPairs = Integer.parseInt(value); 
						break;
					case "maxRoutePoints":
						maxRoutePoints = Integer.parseInt(value); 
						break;
					case "enableTurnRestrictions":
						enableTurnRestrictions = Boolean.parseBoolean(value); 
						break;
					default:
						logger.warn("Unused configuration parameter '{}' with value '{}'", name, value);
					}
				}
			} catch(IllegalArgumentException iae) {
				logger.warn("Unparseable configuration parameter '{}' with value '{}'", name, value);
			}
		}
		session.close();
		cluster.close();
	}
	
}
