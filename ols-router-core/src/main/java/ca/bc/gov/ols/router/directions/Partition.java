package ca.bc.gov.ols.router.directions;

import java.util.EnumMap;
import java.util.EnumSet;

import ca.bc.gov.ols.router.engine.basic.Attribute;
import ca.bc.gov.ols.router.engine.basic.BasicGraph;

public class Partition {
	private int index;
	private EnumMap<Attribute,Object> values;
	private double dist = 0;
	
 	public Partition(int index, double dist, EnumSet<Attribute> attributes, BasicGraph graph, int edgeId) {
		this.index = index;
		this.dist = dist;
		values = new EnumMap<Attribute,Object>(Attribute.class);
		for(Attribute attr : attributes) {
			values.put(attr, attr.get(graph, edgeId));
		}
	}

	public int getIndex() {
		return index;
	}
	
	public EnumMap<Attribute,Object> getValues() {
		return values;
	}
	
	public double getDistance() {
		return dist;
	}
}
