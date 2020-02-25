package ca.bc.gov.ols.router.data.enums;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum VehicleType {
	CAR, TRUCK;
	
	private final static Logger logger = LoggerFactory.getLogger(VehicleType.class);

	public static EnumSet<VehicleType> fromList(String list) {
		if(list == null || list.isBlank()) return EnumSet.allOf(VehicleType.class);
		EnumSet<VehicleType> set = EnumSet.noneOf(VehicleType.class);
		String[] items = list.split("\\|");
		for(String item : items) {
			if(item != null && !item.isEmpty()) {
				try {
					set.add(VehicleType.valueOf(item.toUpperCase()));
				} catch(IllegalArgumentException iae) {
					logger.warn("Invalid VehicleType: '{}'", item);
				}
			}
		}
		return set;
	}
	
	public static String setToString(Set<VehicleType> vehicleTypes) {
		if(vehicleTypes.equals(EnumSet.allOf(VehicleType.class))) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(VehicleType type : vehicleTypes) {
			sb.append(type);
			sb.append("|");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public static VehicleType convert(String s) {
		try {
			return valueOf(s.toUpperCase());
		} catch(IllegalArgumentException iae) {
			return CAR;
		}
	}
	
}
