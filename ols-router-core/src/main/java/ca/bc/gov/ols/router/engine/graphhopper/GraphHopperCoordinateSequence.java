/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.graphhopper;

import com.graphhopper.util.PointList;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

public class GraphHopperCoordinateSequence implements CoordinateSequence {
	
	private PointList points;

	public GraphHopperCoordinateSequence(PointList points) {
		this.points = points;
	}
	
	public GraphHopperCoordinateSequence clone() {
		return new GraphHopperCoordinateSequence(points.clone(false));
	}

	@Override
	public CoordinateSequence copy() {
		return new GraphHopperCoordinateSequence(points.clone(false));
	}


	
	@Override
	public int getDimension() {
		return 2;
	}

	@Override
	public Coordinate getCoordinate(int i) {
		return new Coordinate(points.getLon(i), points.getLat(i));
	}

	@Override
	public Coordinate getCoordinateCopy(int i) {
		return new Coordinate(points.getLon(i), points.getLat(i));
	}

	@Override
	public void getCoordinate(int index, Coordinate coord) {
		coord.x = points.getLon(index);
		coord.y = points.getLat(index);
	}

	@Override
	public double getX(int index) {
		return points.getLon(index);
	}

	@Override
	public double getY(int index) {
		return points.getLat(index);
	}

	@Override
	public double getOrdinate(int index, int ordinateIndex) {
		switch(ordinateIndex) {
		case 0: return points.getLon(index);
		case 1: return points.getLat(index);
		default: return 0;
		}
	}

	@Override
	public int size() {
		return points.getSize();
	}

	@Override
	public void setOrdinate(int index, int ordinateIndex, double value) {
		switch(ordinateIndex) {
		case 0: points.set(index, points.getLat(index), value, 0);
				break;
		case 1: points.set(index, value, points.getLon(index), 0);
				break;
		default:
		}
	}

	@Override
	public Coordinate[] toCoordinateArray() {
		Coordinate[] coords = new Coordinate[points.size()];
		for(int i = 0; i < points.size(); i++) {
			coords[i] = getCoordinate(i);
		}
		return coords;
	}

	@Override
	public Envelope expandEnvelope(Envelope env) {
		for(int i = 0; i < points.size(); i++) {
			env.expandToInclude(points.getLon(i), points.getLat(i));
		}
		return env;
	}

}
