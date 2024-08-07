/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.stereotype.Component;

import com.google.gson.stream.JsonWriter;

import ca.bc.gov.ols.router.api.DefaultsResponse;
import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.data.enums.RouteOption;
import ca.bc.gov.ols.router.data.enums.VehicleType;

@Component
public class JsonDefaultsResponseConverter extends AbstractHttpMessageConverter<DefaultsResponse> {

	protected JsonDefaultsResponseConverter(MediaType mediaType) {
		super(mediaType);
	}

	public JsonDefaultsResponseConverter() {
		super(new MediaType("application", "vnd.geo+json",
				Charset.forName("UTF-8")), MediaType.APPLICATION_JSON);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return DefaultsResponse.class.isAssignableFrom(clazz);
	}

	@Override
	protected DefaultsResponse readInternal(
			Class<? extends DefaultsResponse> clazz,
			HttpInputMessage inputMessage) throws IOException {
		return null;
	}

	@Override
	protected void writeInternal(DefaultsResponse response,
			HttpOutputMessage outputMessage) throws IOException {
		Writer out = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
		RouterConfig config = RouterConfig.getInstance();
		
		@SuppressWarnings("resource")
		JsonWriter jw = new JsonWriter(out);
		jw.beginObject();
		
		jw.name("version");
		jw.value(RouterConfig.VERSION);

		jw.name("defaultTruckRouteMultiplier");
		jw.value(config.getDefaultTruckRouteMultiplier());
		
		jw.name("enableOptions");
		jw.beginArray();
		for(RouteOption ro : response.getEnabledOptions()) {
			jw.value(ro.toString());
		}
		jw.endArray();

		jw.name("globalDistortionField");
		jw.beginObject();
		
		jw.name("CAR");
		JsonConverterHelper.outputMap(jw, response.getGlobalDistortionField(VehicleType.CAR).toMap(VehicleType.CAR)); // globalDistortionField.CAR

		jw.name("TRUCK");
		JsonConverterHelper.outputMap(jw, response.getGlobalDistortionField(VehicleType.TRUCK).toMap(VehicleType.TRUCK)); // globalDistortionField.CAR
		
		jw.endObject(); // globalDistortionField
		
		jw.name("xingCost");
		jw.value(joinArray(config.getDefaultXingCost()));

		jw.name("turnCost");
		jw.value(joinArray(config.getDefaultTurnCost()));
		
		jw.name("simplifyThreshold");
		jw.value(config.getDefaultSimplifyThreshold());
		
		jw.endObject(); //wrapper
		out.flush();
	}
	
	private String joinArray(double[] a) {
		return Arrays.stream(a).boxed().map(n -> Double.toString(n)).collect(Collectors.joining(","));
	}
}
