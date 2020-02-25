/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.data;

import java.util.Set;

import ca.bc.gov.ols.router.data.enums.TurnRestrictionType;
import ca.bc.gov.ols.router.data.enums.VehicleType;

public class TurnRestriction {
	private final int[] ids;
	private WeeklyTimeRange restriction;
	private TurnRestrictionType type;
	private Set<VehicleType> vehicleTypes;
	private String sourceDescription;
	private String customDescription;
	
	public TurnRestriction(String idSeq, WeeklyTimeRange restriction, TurnRestrictionType type, Set<VehicleType> vehicleTypes, String sourceDescription, String customDescription) {
		String[] stringIds = idSeq.split("\\|");
		ids = new int[stringIds.length];
		for(int i = 0; i < stringIds.length; i++) {
			ids[i] = Integer.parseInt(stringIds[i]);
		}
		this.restriction = restriction;
		this.type = type;
		this.vehicleTypes = vehicleTypes;
		this.sourceDescription = sourceDescription;
		this.customDescription = customDescription;
	}

	public TurnRestriction(int inSegId, int intId,	int outSegId, WeeklyTimeRange restriction, TurnRestrictionType type, Set<VehicleType> vehicleTypes, String sourceDescription, String customDescription) {
		ids = new int[3];
		ids[0] = inSegId;
		ids[1] = intId;
		ids[2] = outSegId;
		this.restriction = restriction;
		this.type = type;
		this.vehicleTypes = vehicleTypes;
		this.sourceDescription = sourceDescription;
		this.customDescription = customDescription;
	}

	public TurnRestriction(int[] ids, WeeklyTimeRange restriction, TurnRestrictionType type, Set<VehicleType> vehicleTypes, String sourceDescription, String customDescription) {
		this.ids = ids;
		this.restriction = restriction;
		this.type = type;
		this.vehicleTypes = vehicleTypes;
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

	public boolean sameTurn(TurnRestriction other) {
		if(other.ids.length != ids.length) return false;
		for(int i = 0; i < ids.length; i++) {
			if(other.ids[i] != ids[i]) return false;
		}
		return true;
	}
	
	public int[] getIdSeq() {
		return ids;
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

	public Set<VehicleType> getVehicleTypes() {
		return vehicleTypes;
	}

	public void setVehicleTypes(Set<VehicleType> vehicleTypes) {
		this.vehicleTypes = vehicleTypes;
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