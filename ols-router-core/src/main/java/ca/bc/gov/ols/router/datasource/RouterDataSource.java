/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasource;

import java.io.IOException;
import java.io.Reader;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;

import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.TurnRestriction;
import ca.bc.gov.ols.rowreader.RowReader;

/**
 * The RouterDataSource provides access to the segment data. 
 * 
 * @author chodgson
 * 
 */
public interface RouterDataSource {

	StreetSegment getNextSegment() throws IOException;

	String getNameBySegmentId(int segmentId);

	TurnRestriction getNextTurnRestriction() throws IOException;

	RowReader getTurnClassReader() throws IOException;

	Reader getOpen511Reader() throws IOException;

	RowReader getTrafficReader() throws IOException;

	GtfsDaoImpl getGtfs() throws IOException;

	RowReader getGTFSMappingReader() throws IOException;

	RowReader getTruckNoticeReader() throws IOException;

	RowReader getTruckNoticeMappingReader() throws IOException;

	RowReader getLocalDistortionFieldReader() throws IOException;
	
}
