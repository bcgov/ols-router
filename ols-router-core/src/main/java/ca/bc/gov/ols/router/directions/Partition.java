package ca.bc.gov.ols.router.directions;

import java.util.EnumMap;
import java.util.EnumSet;

import ca.bc.gov.ols.router.engine.basic.Attribute;
import ca.bc.gov.ols.router.engine.basic.QueryGraph;

public class Partition {
	private int index;
	private EnumMap<Attribute,Object> values;
	private double distance = 0;
	
 	public Partition(int index, EnumSet<Attribute> attributes, QueryGraph graph, int edgeId) {
		this.index = index;
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
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public void addDistance(double distance) {
		this.distance += distance;
	}
}
