/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router;

import java.time.ZoneId;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class RouterConfig {
	public static final String VERSION = "1.5.1";
	public static final PrecisionModel BASE_PRECISION_MODEL = new PrecisionModel(1000);
	public static final float ERROR_TIME = -1;
	public static final float ERROR_DISTANCE = -1;
	public static final int EXPECTED_EDGES = 400000;
	protected static final String APP_ID = "ROUTER";
	public static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of("Canada/Pacific");
	private static final Logger logger = LoggerFactory.getLogger(RouterConfig.class.getCanonicalName());
	
	private static RouterConfig INSTANCE;
	
	protected int baseSrsCode = -1;
	protected String dataSourceBaseFileUrl;
	protected String copyrightLicenseURI = "";
	protected String copyrightNotice = "";
	protected String disclaimer = "http://www2.gov.bc.ca/gov/admin/disclaimer.page";
	protected String kmlStylesUrl = "http://openmaps.gov.bc.ca/kml/bgeo_results_styles.kml";
	protected String moreInfoUrl = "http://www.data.gov.bc.ca/dbc/geographic/locate/geocoding.page?";
	protected String glossaryBaseUrl = "http://www.data.gov.bc.ca/dbc/geographic/locate/physical_address_geo/glossary_of_terms.page?WT.svl=LeftNav";
	protected String privacyStatement = "http://www2.gov.bc.ca/gov/admin/privacy.page";
	protected int maxPairs = -1;
	protected int maxRoutePoints = -1;
	protected int maxOptimalRoutePoints = -1;
	protected String defaultDisableOptions = "";
	
	public RouterConfig() {
		INSTANCE = this;
	}
	
	public RouterConfig(Properties props, GeometryFactory gf) {
		INSTANCE = this;
		String baseSrsCodeStr = props.getProperty("baseSrsCode");
		try {
			baseSrsCode = Integer.parseInt(baseSrsCodeStr);
		} catch(NumberFormatException nfe) {
			logger.error("Invalid or no baseSrsCode defined in properties file: \""
					+ baseSrsCodeStr + "\"");
		}
	}

	public static RouterConfig getInstance() {
		if(INSTANCE == null) {
			logger.error("Unexpected request for uninitialized RouterConfig");
		}
		return INSTANCE;
	}
	
	public int getBaseSrsCode() {
		return baseSrsCode;
	}
	
	public String getDataSourceBaseFileUrl() {
		return dataSourceBaseFileUrl;
	}

	public String getVersion() {
		return VERSION;
	}
	
	public int getMaxPairs() {
		return maxPairs;
	}

	public int getMaxRoutePoints() {
		return maxRoutePoints;
	}

	public int getMaxOptimalRoutePoints() {
		return maxOptimalRoutePoints;
	}

	public String getCopyrightLicense() {
		return copyrightLicenseURI;
	}
	
	public String getCopyrightNotice() {
		return copyrightNotice;
	}
	
	public String getDisclaimer() {
		return disclaimer;
	}
	
	public String getKmlStylesUrl() {
		return kmlStylesUrl;
	}
	
	public String getMoreInfoUrl() {
		return moreInfoUrl;
	}
	
	public String getGlossaryBaseUrl() {
		return glossaryBaseUrl;
	}
	
	public String getPrivacyStatement() {
		return privacyStatement;
	}

	public String getDefaultDisableOptions() {
		return defaultDisableOptions;
	}

}
