package ca.bc.gov.ols.router.restrictions.rdm;

import java.util.List;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.restrictions.AbstractRestriction;

public class Restriction extends AbstractRestriction {
	public final int id;
	public final int laneNumber;
	public final double permitableValue;
	public final int segmentId;
	public final double azimuth;
	// LaneType laneType;
	// LaneSubType laneSubType;
	// String publicComment;

	public static RestrictionBuilder builder() {
		return new RestrictionBuilder();
	}
	
	Restriction(RestrictionBuilder rb) {
		super(rb.source, rb.type, rb.location, rb.locationId);
		this.id = rb.id;
		this.permitableValue = rb.permitableValue;
		this.segmentId = rb.segmentId;
		this.laneNumber = rb.laneNumber;
		this.azimuth = rb.azimuth;
	}
	
	@Override
	public List<Integer> getIds() {
		return List.of(id);
	}

	@Override
	public boolean prevents(RoutingParameters params) {
		Double value = params.getRestrictionValue(type);
		if(value == null) return false;
		if(type.hasValue) {
			if(value <= permitableValue) return false;
		} else {
			if(value == 0) return false;
		}
		return true;
	}

	@Override
	public boolean constrains(RoutingParameters params) {
		return false;
	}
	
	@Override
	public String getVisDescriptor() {
		if(type.hasValue) {
			return "Max " + type.visName + ": " + permitableValue + " " + type.unit + " (" + source + ":" + id + ")";
		}
		return type.visName;
	}

	@Override
	public String toString() {
		return id + ":" + source + ":" + type + ":" + permitableValue;
	}

}
