package ca.bc.gov.ols.router.restrictions.rdm;

import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;

public class RestrictionBuilder {
	private final static Logger logger = LoggerFactory.getLogger(RestrictionBuilder.class.getCanonicalName());
	
	int id = -1;
	RestrictionSource source = null;
	RestrictionType type = RestrictionType.UNKNOWN;
	int laneNumber = -1;
	double permitableValue = -1;
	int segmentId = -1;
	double azimuth = -1;
	Point location = null;
	int locationId;

	// LaneType laneType;
	// LaneSubType laneSubType;
	// String publicComment;
	// featureSource
	
	// Call Restriction.builder() to get a RestrictionBuilder
	RestrictionBuilder() {
	}
	
	public RestrictionBuilder id(int id) {
		this.id = id;
		return this;
	}

	public RestrictionBuilder source(RestrictionSource source) {
		this.source = source;
		return this;
	}

	public RestrictionBuilder type(RestrictionType type) {
		this.type = type;
		return this;
	}

	public RestrictionBuilder laneNumber(int laneNumber) {
		this.laneNumber = laneNumber;
		return this;
	}

	public RestrictionBuilder permitableValue(double permitableValue) {
		this.permitableValue = permitableValue;
		return this;
	}

	public RestrictionBuilder segmentId(int segmentId) {
		this.segmentId = segmentId;
		return this;
	}

	public RestrictionBuilder location(Point location) {
		this.location = location;
		return this;
	}

	public RestrictionBuilder locationId(int locationId) {
		this.locationId = locationId;
		return this;
	}

	public RestrictionBuilder azimuth(double azimuth) {
		this.azimuth = azimuth;
		return this;
	}

	public RestrictionBuilder featureSource(String nextString) {
		// TODO add support for this maybe?
		return this;
	}

	public Restriction build() {
		boolean valid = true;
		if(source == null) {
			valid = false;
			logger.warn("Restriction has no source: RestrictionID: {}", id);
		}
		if(permitableValue <= 0) {
			valid = false;
			logger.warn("Restriction permittable Value <= 0: RestrictionID: {}", id);
		}
		if(segmentId == -1) {
			valid = false;
			logger.warn("Restriction with no segmentId: RestrictionID: {}", id);
		}
		if(laneNumber > 0 && type != RestrictionType.VERTICAL) {
			logger.warn("Non-vertical lane-based Restriction: RestrictionId: {}", id);
			laneNumber = 0;
		}
		if(valid) {
			return new Restriction(this);
		}
		return null;
	}

}
