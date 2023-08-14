package ca.bc.gov.ols.router.restrictions.rdm;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;
import ca.bc.gov.ols.router.datasource.RouterDataLoader;
import ca.bc.gov.ols.router.engine.GraphBuilder;
import ca.bc.gov.ols.router.restrictions.RestrictionLookupBuilder;
import ca.bc.gov.ols.rowreader.JsonRowReader;

public class RdmParser {
	private final static Logger logger = LoggerFactory.getLogger(RdmParser.class.getCanonicalName());
	
	GeometryFactory gf;
	
	public RdmParser(GeometryFactory gf) {
		this.gf = gf;
	}

	public List<Restriction> parseRestrictions(Reader reader) throws IOException {
		JsonReader jr = new JsonReader(reader);
		ArrayList<Restriction> restrictions = new ArrayList<Restriction>();
		int count = 0;
		jr.beginArray();
		while(jr.hasNext()) {
			Restriction r = parseRestriction(jr);
			count++;
			if(r != null) {
				restrictions.add(r);
			}
		}
		jr.close();
		logger.info("Read {} RDM restrictions from file.", count);
		return restrictions;
	}
	
	private Restriction parseRestriction(JsonReader jr) throws IOException {
		jr.beginObject();
		RestrictionBuilder rb = Restriction.builder();
		while(jr.hasNext()) {			
			String name = jr.nextName();
			switch(name) {
			case "RESTRICTION_ID":
				rb.id(jr.nextInt());
				break;
			case "RESTRICTION_TYPE":
				rb.type(RestrictionType.convert(jr.nextString()));
				break;
			case "PERMITABLE_VALUE":
				rb.permitableValue(jr.nextDouble());
				break;
			case "RESTRICTION_AZIMUTH":
				if(jr.peek() == JsonToken.NUMBER) {
					rb.azimuth(jr.nextDouble());
				} else {
					jr.skipValue();
				}
				break;
			case "NETWORK_SEGMENT_ID":
				rb.segmentId(jr.nextInt());
				break;
			case "LANE_NUMBER":
				if(jr.peek() == JsonToken.NUMBER) {
					rb.laneNumber(jr.nextInt());
				} else {
					jr.skipValue();
				}
				break;
			case "FEATURE_SOURCE_SYSTEM":
				JsonToken next = jr.peek();
				if(next == JsonToken.STRING) {
					rb.featureSource(jr.nextString());
				} else {
					jr.skipValue();
				}
				break;
			case "LOCATION_ID":
				rb.locationId(jr.nextInt());
				break;
			case "GEOMETRY":
				rb.location((Point)JsonRowReader.parseJsonGeometry(jr, gf));
				break;
			default:
				jr.skipValue();
			}
		}
		jr.endObject();
		rb.source(RestrictionSource.RDM);
		return rb.build();
	}
	
}
