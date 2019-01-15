/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511;

import java.util.List;

import ca.bc.gov.ols.router.open511.enums.Direction;
import ca.bc.gov.ols.router.open511.enums.ImpactedSystem;
import ca.bc.gov.ols.router.open511.enums.RoadState;

public class Road {
	private String name;
	private String url;
	private String from;
	private String to;
	private RoadState state;
	private Direction direction;
	private Integer lanesOpen;
	private Integer lanesClosed;
	private List<ImpactedSystem> impactedSystems;
	private List<Restriction> restrictions;
	private Integer delay;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getFrom() {
		return from;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getTo() {
		return to;
	}
	
	public void setTo(String to) {
		this.to = to;
	}
	
	public RoadState getState() {
		return state;
	}
	
	public void setState(RoadState state) {
		this.state = state;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	public Integer getLanesOpen() {
		return lanesOpen;
	}
	
	public void setLanesOpen(Integer lanesOpen) {
		this.lanesOpen = lanesOpen;
	}
	
	public Integer getLanesClosed() {
		return lanesClosed;
	}
	
	public void setLanesClosed(Integer lanesClosed) {
		this.lanesClosed = lanesClosed;
	}

	public List<ImpactedSystem> getImpactedSystems() {
		return impactedSystems;
	}

	public void setImpactedSystems(List<ImpactedSystem> impactedSystems) {
		this.impactedSystems = impactedSystems;
	}

	public List<Restriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<Restriction> restrictions) {
		this.restrictions = restrictions;
	}

	public Integer getDelay() {
		return delay;
	}

	public void setDelay(Integer delay) {
		this.delay = delay;
	}
	
}
