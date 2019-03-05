package ca.bc.gov.ols.router.admin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.DriverException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import au.com.bytecode.opencsv.CSVReader;
import ca.bc.gov.ols.router.admin.data.Configuration;
import ca.bc.gov.ols.router.admin.data.ConfigurationParameter;

@Component
public class AdminApplication {
	final static Logger logger = LoggerFactory.getLogger(
			AdminApplication.class.getCanonicalName());

	private static AdminApplication singleton;
	
	private Cluster cluster;
	private Session session;
	private String keyspace;
 	
	public AdminApplication() {
		logger.info("AdminApplication() constructor called");
		init();
	}

	@Bean
	public static AdminApplication adminApplication() {
		if(singleton == null) {
			singleton = new AdminApplication();
		}
		return singleton;
	}

	@PreDestroy
	public void preDestroy() {
		if(session != null) {
			session.close();
			cluster.close();
		}
	}
	
	public synchronized void init() {
		if(session == null) {
			// initialize the cassandra connection
			Properties props = loadProps();		
			cluster = Cluster.builder().addContactPoint((String)(props.get("config.cassandraContactPoint"))).build();
			session = cluster.connect();
			keyspace = (String)(props.get("config.cassandraKeyspace"));
			
			// to start fresh, uncomment this
			//session.execute("DROP KEYSPACE " + keyspace);
			
			// ensure that the required Cassandra schema has been created 
			validateKeyspace();
		
		}
	}
	
	private void validateKeyspace() {
		KeyspaceMetadata ks = session.getCluster().getMetadata().getKeyspace(keyspace);
		if(ks == null) {
			logger.warn("Cassandra keyspace '" + keyspace + "' does not exist; creating.");
			// for local unreplicated cassandra
			//session.execute("CREATE KEYSPACE " + keyspace + " WITH REPLICATION={ 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
			session.execute("CREATE KEYSPACE " + keyspace + " WITH REPLICATION={ 'class' : 'SimpleStrategy', 'replication_factor' : 3 };");
			createConfigParametersTable();
		} else {
			validateConfigParametersTable();
		}
	}
	
	private void createConfigParametersTable() {
		logger.warn("Creating table " + keyspace + ".BGEO_CONFIGURATION_PARAMETERS");
		session.execute("CREATE TABLE " + keyspace + ".BGEO_CONFIGURATION_PARAMETERS("
				+ "APP_ID TEXT, "
				+ "CONFIG_PARAM_NAME TEXT, "
				+ "CONFIG_PARAM_VALUE TEXT,"
				+ "PRIMARY KEY(APP_ID, CONFIG_PARAM_NAME));");
		populateConfigParametersTable();
	}

	private void populateConfigParametersTable() {
		logger.warn("Populating table " + keyspace + ".BGEO_CONFIGURATION_PARAMETERS");
		InputStream in = AdminApplication.class.getClassLoader().getResourceAsStream("configuration_parameters.csv");
		try (CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(in), Charset.forName("UTF-8")))) {
			String[] header = reader.readNext(); 
			if(header == null) {
				throw new RuntimeException("CSV file empty: bgeo_configuration_parameters.csv");
			}
			int appIdIdx = -1;
			int nameIdx = -1;
			int valueIdx = -1;
			for(int i = 0; i < header.length; i++) {
				switch(header[i].trim().toLowerCase()) {
				case "app_id":
					appIdIdx = i;
					break;
				case "config_param_name": 
					nameIdx = i;
					break;
				case "config_param_value":
					valueIdx = i;
					break;
				}
			}
			String [] row;
			PreparedStatement pStatement = session.prepare("INSERT INTO " 
					+ keyspace + ".BGEO_CONFIGURATION_PARAMETERS "
					+ "(APP_ID, CONFIG_PARAM_NAME, CONFIG_PARAM_VALUE) " 
					+ "VALUES (?, ?, ?) IF NOT EXISTS;");
			while((row = reader.readNext()) != null) {
				session.execute(pStatement.bind(row[appIdIdx], row[nameIdx], row[valueIdx]));
			}
			reader.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void validateConfigParametersTable() {
		logger.info("Validating table " + keyspace + ".BGEO_CONFIGURATION_PARAMETERS");
		KeyspaceMetadata ks = session.getCluster().getMetadata().getKeyspace(keyspace);
		TableMetadata table = ks.getTable("BGEO_CONFIGURATION_PARAMETERS");
		if(table == null) {
			logger.warn("Table " + keyspace + ".BGEO_CONFIGURATION_PARAMETERS does not exist");
			createConfigParametersTable();
		} else {
			// adds any necessary parameters
			populateConfigParametersTable();
		}
		// This is where we would check that the table has the right columns
		// and make any changes required by new versions
	}

	private static Properties loadProps() {
		Properties props = new Properties();
		try {
			props.load(AdminApplication.class.getClassLoader().getResourceAsStream("ols.properties"));
			return props;
		} catch(IOException ioe) {
			logger.warn("Error locating/reading properties file: {}", ioe.getMessage());
		}
		return props;
	}
	
	public Session getSession() {
		return session;
	}

	public String getKeyspace() {
		return keyspace;
	}
	
	public void save(Configuration conf) {
		if(conf.getErrors().isEmpty()) {
			try {
				// fixed thread pool for limiting number of open Async calls
			    ExecutorService executor = MoreExecutors.getExitingExecutorService(
			            (ThreadPoolExecutor)Executors.newFixedThreadPool(5));
				
				// save BGEO_CONFIGURATION_PARAMETERS
			    // We won't truncate because if we are importing an older config it might not have values for new params
			    // Cassandra inserts act as updates if the key part already exists 
				//logger.info("Truncating table " + keyspace + ".BGEO_CONFIGURATION_PARAMETERS");
				//session.execute("TRUNCATE " + keyspace + ".BGEO_CONFIGURATION_PARAMETERS;");
				PreparedStatement pStatement = session.prepare("INSERT INTO " 
						+ keyspace + ".BGEO_CONFIGURATION_PARAMETERS "
						+ "(APP_ID, CONFIG_PARAM_NAME, CONFIG_PARAM_VALUE) " 
						+ "VALUES (?, ?, ?);");
				for(ConfigurationParameter configParam : conf.getConfigParams()) {
					ResultSetFuture future = session.executeAsync(
							pStatement.bind(configParam.getAppId(), configParam.getConfigParamName(), 
									configParam.getConfigParamValue()));
					Futures.addCallback(future, new ImportCallback(conf), executor);
				}	
			} catch(DriverException de) {
				conf.getErrors().add("Cassandra DriverException thrown: " + de.getMessage());
			}
		}
	}

}

class ImportCallback implements FutureCallback<ResultSet>{

	private Configuration conf;
	
	public ImportCallback(Configuration conf) {
		this.conf = conf;
	}
	
    @Override
    public void onSuccess(ResultSet result) {
        //placeholder: put any logging or on success logic here.
    }

    @Override
    public void onFailure(Throwable t) {
    	conf.getErrors().add("Error importing data: exception was: " + t.getMessage());
    }
}
