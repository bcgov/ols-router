/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;

import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.TurnClass;
import ca.bc.gov.ols.router.data.TurnRestriction;
import ca.bc.gov.ols.router.open511.EventResponse;
import ca.bc.gov.ols.router.restrictions.rdm.Restriction;
import ca.bc.gov.ols.rowreader.DateType;
import ca.bc.gov.ols.rowreader.RowReader;

public interface GraphBuilder {

	public abstract void addEdge(StreetSegment seg);

	public default void addTurnRestriction(TurnRestriction cost) {}

	public default void addTurnClass(TurnClass turnClass) {}

	public default void addRestrictions(List<Restriction> parseRestrictions) {}

	public default void addEvents(EventResponse eventResponse) {}

	public default void addTraffic(RowReader trafficReader) {}

	public default void addSchedules(GtfsDaoImpl gtfs, RowReader mappingReader) throws IOException {}

	public default void addTruckNotices(RowReader truckNoticeReader, RowReader truckNoticeMappingReader) {}

	public default void addLocalDistortionField(RowReader localDistortionFieldReader) {}

	public abstract void setDates(Map<DateType, ZonedDateTime> dates);

}