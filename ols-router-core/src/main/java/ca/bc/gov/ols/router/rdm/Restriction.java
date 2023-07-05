package ca.bc.gov.ols.router.rdm;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;

public class Restriction {
	public final int id;
	public final RestrictionSource source;
	public final RestrictionType type;
	// LaneType laneType;
	// LaneSubType laneSubType;
	// public final int laneNumber;
	public final double permitableValue;
	// String publicComment;
	public int segmentId;

	public Restriction(int id, RestrictionSource source, RestrictionType type, double value) {
		this.id = id;
		this.source = source;
		this.type = type;
		this.permitableValue = value;
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
