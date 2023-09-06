package ca.bc.gov.ols.router.util;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

public class Azimuth {
	
	/*
	 * Calculate an azimuth value in decimal degrees for the given lineString based on only its 
	 * start and end points.
	 * 0 is north, clockwise is positive to 90 east, 180 south, 270 west.
	 * 
	 * @param ls the LineString to calculate the azimuth for 
	 * @returns azimuth value in [0,360]
	 */
	public static double azimuth(LineString ls) {
		Coordinate startCoord = ls.getCoordinateN(0);
		Coordinate endCoord = ls.getCoordinateN(ls.getNumPoints()-1);
		double angleRads = Angle.PI_TIMES_2 - (Angle.angle(startCoord, endCoord) - Angle.PI_OVER_2);
		return Angle.toDegrees(Angle.normalizePositive(angleRads));
	}
	
	public static double compareAzimuth(LineString ls, double azimuth) {
		double segAzimuth = azimuth(ls);
		double diff = Math.abs(segAzimuth - azimuth);
		if(diff > 180.0) {
			diff = 360.0 - diff; 
		}
		return diff;
	}
}
