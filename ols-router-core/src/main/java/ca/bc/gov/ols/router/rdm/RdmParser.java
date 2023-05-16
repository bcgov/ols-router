/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rdm;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import com.google.gson.stream.JsonReader;

public class RdmParser {
	private GeometryFactory gf;	
	
	public static void main(String[] args) throws IOException {
		RdmParser parser = new RdmParser(new GeometryFactory(new PrecisionModel(), 4326));
		List<Restriction> restrictions = parser.parseRestrictions(new FileReader("c:\\apps\\router\\data\\restrictions_active.json"));
		System.out.println(restrictions.toString());
	}

	public RdmParser(GeometryFactory gf) {
		this.gf = gf;
	}
	
	public List<Restriction> parseRestrictions(Reader reader) throws IOException {
		JsonReader jr = new JsonReader(reader);
		jr.beginArray();
		List<Restriction> restrictions = new ArrayList<Restriction>();
		while(jr.hasNext()) {
			restrictions.add(parseRestriction(jr));
		}
		jr.close();
		return restrictions;
	}
	
	private Restriction parseRestriction(JsonReader jr) throws IOException {
		Restriction r = new Restriction();
		jr.beginObject();
		while(jr.hasNext()) {
			String name = jr.nextName();
			switch(name) {
			case "RESTRICTION_ID":
				r.restrictionId = jr.nextInt();
				break;
			case "RESTRICTION_TYPE":
				r.restrictionType = jr.nextString();
				break;
			case "PERMITABLE_VALUE":
				r.permitableValue = jr.nextDouble();
				break;
			case "NETWORK_SEGMENT_ID":
				r.networkSegmentId = jr.nextInt();
				break;
			default:
				jr.skipValue();
			}
		}
		jr.endObject();
		return r;
	}

	
}
