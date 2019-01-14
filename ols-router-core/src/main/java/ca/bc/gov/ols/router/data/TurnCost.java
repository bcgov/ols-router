/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;


public class TurnCost {
	private final String idSeq;
	private byte cost = 0;
	private WeeklyTimeRange restriction;
	private String sourceDescription;
	private String customDescription;
	
	public TurnCost(String idSeq, byte cost, WeeklyTimeRange restriction, String sourceDescription, String customDescription) {
		this.idSeq = idSeq;
		this.cost = cost;
		this.restriction = restriction;
		this.sourceDescription = sourceDescription;
		this.customDescription = customDescription;
	}

	public TurnCost(int inSegId, int intId,	int outSegId, byte cost, WeeklyTimeRange restriction, String sourceDescription, String customDescription) {
		this.idSeq = inSegId + "|" + intId + "|" + outSegId;
		this.cost = cost;
		this.restriction = restriction;
		this.sourceDescription = sourceDescription;
		this.customDescription = customDescription;
	}

	public String getIdSeqString() {
		return idSeq;
	}

	public int[] getIdSeq() {
		String[] stringIds = idSeq.split("\\|");
		int[] ids = new int[stringIds.length];
		for(int i = 0; i < stringIds.length; i++) {
			ids[i] = Integer.parseInt(stringIds[i]);
		}
		return ids;
	}
	
	public byte getCost() {
		return cost;
	}

	public void setCost(byte cost) {
		this.cost = cost;
	}

	public WeeklyTimeRange getRestriction() {
		return restriction;
	}

	public void setRestriction(WeeklyTimeRange restriction) {
		this.restriction = restriction;
	}

	public String getSourceDescription() {
		return sourceDescription;
	}

	public void setSourceDescription(String description) {
		this.sourceDescription = description;
	}

	public String getCustomDescription() {
		return customDescription;
	}

	public void setCustomDescription(String customDescription) {
		this.customDescription = customDescription;
	}

}