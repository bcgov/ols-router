package ca.bc.gov.ols.router.engine.basic;

import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.restrictions.Constraint;
import ca.bc.gov.ols.util.MapList;

class SplitEdge extends Edge {
	Edge baseEdge;
	LineString lineString;
	MapList<RestrictionSource,Constraint> restrictions; 
	
	public SplitEdge(int edgeId, Edge baseEdge, int otherEdgeId, int fromNodeId, int toNodeId, 
			LineString lineString, MapList<RestrictionSource,Constraint> restrictions) {
		super(edgeId, fromNodeId, toNodeId, baseEdge.data, baseEdge.reversed);
		this.baseEdge = baseEdge;
		this.otherEdgeId = otherEdgeId;
		this.lineString = lineString;
		this.restrictions = restrictions;
	}
	
	// TODO Maybe move all the property accessors from the graph/querygraph to the edge/splitedge
	// might be faster than using the graph
}