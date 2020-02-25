/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.open511.parser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import ca.bc.gov.ols.router.open511.Area;
import ca.bc.gov.ols.router.open511.Event;
import ca.bc.gov.ols.router.open511.EventResponse;
import ca.bc.gov.ols.router.open511.Meta;
import ca.bc.gov.ols.router.open511.Pagination;
import ca.bc.gov.ols.router.open511.RecurringSchedule;
import ca.bc.gov.ols.router.open511.Restriction;
import ca.bc.gov.ols.router.open511.Road;
import ca.bc.gov.ols.router.open511.Schedule;
import ca.bc.gov.ols.router.open511.ScheduleInterval;
import ca.bc.gov.ols.router.open511.enums.Direction;
import ca.bc.gov.ols.router.open511.enums.EventCertainty;
import ca.bc.gov.ols.router.open511.enums.EventSeverity;
import ca.bc.gov.ols.router.open511.enums.EventStatus;
import ca.bc.gov.ols.router.open511.enums.EventSubtype;
import ca.bc.gov.ols.router.open511.enums.EventType;
import ca.bc.gov.ols.router.open511.enums.ImpactedSystem;
import ca.bc.gov.ols.router.open511.enums.RestrictionType;
import ca.bc.gov.ols.router.open511.enums.RoadState;
import ca.bc.gov.ols.router.open511.enums.ScheduleException;

public class Open511Parser {
	private GeometryFactory gf;	
	
	public static void main(String[] args) throws IOException {
		Open511Parser parser = new Open511Parser(new GeometryFactory(new PrecisionModel(), 4326));
		EventResponse er = parser.parseEventResponse(new FileReader("c:\\apps\\router\\data\\active_events.json"));
		System.out.println(er.toString());
	}

	public Open511Parser(GeometryFactory gf) {
		this.gf = gf;
	}
	
	public EventResponse parseEventResponse(Reader reader) throws IOException {
		JsonReader jr = new JsonReader(reader);
		EventResponse er = new EventResponse();
		jr.beginObject();
		while(jr.hasNext()) {
			String name = jr.nextName();
			switch(name) {
			case "events":
				er.setEvents(parseList(jr, this::parseEvent));
				break;
			case "pagination":
				er.setPagination(parsePagination(jr));
				break;
			case "meta":
				er.setMeta(parseMeta(jr));
				break;
			default:
				jr.skipValue();
			}
		}
		jr.endObject();
		jr.close();
		return er;
	}

