/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;

import ca.bc.gov.ols.router.data.enums.TurnRestrictionType;

public class TurnCost {
	private final int[] ids;
	private byte cost = 0;
	private WeeklyTimeRange restriction;
	private TurnRestrictionType type;
	private String sourceDescription;
	private String customDescription;
	
	public TurnCost(String idSeq, byte cost, WeeklyTimeRange restriction, TurnRestrictionType type, String sourceDescription, String customDescription) {
		String[] stringIds = idSeq.split("\\|");
		ids = new int[stringIds.length];
		for(int i = 0; i < stringIds.length; i++) {
			ids[i] = Integer.parseInt(stringIds[i]);
		}
		this.cost = cost;
		this.restriction = restriction;
		this.setType(type);
		this.sourceDescription = sourceDescription;
		this.customDescription = customDescription;
	}

	public TurnCost(int inSegId, int intId,	int outSegId, byte cost, WeeklyTimeRange restriction, TurnRestrictionType type, String sourceDescription, String customDescription) {
		ids = new int[3];
		ids[0] = inSegId;
		ids[1] = intId;
		ids[2] = outSegId;
		this.cost = cost;
		this.restriction = restriction;
		this.setType(type);
		this.sourceDescription = sourceDescription;
		this.customDescription = customDescription;
	}

	public TurnCost(int[] ids, byte cost, WeeklyTimeRange restriction, TurnRestrictionType type, String sourceDescription, String customDescription) {
		this.ids = ids;
		this.cost = cost;
		this.restriction = restriction;
		this.setType(type);
		this.sourceDescription = sourceDescription;
		this.customDescription = customDescription;
	}

	public String getIdSeqString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(ids[0] + "|" + ids[1] + "|" + ids[2]);
		for(int i = 3; i < ids.length; i++) {
			sb.append("|" + ids[i]);
		}
		return sb.toString();
	}

	public boolean sameTurn(TurnCost other) {
		if(other.ids.length != ids.length) return false;
		for(int i = 0; i < ids.length; i++) {
			if(other.ids[i] != ids[i]) return false;
		}
		return true;
	}
	
	public int[] getIdSeq() {
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