package ca.bc.gov.ols.router.engine.basic;

import java.util.EnumSet;
import java.util.function.BiFunction;

public enum Attribute {
	isFerry((g,i) -> g.getScheduleLookup().getFerryInfo(i) != null),
	isTruckRoute((g,i) -> g.isTruckRoute(i)),
	locality((g,i) -> g.getLocality(i));

	private BiFunction<BasicGraph,Integer,Object> method;
	
	private Attribute(BiFunction<BasicGraph,Integer,Object> method) {
		this.method = method;
	}
	
	public Object get(BasicGraph graph, Integer edgeId) {
		return method.apply(graph, edgeId);
	}
	
	/**
	 * Takes a string with a comma-separated list of Attribute values 
	 * and returns an EnumSet of the corresponding Attributes.
	 * 
	 * @param attributeList string of comma-separated list of Attribute values
	 * @return the EnumSet of Attributes corresponding to the given string list
	 */
	public static EnumSet<Attribute> fromList(String attributeList) {
		EnumSet<Attribute> attrSet = EnumSet.noneOf(Attribute.class);
		String[] attrs = attributeList.split(",");
		for(String attr : attrs) {
			if(attr != null && !attr.isEmpty()) {
				for(Attribute a : values()) {
					if(a.name().equalsIgnoreCase(attr)) {
						attrSet.add(a);
					}
				}
			}
		}
		return attrSet;
	}
}
