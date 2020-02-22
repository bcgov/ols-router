/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class LineStringSplitter {
	/**
	 * Splits the lineString at the point closest to the target. If the closest point 
	 * on the linestring is at either end, the corresponding returned linestring will
	 * be an invalid 2-point LineString with the start and end points at the closest 
	 * endpoint of the input linestring.
	 * 
	 * @param lineString The LineString to be split
     * @param target The Point to use to split the lineString
     * @return an array of two lineStrings, respectively representing
     * 		the parts of the input lineString before and after the target point
	 */
	public static LineString[] split(LineString lineString, Point target) {
		Coordinate closestCoord = new DistanceOp(lineString, target).nearestLocations()[0].getCoordinate();
		if(closestCoord.equals(lineString.getStartPoint().getCoordinate())) {
			return new LineString[] {
					lineString.getFactory().createLineString(
							new Coordinate[] {lineString.getStartPoint().getCoordinate(),lineString.getStartPoint().getCoordinate()}), 
					lineString};
		}
		if(closestCoord.equals(lineString.getEndPoint().getCoordinate())) {
			return new LineString[] {lineString,
					lineString.getFactory().createLineString(
							new Coordinate[] {lineString.getEndPoint().getCoordinate(),lineString.getEndPoint().getCoordinate()})};
		}
		LineString[] newLineStrings = splitLineString(lineString, target.getCoordinate());
		return newLineStrings;
	}
	
	private static LineString[] splitLineString(LineString lineString, Coordinate target) {
		LineSegment[] lineSegments = lineSegments(lineString);
		int i = indexOfClosestLineSegment(lineSegments, target);
		Coordinate split = lineSegments[i].closestPoint(target);
		LineSegment[] splitLineSegments = new LineSegment[] {
				new LineSegment(lineSegments[i].p0, (Coordinate) split.clone()),
				new LineSegment((Coordinate) split.clone(), lineSegments[i].p1)
		};
		return new LineString[] {
				lineString.getFactory().createLineString(coordinates(Arrays.asList(lineSegments).subList(0, i), 
						Collections.singletonList(splitLineSegments[0]))),
				lineString.getFactory().createLineString(coordinates(Collections.singletonList(splitLineSegments[1]), 
						Arrays.asList(lineSegments).subList(i+1, lineSegments.length)))
		};
	}
	
	private static int indexOfClosestLineSegment(LineSegment[] lineSegments,
			Coordinate target) {
		int indexOfClosestLineSegment = 0;
		double closestDist = Double.MAX_VALUE;
		for (int i = 0; i < lineSegments.length; i++) {
			double dist = lineSegments[i].distance(target); 
			if (dist < closestDist) {
				indexOfClosestLineSegment = i;
				closestDist = dist;
			}
		}
		return indexOfClosestLineSegment;
	}
	
	private static Coordinate[] coordinates(List<LineSegment> lineSegmentsA, List<LineSegment> lineSegmentsB) {
		int size = lineSegmentsA.size() + lineSegmentsB.size();
		Coordinate[] coordinates = new Coordinate[size + 1];
		int i = 0;
		for(LineSegment seg : (Iterable<LineSegment>)Stream.concat(lineSegmentsA.stream(), lineSegmentsB.stream())::iterator) {
			coordinates[i++] = seg.p0;
		}
		if(lineSegmentsB.size() > 0) {
			coordinates[size] = lineSegmentsB.get(lineSegmentsB.size()-1).p1;
		} else {
			coordinates[size] = lineSegmentsA.get(lineSegmentsA.size()-1).p1;
		}
		return coordinates;
	}
	
	private static LineSegment[] lineSegments(LineString lineString) {
		LineSegment[] lineSegments = new LineSegment[lineString.getNumPoints()-1];
		for (int i = 1; i < lineString.getNumPoints(); i++) {
			lineSegments[i - 1] = new LineSegment(lineString.getCoordinateN(i - 1),
					lineString.getCoordinateN(i));
		}
		return lineSegments;
	}
}