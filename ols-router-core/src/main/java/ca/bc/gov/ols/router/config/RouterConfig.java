/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.config;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.Stream;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.config.ConfigurationParameter;
import ca.bc.gov.ols.config.ConfigurationStore;

public class RouterConfig {
	public static final String VERSION = "2.1.4";
	public static final PrecisionModel BASE_PRECISION_MODEL = new PrecisionModel(1000);
	public static final float ERROR_TIME = -1;
	public static final float ERROR_DISTANCE = -1;
	public static final int EXPECTED_EDGES = 400000;
	protected static final String APP_ID = "ROUTER";
	public static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of("Canada/Pacific");
	private static final Logger logger = LoggerFactory.getLogger(RouterConfig.class.getCanonicalName());
	
	private static RouterConfig INSTANCE;
	
	protected ConfigurationStore configStore;
	protected int baseSrsCode = -1;
	protected String dataSourceClassName;
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
	protected String defaultEnableOptions = "tc,xc,tr";
	protected double[] defaultXingCost = {5,7,10,1.5};
	protected double[] defaultTurnCost = {3,1,5,2};
	protected String defaultGlobalDistortionField = "";
	private double defaultTruckRouteMultiplier = 9;
		
	public RouterConfig() {
		INSTANCE = this;
	}
	
	public RouterConfig(ConfigurationStore configStore, GeometryFactory gf) {
		INSTANCE = this;
		this.configStore = configStore;

		Stream<ConfigurationParameter> configParams = configStore.getConfigParams();
		configParams.forEach(configParam -> {
			String name = configParam.getConfigParamName();
			String value = configParam.getConfigParamValue();
			try {
				switch(name) {
				case "baseSrsCode":
					baseSrsCode = Integer.parseInt(value);
					break;
				case "copyrightLicenseURI":
					copyrightLicenseURI = value;
					break;
				case "copyrightNotice":
					copyrightNotice = value; 
					break;
				case "dataSource.className":
					dataSourceClassName = value;
					break;
				case "dataSource.baseFileUrl":
					dataSourceBaseFileUrl = value;
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
				case "defaultEnableOptions":
					defaultEnableOptions = value; 
					break;
				case "defaultXingCost":
					defaultXingCost = Arrays.asList(value.split(",")).stream().mapToDouble(Double::parseDouble).toArray();
					break;
				case "defaultTurnCost":
					defaultTurnCost = Arrays.asList(value.split(",")).stream().mapToDouble(Double::parseDouble).toArray();
					break;
				case "defaultGlobalDistortionField":
					defaultGlobalDistortionField = value;
					break;
				case "defaultTruckRouteMultiplier":
					defaultTruckRouteMultiplier = Double.parseDouble(value);
					break;
				default:
					logger.warn("Unused configuration parameter '{}' with value '{}'", name, value);
				}
			} catch(IllegalArgumentException iae) {
				logger.warn("Unparseable configuration parameter '{}' with value '{}'", name, value);
			}
		});
		configStore.close();
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
	
	public String getDataSourceClassName() {
		return dataSourceClassName;
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

	public String getDefaultEnableOptions() {
		return defaultEnableOptions;
	}

	public double[] getDefaultXingCost() {
		return defaultXingCost;
	}

	public double[] getDefaultTurnCost() {
		return defaultTurnCost;
	}

	public String getDefaultGlobalDistortionField() {
		return defaultGlobalDistortionField;
	}

	public double getDefaultTruckRouteMultiplier() {
		return defaultTruckRouteMultiplier;
	}

	

}
