/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511;

public class Pagination {
	private int offset;
	private String nextUrl;
	private String previousUrl;
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public String getNextUrl() {
		return nextUrl;
	}
	
	public void setNextUrl(String nextUrl) {
		this.nextUrl = nextUrl;
	}
	
	public String getPreviousUrl() {
		return previousUrl;
	}
	
	public void setPreviousUrl(String previousUrl) {
		this.previousUrl = previousUrl;
	}
	
}
