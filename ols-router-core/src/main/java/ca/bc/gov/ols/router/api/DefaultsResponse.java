package ca.bc.gov.ols.router.api;

import java.util.Set;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.engine.basic.GlobalDistortionField;

public class DefaultsResponse {
	
	RouterConfig config;
	
	public DefaultsResponse(RouterConfig config) {
		this.config = config;
	}
	
	public Set<RouteOption> getEnabledOptions() {
		return RouteOption.fromList(config.getDefaultEnableOptions());
	}
	
	public GlobalDistortionField getGlobalDistortionField() {
		return new GlobalDistortionField(config.getDefaultGlobalDistortionField());
	}
}
