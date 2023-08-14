package ca.bc.gov.ols.router.restrictions;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;

public abstract class AbstractRestriction implements Constraint {

	public final RestrictionSource source;
	public final RestrictionType type;
	public final Point location;
	public final int locationId;

	public AbstractRestriction(RestrictionSource source, RestrictionType type, Point location, int locationId) {
		this.source = source;
		this.type = type;
		this.location = location;
		this.locationId = locationId;
	}

	@Override
	public RestrictionSource getSource() {
		return source;
	}

	@Override
	public RestrictionType getType() {
		return type;
	}

	@Override
	public Point getLocation() {
		return location;
	}

	public int getLocationId() {
		return locationId;
	}

}