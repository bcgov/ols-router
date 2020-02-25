package ca.bc.gov.ols.router.admin;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import ca.bc.gov.ols.config.ConfigurationStore;
import ca.bc.gov.ols.router.RouterFactory;
import ca.bc.gov.ols.router.config.RouterConfigurationStoreFactory;

@Component
public class AdminApplication {

	static final Logger logger = LoggerFactory.getLogger(
			AdminApplication.class.getCanonicalName());

	private static AdminApplication singleton;
	
	private ConfigurationStore configStore;
 	
	public AdminApplication() {
		logger.info("AdminApplication() constructor called");
		configStore = RouterConfigurationStoreFactory.getConfigurationStore(RouterFactory.getBootstrapConfigFromEnvironment());
	}

	@Bean
	public static AdminApplication adminApplication() {
		if(singleton == null) {
			singleton = new AdminApplication();
		}
		return singleton;
	}
	
	public ConfigurationStore getConfigStore() {
		return configStore;
	}
		
	@PreDestroy
    public void preDestroy() {
        configStore.close();
    }
	
}
