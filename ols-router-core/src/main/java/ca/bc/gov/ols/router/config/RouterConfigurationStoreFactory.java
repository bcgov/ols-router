/**
 * Copyright © 2008-2019, Province of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.bc.gov.ols.router.config;

import java.lang.reflect.Constructor;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.config.ConfigurationStore;

public class RouterConfigurationStoreFactory {
	private static final Logger logger = LoggerFactory.getLogger(RouterConfigurationStoreFactory.class);
	
	public static ConfigurationStore getConfigurationStore(Properties bootstrapConfig) {
		logger.info("RouterConfigurationStoreFactory.getConfigurationStore() called");
		
		String configStoreClassName = bootstrapConfig.getProperty("OLS_ROUTER_CONFIGURATION_STORE");
		try {
			Class<?> cl = Class.forName(configStoreClassName);
			Constructor<?> con = cl.getConstructor(new Class[] {Properties.class});
			return (ConfigurationStore)con.newInstance(bootstrapConfig);
		} catch(ReflectiveOperationException roe) {
			throw new RuntimeException("Unable to load ConfigurationStore class: '"	+ configStoreClassName, roe);
		}
	}
}
