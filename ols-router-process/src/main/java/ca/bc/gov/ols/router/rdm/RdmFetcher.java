package ca.bc.gov.ols.router.rdm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

public class RdmFetcher {
	private final static Logger logger = LoggerFactory.getLogger(RdmFetcher.class.getCanonicalName());
	
	private static String outputDir = "C:/apps/router/data/";
	//private static final String RDM_API = "https://dev-rdm-public.th.gov.bc.ca/api/view/restrictions_active"; // dev API
	//private static final String RDM_API = "https://tst-rdm-public.th.gov.bc.ca/api/view/restrictions_active"; // test API
	private static final String RDM_API = "https://rdm-public.th.gov.bc.ca/api/view/restrictions_active"; // prod API
	private static final String OUTPUT_FILE = "restrictions_active.json";
	private static final String OUTPUT_GEOJSON_FILE = "restrictions_active.geojson";
	private static final int OFFSET_INCREMENT = 250;
	private static final String[] PROPERTIES = {"RESTRICTION_ID", "RESTRICTION_TYPE", "PERMITABLE_VALUE", "PUBLIC_COMMENT", "TRAVEL_DIRECTION", 
			"GROUP_NAME", "LOCATION_ID", "LOCATION_ROAD_NAME", "NETWORK_SEGMENT_ID", "RESTRICTION_AZIMUTH", "NETWORK_VERSION", "LAST_UPDATE_CONTEXT",
			"FEATURE_NAME", "FEATURE_SOURCE_SYSTEM", "FEATURE_SOURCE_SYSTEM_KEY"};
	
	private int offset = 0;
	
	public static void main(String[] args) throws IOException {
		if(args.length != 1) {
			logger.error("Output directory parameter is required.");
			System.exit(-1);
		}
		String dir = args[0];
		File f = new File(dir);
		if(!f.isDirectory()) {
			logger.error("Invalid data dir: '{}'", dir);
			System.exit(-1);
		}
		outputDir = dir;
		RdmFetcher fetcher = new RdmFetcher();
		fetcher.fetchAll();
	}

	private void fetchAll() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		
		try(JsonWriter out = gson.newJsonWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputDir + OUTPUT_FILE)), "UTF-8")));
				JsonWriter geoJson = gson.newJsonWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputDir + OUTPUT_GEOJSON_FILE)), "UTF-8")))) {
			out.beginArray();
			
			geoJson.beginObject();
			geoJson.name("type").value("FeatureCollection");
			geoJson.name("features");
			geoJson.beginArray();
			
			while(true) {
				JsonElement json = JsonParser.parseReader(fetchNext());
				JsonArray jsonArray = json.getAsJsonArray();
				if(jsonArray.isEmpty()) {
					break;
				}
				for(JsonElement element : jsonArray) {
					gson.toJson(element, out);
					
					JsonObject obj = element.getAsJsonObject();
					geoJson.beginObject();
					geoJson.name("type").value("Feature");
					geoJson.name("geometry");
					gson.toJson(obj.get("GEOMETRY"), geoJson);
					geoJson.name("properties");
					geoJson.beginObject();
					
					for(Entry<String, JsonElement> entry: obj.entrySet()) {
						String name = entry.getKey();
						JsonElement value = entry.getValue();
						if(name.equalsIgnoreCase("GEOMETRY")) continue;
						geoJson.name(name);
						if(value.isJsonPrimitive() || value.isJsonNull()) {
							gson.toJson(value, geoJson);
						} else if(value.isJsonObject()) {
							JsonObject valueObj = value.getAsJsonObject();
							if(valueObj.has("date")) {
								gson.toJson(valueObj.get("date"), geoJson);
							} else {
								geoJson.value("Unknown Object");
							}
						} else {
							geoJson.value("Unexpected Value");
						}
					}
					geoJson.endObject();
					geoJson.endObject();
				}
			}
			out.endArray();
			
			geoJson.endArray();
			geoJson.endObject();
		}
	}
	
	private Reader fetchNext() throws IOException {
		URL url = new URL(RDM_API + "?offset=" + offset);
		System.out.println(url.toString());
		final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		offset += OFFSET_INCREMENT;
		return reader;
	}
		
}
