/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest.messageconverters;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.api.ApiResponse;
import ca.bc.gov.ols.router.api.GeometryReprojector;
import ca.bc.gov.ols.router.api.IsochroneResponse;
import ca.bc.gov.ols.router.api.RouterDirectionsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceBetweenPairsResponse;
import ca.bc.gov.ols.router.api.RouterDistanceResponse;
import ca.bc.gov.ols.router.api.RouterOptimalRouteResponse;
import ca.bc.gov.ols.router.api.RouterRouteResponse;
import ca.bc.gov.ols.router.config.RouterConfig;

public abstract class ConverterHelper {
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s");

	static final DecimalFormat DEGREE_FORMAT = new DecimalFormat("###.#####");
	static final DecimalFormat METRE_FORMAT = new DecimalFormat("###.##");
	
	protected RouterConfig config;
	protected GeometryReprojector gr;
	protected Writer out;
	
	public ConverterHelper(Router router, Writer out) {
		this.config = router.getConfig();
		this.gr = router.getGeometryReprojector();
		this.out = out;
	}

	protected abstract void writeHeader(ApiResponse response) throws IOException;
	protected abstract void writeFooter(ApiResponse response) throws IOException;
	
	protected abstract void writeFields(RouterDistanceResponse response) throws IOException;
	protected abstract void writeFields(RouterDistanceBetweenPairsResponse response) throws IOException;
	protected abstract void writeFields(RouterRouteResponse response) throws IOException;
	protected abstract void writeFields(RouterDirectionsResponse response) throws IOException;
	protected abstract void writeFields(IsochroneResponse response) throws IOException;

	public void convertResponse(ApiResponse response) 
			throws IOException {
		response.reproject(gr);
		if(response instanceof RouterDirectionsResponse) {
			convertResponse((RouterDirectionsResponse)response);
		} else if(response instanceof RouterDistanceBetweenPairsResponse) {
			convertResponse((RouterDistanceBetweenPairsResponse)response);
		} else if(response instanceof RouterOptimalRouteResponse) {
			convertResponse((RouterOptimalRouteResponse)response);
		} else if(response instanceof RouterRouteResponse) {
			convertResponse((RouterRouteResponse)response);
		} else if(response instanceof RouterDistanceResponse) {
			convertResponse((RouterDistanceResponse)response);
		} else if(response instanceof IsochroneResponse) {
			convertResponse((IsochroneResponse)response);
		} else {
			writeHeader(response);
			writeFooter(response);
		}
	}
	
	public void convertResponse(RouterDistanceResponse response)
			throws IOException {
		writeHeader(response);
		writeFields(response);
		writeFooter(response);
	}

	public void convertResponse(RouterDistanceBetweenPairsResponse response)
			throws IOException {
		writeHeader(response);
		writeFields(response);
		writeFooter(response);
	}

	public void convertResponse(RouterRouteResponse response)
			throws IOException {
		writeHeader(response);
		writeFields(response);
		writeFooter(response);
	}

	public void convertResponse(RouterDirectionsResponse response)
			throws IOException {
		writeHeader(response);
		writeFields(response);
		writeFooter(response);
	}

	public void convertResponse(RouterOptimalRouteResponse response)
			throws IOException {
		writeHeader(response);
		writeFields(response);
		writeFooter(response);
	}

	public void convertResponse(IsochroneResponse response)
			throws IOException {
		writeHeader(response);
		writeFields(response);
		writeFooter(response);
	}

	public static String formatOrdinate(double ord) {
		if(ord <= 180 && ord >= -180) {
			return DEGREE_FORMAT.format(ord);
		}
		return METRE_FORMAT.format(ord);
	}

	static Object formatDate(Object field) {
		if(field instanceof LocalDate) {
			return ((LocalDate)field).format(DATE_FORMATTER);
		} else if(field instanceof LocalDateTime) {
			return ((LocalDateTime)field).format(DATE_TIME_FORMATTER);
		}
		return field;
	}
	
}
