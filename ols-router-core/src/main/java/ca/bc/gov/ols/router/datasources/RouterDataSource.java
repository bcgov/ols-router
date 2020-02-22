/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.datasources;

import java.io.IOException;
import java.io.Reader;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;

import ca.bc.gov.ols.router.data.StreetSegment;
import ca.bc.gov.ols.router.data.TurnCost;

/**
 * The RouterDataSource provides access to the segment data. 
 * 
 * @author chodgson
 * 
 */
public interface RouterDataSource {

	StreetSegment getNextSegment() throws IOException;

	String getNameBySegmentId(int segmentId);

	TurnCost getNextTurnCost() throws IOException;

	Reader getOpen511Reader() throws IOException;

	RowReader getTrafficReader() throws IOException;

	GtfsDaoImpl getGtfs() throws IOException;

	RowReader getGTFSMappingReader() throws IOException;
	
}
