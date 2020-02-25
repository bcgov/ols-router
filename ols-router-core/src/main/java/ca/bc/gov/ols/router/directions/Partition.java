package ca.bc.gov.ols.router.directions;

import java.util.EnumMap;
import java.util.EnumSet;

import ca.bc.gov.ols.router.engine.basic.Attribute;
import ca.bc.gov.ols.router.engine.basic.BasicGraph;

public class Partition {
	private int index;
	private EnumMap<Attribute,Object> values;
	
 	public Partition(int index, EnumSet<Attribute> attributes, BasicGraph graph, int edgeId) {
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
}
