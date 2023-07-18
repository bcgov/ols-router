package ca.bc.gov.ols.router.rdm;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;

public class Restriction {
	public final int id;
	public final RestrictionSource source;
	public final RestrictionType type;
	public final double permitableValue;
	public final int segmentId;
	public final Point location;
	public final int laneNumber;
	// LaneType laneType;
	// LaneSubType laneSubType;
	// String publicComment;

	public static RestrictionBuilder builder() {
		return new RestrictionBuilder();
	}
	
	Restriction(RestrictionBuilder rb) {
		this.id = rb.id;
		this.source = rb.source;
		this.type = rb.type;
		this.permitableValue = rb.permitableValue;
		this.segmentId = rb.segmentId;
		this.location = rb.location;
		this.laneNumber = rb.laneNumber;
	}

	public boolean restricts(RoutingParameters params) {
		switch (type) {
		case HORIZONTAL:
			if (params.getWidth() != null && params.getWidth() > permitableValue)
				return true;
			return false;
		case VERTICAL:
			if (params.getHeight() != null && params.getHeight() > permitableValue)
				return true;
			return false;
		case WEIGHT:
			if (params.getWeight() != null && params.getWeight() > permitableValue)
				return true;
			return false;
		case UNKNOWN:
		}
		return false;
	}

	public String toString() {
		if(id > 0) {
			return id + ":" + source + ":" + type + ":" + permitableValue;
		}
		return source + ":" + type + ":" + permitableValue;
	}

}
