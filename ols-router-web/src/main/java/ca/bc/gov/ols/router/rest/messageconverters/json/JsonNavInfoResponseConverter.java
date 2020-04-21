/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import com.google.gson.stream.JsonWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.api.GeometryReprojector;
import ca.bc.gov.ols.router.api.NavInfoResponse;
import ca.bc.gov.ols.router.data.vis.VisFeature;
import ca.bc.gov.ols.router.data.vis.VisTurnRestriction;

public class JsonNavInfoResponseConverter extends AbstractHttpMessageConverter<NavInfoResponse> {

	@Autowired
	private Router router;

	protected JsonNavInfoResponseConverter(MediaType mediaType) {
		super(mediaType);
	}

	public JsonNavInfoResponseConverter() {
		super(new MediaType("application", "vnd.geo+json",
				Charset.forName("UTF-8")), MediaType.APPLICATION_JSON);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return NavInfoResponse.class.isAssignableFrom(clazz);
	}

	@Override
	protected NavInfoResponse readInternal(
			Class<? extends NavInfoResponse> clazz,
			HttpInputMessage inputMessage) throws IOException {
		return null;
	}

	@Override
	protected void writeInternal(NavInfoResponse response,
			HttpOutputMessage outputMessage) throws IOException {
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		GeometryReprojector gr = router.getGeometryReprojector();
		
		JsonWriter jw = new JsonWriter(out);
		jw.beginObject();
		jw.name("type").value("FeatureCollection");
		jw.name("features");
		jw.beginArray();
		for(VisFeature mp : response.getMapGeoms()) {
			jw.beginObject();
			jw.name("type").value("Feature");
			jw.name("geometry");
			Geometry g = gr.reproject(mp.getGeometry(), response.getSrsCode());
			JsonConverterHelper.geometry(jw, g);
			jw.name("properties");
			jw.beginObject();
			jw.name("type").value(mp.getType().toString());
			if(mp.getSubType() != null) {
				jw.name("subType").value(mp.getSubType());
			}
			if(mp.getDetail() != null) {
				jw.name("detail").value(mp.getDetail());
			}
			jw.name("angle").value(mp.getAngle());
			if(mp instanceof VisTurnRestriction) {
				VisTurnRestriction tr = (VisTurnRestriction)mp;
				if(tr.getFromFragment() != null) {
					jw.name("fromFragment");
					LineString fromFrag = gr.reproject(tr.getFromFragment(), response.getSrsCode());
					JsonConverterHelper.geometry(jw, fromFrag);
				}
				if(tr.getToFragments() != null) {
					jw.name("toFragments");
					jw.beginArray();
					for(LineString toFragment : tr.getToFragments()) {
						LineString toFrag = gr.reproject(toFragment, response.getSrsCode());
						JsonConverterHelper.geometry(jw, toFrag);
					}
					jw.endArray();
				}
			}
			jw.endObject(); // end properties
			jw.endObject();	// end feature	
		}
		jw.endArray();
		jw.endObject();
		//jw.close();
		out.flush();
	}
}
