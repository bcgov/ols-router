package ca.bc.gov.ols.router.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

class AzimuthTest {

	GeometryFactory gf = new GeometryFactory();
	
	@Test
	void testAzimuth() {
		assertEquals(0, Azimuth.azimuth(buildLineString(0.0, 1.0)), 0.001);
		assertEquals(45, Azimuth.azimuth(buildLineString(1.0, 1.0)), 0.001);
		assertEquals(90, Azimuth.azimuth(buildLineString(1.0, 0.0)), 0.001);
		assertEquals(135, Azimuth.azimuth(buildLineString(1.0, -1.0)), 0.001);
		assertEquals(180, Azimuth.azimuth(buildLineString(0.0, -1.0)), 0.001);
		assertEquals(225, Azimuth.azimuth(buildLineString(-1.0, -1.0)), 0.001);
		assertEquals(270, Azimuth.azimuth(buildLineString(-1.0, 0.0)), 0.001);
		assertEquals(315, Azimuth.azimuth(buildLineString(-1.0, 1.0)), 0.001);
	}
	
	@Test
	void testCompareAzimuth() {
		// lineString with Azimuth 0
		assertEquals(10, Azimuth.compareAzimuth(buildLineString(0.0, 1.0), 10.0), 0.001);
		assertEquals(170, Azimuth.compareAzimuth(buildLineString(0.0, 1.0), 190.0), 0.001);
		assertEquals(90, Azimuth.compareAzimuth(buildLineString(0.0, 1.0), 90.0), 0.001);
		assertEquals(90, Azimuth.compareAzimuth(buildLineString(0.0, 1.0), 270.0), 0.001);
		assertEquals(135, Azimuth.compareAzimuth(buildLineString(0.0, 1.0), 225.0), 0.001);
		
		// lineString with Azimuth 45
		assertEquals(180, Azimuth.compareAzimuth(buildLineString(1.0, 1.0), 225.0), 0.001);
		assertEquals(5, Azimuth.compareAzimuth(buildLineString(1.0, 1.0), 40.0), 0.001);
	
		// lineString with Azimuth 225
		assertEquals(0, Azimuth.compareAzimuth(buildLineString(-1.0, -1.0), 225.0), 0.001);
		assertEquals(175, Azimuth.compareAzimuth(buildLineString(-1.0, -1.0), 40.0), 0.001);

		// lineString with Azimuth 315
		assertEquals(25, Azimuth.compareAzimuth(buildLineString(-1.0, 1.0), 340.0), 0.001);
		assertEquals(85, Azimuth.compareAzimuth(buildLineString(-1.0, 1.0), 40.0), 0.001);
}

	private LineString buildLineString(double toX, double toY) {
		return gf.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(toX, toY)});
	}
	
}
