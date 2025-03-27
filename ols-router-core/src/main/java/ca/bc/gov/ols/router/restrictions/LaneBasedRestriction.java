package ca.bc.gov.ols.router.restrictions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;

public class LaneBasedRestriction extends AbstractRestriction {

	public final int[] id;
	public final double[] permitableValue;

	LaneBasedRestriction(final int[] id, final RestrictionSource source, final RestrictionType type, 
			final double[] permitableValue, final Point location, final int locationId) {
		super(source, type, location, locationId);
		this.id = id;
		this.permitableValue = permitableValue;
	}
	
	@Override
	public RestrictionSource getSource() {
		return source;
	}
	
	@Override
	public List<Integer> getIds() {
		return Arrays.stream(id).boxed().collect(Collectors.toList());
	}
	
	/**
	 * As long as the vehicle's (height) value is less than the maximum allowable in at least one lane, travel is not prevented.
	 */
	@Override
	public boolean prevents(RoutingParameters params) {
		Double value = params.getRestrictionValue(type);
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
		Double value = params.getRestrictionValue(type);
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
		double value = params.getRestrictionValue(type);
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
			sb.append("lane ").append(i+1).append(": ").append(permitableValue[i]).append(" ").append(type.unit).append("; ");
		}
		sb.append("(").append(source).append(":").append(Arrays.toString(id)).append(")");
		return sb.toString();
	}

}
