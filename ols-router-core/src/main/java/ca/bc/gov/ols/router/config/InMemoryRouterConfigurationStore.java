package ca.bc.gov.ols.router.config;

import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import ca.bc.gov.ols.config.ConfigurationParameter;
import ca.bc.gov.ols.config.ConfigurationStore;
import ca.bc.gov.ols.config.FileConfigurationStore;

public class InMemoryRouterConfigurationStore extends FileConfigurationStore {

    public InMemoryRouterConfigurationStore(Properties bootstrapConfig) {
        super();
    }

    @Override
    public Stream<ConfigurationParameter> getConfigParams() {
        ConfigurationParameter cp1 = new ConfigurationParameter("ROUTER", "dataSource.className", "ca.bc.gov.ols.router.datasource.TestDataSource");
        ConfigurationParameter cp2 = new ConfigurationParameter("ROUTER", "baseSrsCode", "3005");
        return Stream.of(cp1, cp2);
    }

    @Override
    public void setConfigParam(ConfigurationParameter param) {
        // not implemented
    }

    @Override
    public void removeConfigParam(ConfigurationParameter param) {
        // not implemented;
    }

    @Override
    public void replaceWith(ConfigurationStore configStore) {
        // not implemented
    }

    protected void writeConfigParams(List<ConfigurationParameter> configParams) {
        // not implemented
    }

    @Override
    public void close() {
        // no-op
    }
}