/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;

import ca.bc.gov.ols.router.api.GeometryReprojector;

public class GeotoolsGeometryReprojector implements GeometryReprojector {
	
	@Override
	public <T extends Geometry> T reproject(T geom, int toSRSCode) {
		if(geom == null) {
			return null;
		}
		if(geom.getSRID() == toSRSCode) {
			return geom; 
		}
		CoordinateReferenceSystem fromCRS = srsCodeToCRS(geom.getSRID());
		CoordinateReferenceSystem toCRS = srsCodeToCRS(toSRSCode);
		
		try {
			MathTransform transform = CRS.findMathTransform(fromCRS, toCRS);
			if(fromCRS.getCoordinateSystem().getAxis(0).getDirection().absolute()
					.equals(AxisDirection.NORTH)) {
				geom = flipAxes(geom);
			}
			@SuppressWarnings("unchecked")
			T newGeom = (T)JTS.transform(geom, transform);
			if(toCRS.getCoordinateSystem().getAxis(0).getDirection().absolute()
					.equals(AxisDirection.NORTH)) {
				newGeom = flipAxes(newGeom);
			}
			newGeom.setSRID(toSRSCode);
			return newGeom;
		} catch(FactoryException fe) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.", fe);
		} catch(TransformException te) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.", te);
		}
	}
	
	private static CoordinateReferenceSystem srsCodeToCRS(int srsCode) {
		try {
			return CRS.decode("EPSG:" + srsCode);
		} catch(NoSuchAuthorityCodeException e) {
			throw new IllegalArgumentException("Invalid srsCode: \"" + srsCode + "\"");
		} catch(FactoryException e) {
			throw new RuntimeException("Unexpected error in coordinate reprojection.");
		}
	}

	private static <T extends Geometry> T flipAxes(T geom) {
		AffineTransformation transform = new AffineTransformation(0, 1, 0, 1, 0, 0);
		@SuppressWarnings("unchecked")
		T newGeom = (T)transform.transform(geom);
		newGeom.setSRID(geom.getSRID());
		return newGeom;
	}
}
