package ca.bc.gov.ols.router.admin;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import ca.bc.gov.ols.config.ConfigurationStore;
import ca.bc.gov.ols.router.RouterFactory;
import ca.bc.gov.ols.router.config.RouterConfigurationStoreFactory;

@SpringBootApplication
@EnableAutoConfiguration(exclude={CassandraAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
public class AdminApplication {

	static final Logger logger = LoggerFactory.getLogger(
			AdminApplication.class.getCanonicalName());

	private static AdminApplication singleton;
	
	private ConfigurationStore configStore;
 	
	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
	   
	public AdminApplication() {
		logger.info("AdminApplication() constructor called");
		configStore = RouterConfigurationStoreFactory.getConfigurationStore(RouterFactory.getBootstrapConfigFromEnvironment());
		singleton = this;
	}

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
