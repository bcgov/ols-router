package ca.bc.gov.ols.router.api;

import java.util.Set;

import ca.bc.gov.ols.router.RouterConfig;
import ca.bc.gov.ols.router.data.enums.RouteOption;

public class DefaultsResponse {
	
	RouterConfig config;
	
	public DefaultsResponse(RouterConfig config) {
		this.config = config;
	}
	
	public Set<RouteOption> getDisabledOptions() {
		return RouteOption.fromList(config.getDefaultDisableOptions());
	}
}