	private Event parseEvent(JsonReader jr) {
		Event ev = new Event();
		try {
			jr.beginObject();
			while(jr.hasNext()) {
				String name = jr.nextName();
				switch(name) {
				case "url":
					ev.setUrl(jr.nextString());
					break;
				case "jurisdiction":
					ev.setJurisdiction(jr.nextString());
					break;
				case "id":
					ev.setId(jr.nextString());
					break;
				case "status":
					ev.setStatus(EventStatus.valueOf(jr.nextString()));
					break;
				case "headline":
					ev.setHeadline(jr.nextString());
					break;
				case "event_type":
					ev.setEventType(EventType.valueOf(jr.nextString()));
					break;
				case "severity":
					ev.setSeverity(EventSeverity.valueOf(jr.nextString()));
					break;
				case "geography":
					ev.setGeography(parseGeography(jr));
					break;
				case "created":
					ev.setCreated(ZonedDateTime.parse(jr.nextString()));
					break;
				case "updated":
					ev.setUpdated(ZonedDateTime.parse(jr.nextString()));
					break;
				case "schedule":
					ev.setSchedule(parseSchedule(jr));
					break;
				case "timezone":
					ev.setTimezone(ZoneId.of(jr.nextString()));
					break;
				case "description":
					ev.setDescription(jr.nextString());
					break;
				case "event_subtypes":
					ev.setEventSubtypes(parseList(jr, (jsonReader) -> EventSubtype.valueOf(this.parseString(jsonReader))));
					break;
				case "certainty":
					ev.setCertainty(EventCertainty.valueOf(jr.nextString()));
					break;
				case "grouped_events":
					ev.setGroupedEvents(parseList(jr, this::parseString));
					break;
				case "detour":
					ev.setDetour(jr.nextString());
					break;
				case "roads":
					ev.setRoads(parseList(jr, this::parseRoad));
					break;
				case "areas":
					ev.setAreas(parseList(jr, this::parseArea));
					break;
				case "attachments":
					ev.setAttachments(parseList(jr, this::parseString));
					break;
				default:
					jr.skipValue();
				}
			}
			jr.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return ev;
	}

	private String parseString(JsonReader jr) {
		try {
			return jr.nextString();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private int parseInt(JsonReader jr) {
		try {
			return jr.nextInt();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Area parseArea(JsonReader jr) {
		Area area = new Area();
		try {
			jr.beginObject();
			while(jr.hasNext()) {
				String name = jr.nextName();
				switch(name) {
				case "url":
					area.setUrl(jr.nextString());
					break;
				case "name":
					area.setName(jr.nextString());
					break;
				case "id":
					area.setId(jr.nextString());
					break;
				default:
					jr.skipValue();
				}
			}
			jr.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return area;	
	}

	private <N> List<N> parseList(JsonReader jr, Function<JsonReader,N> func) throws IOException {
		List<N> list = new ArrayList<N>();
		jr.beginArray();
		while(jr.hasNext()) {
			list.add(func.apply(jr));
		}
		jr.endArray();
		return list;		
	}
	
	private Road parseRoad(JsonReader jr) {
		Road road = new Road();
		try {
			jr.beginObject();
			while(jr.hasNext()) {
				String name = jr.nextName();
				switch(name) {
				case "url":
					road.setUrl(jr.nextString());
					break;
				case "name":
					road.setName(jr.nextString());
					break;
				case "direction":
					road.setDirection(Direction.valueOf(jr.nextString()));
					break;
				case "from":
					road.setFrom(jr.nextString());
					break;
				case "lanes_closed":
					road.setLanesClosed(jr.nextInt());
					break;
				case "lanes_open":
					road.setLanesOpen(jr.nextInt());
					break;
				case "state":
					road.setState(RoadState.valueOf(jr.nextString()));
					break;
				case "to":
					road.setTo(jr.nextString());
					break;
				case "impacted_systems":
					road.setImpactedSystems(parseList(jr, (jsonReader) -> ImpactedSystem.valueOf(this.parseString(jsonReader))));
					break;
				case "restrictions":
					road.setRestrictions(parseList(jr, this::parseRestriction));
					break;
				case "+delay":
					road.setDelay(jr.nextInt());
					break;
				default:
					jr.skipValue();
				}
			}
			jr.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return road;
	}

	private Restriction parseRestriction(JsonReader jr) {
		Restriction restriction =  new Restriction();
		try {
			jr.beginObject();
			while(jr.hasNext()) {
				String name = jr.nextName();
				switch(name) {
				case "restriction_type":
					restriction.setRestrictionType(RestrictionType.valueOf(jr.nextString()));
					break;
				case "value":
					restriction.setValue(jr.nextDouble());
					break;
				default:
					jr.skipValue();
				}
			}
			jr.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return restriction;
	}
	
	private Schedule parseSchedule(JsonReader jr) {
		Schedule schedule = new Schedule();
		try {
			jr.beginObject();
			while(jr.hasNext()) {
				String name = jr.nextName().trim();
				switch(name) {
				case "recurring_schedules":
					schedule.setRecurringSchedules(parseList(jr, this::parseRecurringSchedule));
					break;
				case "exceptions":
					schedule.setExceptions(parseList(jr, this::parseException));
					break;
				case "intervals":
					schedule.setIntervals(parseList(jr, this::parseInterval));
					break;
				default:
					jr.skipValue();
				}
			}
			jr.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return schedule;	
	}

	private ScheduleInterval parseInterval(JsonReader jr) {
		try{
			return ScheduleInterval.parse(jr.nextString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ScheduleException parseException(JsonReader jr) {
		try{
			ScheduleException exception = ScheduleException.parse(jr.nextString());
			return exception;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private RecurringSchedule parseRecurringSchedule (JsonReader jr) {
		RecurringSchedule recurringSchedule = new RecurringSchedule();
		try {
			jr.beginObject();
			while(jr.hasNext()) {
				String name = jr.nextName();
				switch(name) {
				case "start_date":
					recurringSchedule.setStartDate(LocalDate.parse(jr.nextString()));
					break;
				case "end_date":
					recurringSchedule.setEndDate(LocalDate.parse(jr.nextString()));
					break;
				case "daily_start_time":
					recurringSchedule.setDailyStartTime(LocalTime.parse(jr.nextString()));
					break;
				case "daily_end_time":
					recurringSchedule.setDailyEndTime(LocalTime.parse(jr.nextString()));
					break;
				case "days":
					recurringSchedule.setDays(EnumSet.copyOf((List<DayOfWeek>)parseList(jr, (jsonReader) -> DayOfWeek.of(this.parseInt(jsonReader)))));
					break;
				default:
					jr.skipValue();
				}
			}
			jr.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return recurringSchedule;
	}
	
	private Geometry parseGeography(JsonReader jr) throws IOException {
		JsonToken tok = jr.peek();
		if(tok.equals(JsonToken.NULL)) {
			jr.nextNull();
			return null;
		}
		Geometry geom = null;
		jr.beginObject();
		String type = null;
		while(jr.hasNext()) {
			double x;
			double y;
			switch(jr.nextName()) {
			case "type":
				type = jr.nextString();
				break;
			case "coordinates":
				if(type == null) {
					throw new RuntimeException("GeoJSON geometry type must come before coordinates.");
				}
				switch(type) {
				case "Point":
					jr.beginArray();
					x = jr.nextDouble();
					y = jr.nextDouble();
					jr.endArray();
					geom = gf.createPoint(new Coordinate(x,y));
					break;
				case "LineString":
					ArrayList<Coordinate> coordBuffer = new ArrayList<Coordinate>();
					jr.beginArray();
					while(jr.hasNext()) {
						jr.beginArray();
						x = jr.nextDouble();
						y = jr.nextDouble();
						coordBuffer.add(new Coordinate(x,y));
						jr.endArray();
					}
					jr.endArray();
					geom = gf.createLineString(coordBuffer.toArray(new Coordinate[coordBuffer.size()]));
					break;
				default:
					throw new RuntimeException("Unsupported GeoJSON geometry type: " + type );
				}
				break;
			default:
				jr.skipValue();
			}
		}
		jr.endObject();
		return geom;
	}
	
	private Pagination parsePagination(JsonReader jr) {
		Pagination pagination = new Pagination();
		try {
			jr.beginObject();
			while(jr.hasNext()) {
				String name = jr.nextName();
				switch(name) {
				case "offset":
					pagination.setOffset(jr.nextInt());
					break;
				case "next_url":
					pagination.setNextUrl(jr.nextString());
					break;
				case "previous_url":
					pagination.setPreviousUrl(jr.nextString());
					break;
				default:
					jr.skipValue();
				}
			}
			jr.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return pagination;	
	}

	private Meta parseMeta(JsonReader jr) {
		Meta meta = new Meta();
		try {
			jr.beginObject();
			while(jr.hasNext()) {
				String name = jr.nextName();
				switch(name) {
				case "version":
					meta.setVersion(jr.nextString());
					break;
				default:
					jr.skipValue();
				}
			}
			jr.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return meta;	
	}
	
}
