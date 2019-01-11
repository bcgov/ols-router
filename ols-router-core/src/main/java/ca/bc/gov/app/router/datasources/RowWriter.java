/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.datasources;

import java.util.Map;

public interface RowWriter {

	public abstract void close();

	public abstract void writeRow(Map<String, Object> row);

}