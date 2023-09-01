package ca.bc.gov.ols.router.restrictions;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;

public class LaneBasedRestriction extends AbstractRestriction {

	public final int[] id;
	public final double[] permitableValue;
	public final double dist;

	LaneBasedRestriction(final int[] id, final RestrictionSource source, final RestrictionType type, final double[] permitableValue, final Point location, final int locationId, double dist) {
		super(source, type, location, locationId);
		this.id = id;
		this.permitableValue = permitableValue;
		this.dist = dist;
	}
	
	@Override
	public RestrictionSource getSource() {
		return source;
	}
	
	/**
	 * As long as the vehicle's (height) value is less than the maximum allowable in at least one lane, travel is not prevented.
	 */
	@Override
	public boolean prevents(RoutingParameters params) {
		Double value = getVehicleValue(params);
		if(value == null) return false;
		for(double pv : permitableValue) {
			if(value <= pv) {
				return false;
			}
		}
		return true;
	}

	/**
	 * If the vehicle's (height) value is higher than allowed in some lane, this restriction constrains travel.
	 */
	@Override
	public boolean constrains(RoutingParameters params) {
		Double value = getVehicleValue(params);
		if(value == null) return false;
		for(double pv : permitableValue) {
			if(value > pv) {
				return true;
			}
		}
		return false;
	}
	
	public boolean[] getSafeLanes(RoutingParameters params) {
		boolean[] safeLanes = new boolean[permitableValue.length];
		double value = getVehicleValue(params);
		for(int i = 0; i < permitableValue.length; i++) {
			if(value <= permitableValue[i]) {
				safeLanes[i] = true;
			} else {
				safeLanes[i] = false;
			}
		}
		return safeLanes;
	}

	@Override
	public String getVisDescriptor() {
		StringBuilder sb = new StringBuilder("Max ");
		sb.append(type.visName).append(": ");
		for(int i = 0; i < permitableValue.length; i++) {
			sb.append("lane ").append(i+1).append(": ").append(permitableValue[i]).append("; ");
		}
		sb.append("(").append(source).append(")");
		return sb.toString();
	}

}
