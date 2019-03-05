package ca.bc.gov.ols.router.admin.data;

import java.io.IOException;
import java.util.List;

import com.google.gson.stream.JsonReader;

public class ConfigurationParameter implements Comparable<ConfigurationParameter> {

	private String appId;
	private String configParamName;
	private String configParamValue;
	
	public ConfigurationParameter(String appId, String configParamName, String configParamValue) {
		this.appId = appId;
		this.configParamName = configParamName;
		this.configParamValue = configParamValue;
	}
	
	public ConfigurationParameter(JsonReader jsonReader, List<String> messages) 
			throws IOException {
		jsonReader.beginObject();
		while(jsonReader.hasNext()) {
			switch(jsonReader.nextName()) {
			case "app_id":
				appId = jsonReader.nextString();
				break;
			case "config_param_name":
				configParamName = jsonReader.nextString();
				break;
			case "config_param_value":
				configParamValue = jsonReader.nextString();
				break;
			default:
				messages.add("Unexpected key/value: " + jsonReader.getPath() 
						+ " = " + jsonReader.nextString());
			}
		}
		jsonReader.endObject();
	}

	public String getAppId() {
		return appId;
	}

	public String getConfigParamName() {
		return configParamName;
	}

	public String getConfigParamValue() {
		return configParamValue;
	}

	/**
	 * Compares the key part only, not the value.
	 */
	@Override
	public int compareTo(ConfigurationParameter other) {
		int comp = appId.compareTo(other.appId);
		if(comp == 0) {
			comp = configParamName.compareTo(other.configParamName);
		}
		return comp;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof ConfigurationParameter) {
			ConfigurationParameter o = (ConfigurationParameter)other;
			if(appId.equals(o.appId) 
					&& configParamName.equals(o.configParamName)
					&& configParamValue.equals(o.configParamValue)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (appId + configParamName + configParamValue).hashCode();
	}

}
