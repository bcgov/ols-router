package ca.bc.gov.ols.router.admin.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ca.bc.gov.ols.router.admin.AdminApplication;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.gson.stream.JsonReader;

public class Configuration {
	private String exportDate;
	private String fileName;
	
	private List<String> errors = Collections.synchronizedList(new ArrayList<String>());
	private List<String> messages = Collections.synchronizedList(new ArrayList<String>());
	
	private List<ConfigurationParameter> configParams = new ArrayList<ConfigurationParameter>();
	private int configParamCount = 0;
	private int dbConfigParamCount = 0;
	private List<ConfigDifference<ConfigurationParameter>> configParamDiffs;

	public Configuration(MultipartFile file) {
		fileName = file.getOriginalFilename();
		try {
			JsonReader jsonReader = new JsonReader(
					new InputStreamReader(file.getInputStream(), Charset.forName("UTF-8")));
			jsonReader.beginObject();
			while(jsonReader.hasNext()) {
				switch(jsonReader.nextName()) {
				case "exportDate":
					exportDate = jsonReader.nextString();
					break;
				case "BGEO_CONFIGURATION_PARAMETERS":
					jsonReader.beginObject();
					while(jsonReader.hasNext()) {
						switch(jsonReader.nextName()) {
							case "rows":
								jsonReader.beginArray();
								while(jsonReader.hasNext()) {
									configParams.add(new ConfigurationParameter(jsonReader, messages));
								}
								jsonReader.endArray();
								break;
							case "rowCount":
								configParamCount = jsonReader.nextInt();
								break;
							default:
								messages.add("Unexpected key/value: " + jsonReader.getPath() 
										+ " = " + jsonReader.nextString());
						}
					}
					jsonReader.endObject();
					break;
				default:
					messages.add("Unexpected key/value: " + jsonReader.getPath() 
							+ " = " + jsonReader.nextString());
				}
			}
			validate();
		} catch(IOException ioe) {
			errors.add("IOException was thrown while reading configuration: " + ioe.toString());
		} catch(IllegalStateException ise) {
			errors.add("Invalid JSON input; error message was: " + ise.toString());
		}
	}

	private void validate() {
		if(configParamCount != configParams.size()) {
			errors.add("BGEO_CONFIGURATION_PARAMETERS count: " 
					+ configParams.size() + " does not match expected count " + configParamCount);			
		}
	}

	// for each table
	// select all rows from DB into sorted array of data objects
	// diff the list from the db against the list from the export file
	public void compare(AdminApplication adminApp) {
		Session session = adminApp.getSession();
		String keyspace = adminApp.getKeyspace();
		ResultSet rs;

		// Configuration Parameters
		List<ConfigurationParameter> dbConfigParams = new ArrayList<ConfigurationParameter>();
		configParamDiffs = new ArrayList<ConfigDifference<ConfigurationParameter>>(); 
		rs = session.execute("SELECT app_id, config_param_name, config_param_value FROM " 
				+ keyspace + ".BGEO_CONFIGURATION_PARAMETERS WHERE app_id = 'ROUTER'");
		for (Row row : rs) {
			dbConfigParams.add(new ConfigurationParameter(row.getString("app_id"),
					row.getString("config_param_name"), row.getString("config_param_value")));
		}
		dbConfigParamCount = dbConfigParams.size();
		diffLists(dbConfigParams, configParams, configParamDiffs);
	}
	
	private <T extends Comparable<T>> void diffLists(List<T> a, List<T> b, List<ConfigDifference<T>> diffs) {
		Collections.sort(a);
		Collections.sort(b);
		int aIndex = 0;
		int bIndex = 0;
		while(aIndex < a.size() && bIndex < b.size()) {
			T nextA = a.get(aIndex);
			T nextB = b.get(bIndex);
			int comp = nextA.compareTo(nextB);
			if(comp == 0) {
				if(!nextA.equals(nextB)) {
					// same key but different value, save diff
					diffs.add(new ConfigDifference<T>(nextA, nextB));
				}
				aIndex++;
				bIndex++;
			} else if(comp < 0) {
				// different keys, nextA comes first, so it is unmatched
				diffs.add(new ConfigDifference<T>(nextA, null));
				aIndex++;
			} else if(comp > 0) {
				diffs.add(new ConfigDifference<T>(null, nextB));
				bIndex++;
			}
		}
		// if there are more items in either list, add them as diffs
		while(aIndex < a.size()) {
			diffs.add(new ConfigDifference<T>(a.get(aIndex), null));
			aIndex++;			
		}
		while(bIndex < b.size()) {
			diffs.add(new ConfigDifference<T>(null, b.get(bIndex)));
			bIndex++;			
		}
	}

	public String getExportDate() {
		return exportDate;
	}
	
	public String getFileName() {
		return fileName;
	}

	public List<String> getErrors() {
		return errors;
	}

	public List<String> getMessages() {
		return messages;
	}

	public List<ConfigurationParameter> getConfigParams() {
		return configParams;
	}

	public int getConfigParamCount() {
		return configParamCount;
	}

	public int getDbConfigParamCount() {
		return dbConfigParamCount;
	}

	public List<ConfigDifference<ConfigurationParameter>> getConfigParamDiffs() {
		return configParamDiffs;
	}

}
