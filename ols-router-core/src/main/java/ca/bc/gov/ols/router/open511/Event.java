/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import ca.bc.gov.ols.router.open511.enums.EventCertainty;
import ca.bc.gov.ols.router.open511.enums.EventSeverity;
import ca.bc.gov.ols.router.open511.enums.EventStatus;
import ca.bc.gov.ols.router.open511.enums.EventSubtype;
import ca.bc.gov.ols.router.open511.enums.EventType;

public class Event {
	private String url;
	private String jurisdiction;
	private String id;
	private EventStatus status;
	private String headline;
	private EventType eventType;
	private EventSeverity severity;
	private Geometry geography;
	private ZonedDateTime created;
	private ZonedDateTime updated;
	private Schedule schedule;
	private ZoneId timezone;
	private String description;
	private List<EventSubtype> eventSubtypes;
	private EventCertainty certainty;
	private List<String> groupedEvents;
	private String detour;
	private List<Road> roads;
	private List<Area> areas;
	private List<String> attachments;
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getJurisdiction() {
		return jurisdiction;
	}
	
	public void setJurisdiction(String jurisdiction) {
		this.jurisdiction = jurisdiction;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public EventStatus getStatus() {
		return status;
	}
	
	public void setStatus(EventStatus status) {
		this.status = status;
	}
	
	public String getHeadline() {
		return headline;
	}
	
	public void setHeadline(String headline) {
		this.headline = headline;
	}
	
	public EventType getEventType() {
		return eventType;
	}
	
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	
	public EventSeverity getSeverity() {
		return severity;
	}
	
	public void setSeverity(EventSeverity severity) {
		this.severity = severity;
	}
	
	public Geometry getGeography() {
		return geography;
	}
	
	public void setGeography(Geometry geography) {
		this.geography = geography;
	}
	
	public ZonedDateTime getCreated() {
		return created;
	}
	
	public void setCreated(ZonedDateTime created) {
		this.created = created;
	}
	
	public ZonedDateTime getUpdated() {
		return updated;
	}
	
	public void setUpdated(ZonedDateTime updated) {
		this.updated = updated;
	}
	
	public Schedule getSchedule() {
		return schedule;
	}
	
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	
	public ZoneId getTimezone() {
		return timezone;
	}
	
	public void setTimezone(ZoneId timezone) {
		this.timezone = timezone;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<EventSubtype> getEventSubtypes() {
		return eventSubtypes;
	}
	
	public void setEventSubtypes(List<EventSubtype> eventSubtypes) {
		this.eventSubtypes = eventSubtypes;
	}
	
	public EventCertainty getCertainty() {
		return certainty;
	}
	
	public void setCertainty(EventCertainty certainty) {
		this.certainty = certainty;
	}
	
	public List<String> getGroupedEvents() {
		return groupedEvents;
	}
	
	public void setGroupedEvents(List<String> groupedEvents) {
		this.groupedEvents = groupedEvents;
	}
	
	public String getDetour() {
		return detour;
	}
	
	public void setDetour(String detour) {
		this.detour = detour;
	}
	
	public List<Road> getRoads() {
		return roads;
	}
	
	public void setRoads(List<Road> roads) {
		this.roads = roads;
	}
	
	public List<Area> getAreas() {
		return areas;
	}
	
	public void setAreas(List<Area> areas) {
		this.areas = areas;
	}
	
	public List<String> getAttachments() {
		return attachments;
	}
	
	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}
	
}
