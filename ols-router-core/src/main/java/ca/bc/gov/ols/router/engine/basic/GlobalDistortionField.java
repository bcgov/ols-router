package ca.bc.gov.ols.router.engine.basic;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.enums.RoadClass;

public class GlobalDistortionField {
	private static final Logger logger = LoggerFactory.getLogger(GlobalDistortionField.class.getCanonicalName());
	
	private EnumMap<RoadClass, Double> truckRouteField;
	private EnumMap<RoadClass, Double> baseField;
	
	public GlobalDistortionField() {
		init();
	}

	public GlobalDistortionField(String gdfString) {
		init();
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

	private void init() {
		truckRouteField = new EnumMap<RoadClass,Double>(RoadClass.class);
		baseField = new EnumMap<RoadClass,Double>(RoadClass.class);
		for(RoadClass rc : RoadClass.values()) {
			truckRouteField.put(rc, 1.0);
			baseField.put(rc, 1.0);
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

}
