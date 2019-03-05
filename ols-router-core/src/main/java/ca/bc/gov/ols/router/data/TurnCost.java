/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;

import ca.bc.gov.ols.router.data.enums.TurnRestrictionType;

public class TurnCost {
	private final String idSeq;
	private byte cost = 0;
	private WeeklyTimeRange restriction;
	private TurnRestrictionType type;
	private String sourceDescription;
	private String customDescription;
	
	public TurnCost(String idSeq, byte cost, WeeklyTimeRange restriction, TurnRestrictionType type, String sourceDescription, String customDescription) {
		this.idSeq = idSeq;
		this.cost = cost;
		this.restriction = restriction;
		this.setType(type);
		this.sourceDescription = sourceDescription;
		this.customDescription = customDescription;
	}

	public TurnCost(int inSegId, int intId,	int outSegId, byte cost, WeeklyTimeRange restriction, TurnRestrictionType type, String sourceDescription, String customDescription) {
		this.idSeq = inSegId + "|" + intId + "|" + outSegId;
		this.cost = cost;
		this.restriction = restriction;
		this.setType(type);
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

	public TurnRestrictionType getType() {
		return type;
	}

	public void setType(TurnRestrictionType type) {
		this.type = type;
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