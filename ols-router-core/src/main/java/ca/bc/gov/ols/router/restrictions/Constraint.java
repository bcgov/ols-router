package ca.bc.gov.ols.router.restrictions;

import org.locationtech.jts.geom.Point;

import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;

public interface Constraint {

	/**
	 * Tests whether the vehicle described in the params cannot pass through this constraint (restriction) at all. 
	 * @param params The route options and vehicle parameters
	 * @return True if the vehicle described in the params cannot pass through this constraint (restriction) at all
	 */
	boolean prevents(RoutingParameters params);
	
	/**
	 * Tests whether the vehicle described in the params is constrained in some way by this constraint (restriction), eg. must be in certain lane(s), etc. 
	 * @param params The route options and vehicle parameters
	 * @return True if the vehicle described in the params can pass through this constraint (restriction) only with some special care taken
	 */
	boolean constrains(RoutingParameters params);

	Point getLocation();
	
	RestrictionSource getSource();
	
	RestrictionType getType();

	String getVisDescriptor();
}
