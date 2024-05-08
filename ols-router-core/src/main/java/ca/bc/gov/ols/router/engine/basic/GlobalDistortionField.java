package ca.bc.gov.ols.router.engine.basic;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

import ca.bc.gov.ols.enums.RoadClass;
import ca.bc.gov.ols.router.data.enums.VehicleType;

public class GlobalDistortionField {
	private static final Logger logger = LoggerFactory.getLogger(GlobalDistortionField.class.getCanonicalName());
	
	private EnumMap<RoadClass, Double> truckRouteField;
	private EnumMap<RoadClass, Double> baseField;
	
	public GlobalDistortionField() {
		init();
	}
	
	public GlobalDistortionField(GlobalDistortionField source) {
		this.truckRouteField = source.truckRouteField.clone();
		this.baseField = source.baseField.clone();
	}

	public GlobalDistortionField(String gdfString) {
		init();
		applyString(gdfString);
	}

	private void init() {
		truckRouteField = new EnumMap<RoadClass,Double>(RoadClass.class);
		baseField = new EnumMap<RoadClass,Double>(RoadClass.class);
		for(RoadClass rc : RoadClass.values()) {
			truckRouteField.put(rc, 1.0);
			baseField.put(rc, 1.0);
		}
	}
	
	public void applyString(String gdfString) {
		if(gdfString == null || gdfString.isEmpty()) return;
		String[] entries = gdfString.split("\\,");
		for(String entry : entries) {
			try {
				String[] pair = entry.split("\\:");
				EnumMap<RoadClass, Double> map = baseField;
				if(pair[0].endsWith(".truck")) {
					map = truckRouteField;
					pair[0] = pair[0].substring(0,pair[0].lastIndexOf('.'));
				}
				RoadClass rc = RoadClass.convert(pair[0]);
				if(RoadClass.UNKNOWN == rc) {
					throw new Exception("Unknown RoadClass: " + pair[0]);
				}
				map.put(rc, Double.parseDouble(pair[1]));
			} catch(Exception e) {
				logger.warn("Invalid Global Distortion entry ignored: {}", entry);
			}
		}		
	}
	
	public double lookup(RoadClass roadClass, boolean isTruckRoute) {
		Double ff;
		if(isTruckRoute) {
			ff = truckRouteField.get(roadClass);
		} else {
			ff = baseField.get(roadClass);
		}
		if(ff == null) {
			return 1;
		}
		return ff;
	}

	public Map<RoadClass, Double> getTruckField() {
		return Collections.unmodifiableMap(truckRouteField);
	}

	public Map<RoadClass, Double> getNonTruckField() {
		return Collections.unmodifiableMap(baseField);
	}

	public Map<String, Double> toMap(VehicleType type) {
		HashMap<String, Double> map = new HashMap<String, Double>();
		for(Entry<RoadClass, Double> entry : baseField.entrySet()) {
			if(entry.getKey().isRouteable()) {
				map.put(entry.getKey().toString(), entry.getValue());
			}
		}
		if(VehicleType.TRUCK == type) {
			for(Entry<RoadClass, Double> entry : truckRouteField.entrySet()) {
				if(entry.getKey().isRouteable()) {
					map.put(entry.getKey().toString() + ".truck", entry.getValue());
				}
			}
		}
		return map;
	}
	
}
