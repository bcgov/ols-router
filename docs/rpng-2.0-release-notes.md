# Route Planner 2.0 Release Notes

## Overview
Route Planner 2.0 is the first release of Route Planner Next Generation. We took what we learned from using the excellent Graph Hopper open source library and built a new, open-source route planner from scratch. RPNG focusses on time-dependent routing and commercial vehicle routing. We still use the open-source Jsprit library for route optimization.

## API Changes

1. Route Planner 2.0 is backward compatible with Route Planner 1.x .
2. All new features are grouped into modules which can be turned on or off with each routing request.

## Features

### Basic Routing
1. Time-independent turn restrictions
2. Shortest/fastest route
3. Turn-by-turn directions including travel time and cardinal direction on each leg.
4. Visit multiple waypoints in a specific order
5. Find optimal order of multiple waypoints (aka Travelling Salesperson Problem)
6. Find nearest location by road (e.g., find nearest of 160 medical diagnostic facilities to a given point location)
7. U-turns restrictions
8. Implicit turn restrictions (e.g., don't turn right at stop light if there is a right-turn yield lane)
8. Optionally start and end on the correct side of the street

There are few time-independent turn-restrictions in the Integrated Transportation Network so their influence on route accuracy will be minimal.

### Time-dependent routing

1. Time-dependent turn-restrictions
3. Turn costs
4. Road events; suitable for demo purposes only due to lack of data
5. Ferry schedules; suitable for demo purposes only due to lack of data
6. Traffic congestion; suitable for demo purposes only due to lack of data


### Truck routing
1. Turn restrictions based on vehicle dimensions
2. Road restrictions including height, width, length restrictions; suitable for demo purposes only
3. Bridge weight restrictions; suitable for demo purposes only
4. Designated truck routes; suitable for demo purposes only

There are few road and bridge restrictions in the Integrated Transportation Network so their influence on route accuracy will be minimal.

### More routing options
1. Trip departure date/time
2. Vehicle dimensions including overall height, overall width, overall length, and gross vehicle weight
4. Disable advanced routing functions such as time-dependent turn restrictions, turn costs, road events, ferry schedules, and traffic congestion. By default, all advanced routing functions are turned off.

### Location Services in Action application
1. Added layers for traffic impactors, road and turn restrictions, and designated truck routes
2. Simplified method of adding and moving route start, end, and way points. 


## Feature Matrix

Feature                | Disabled<br>by Default| Feature Quality | Data Needed            |Data Quality          
|----------------------|:---------:|------------------|-----------------------|----------------------|
Correct-side routing|No|Good|Road geometry from ITN and address ranges from BC Address Geocoder|Excellent|
Time independent turn restrictions|Yes|Supports No-U-Turns, no very sharp turns but demo only due to poor explicit turn-restrictions in ITN|[Implicit restrictions](https://www.mapbox.com/mapping/mapping-for-navigation/implicit-restrictions/)<br>Explicit restrictions from ITN (e.g., based on observed road signs)|Implicit restrictions: high<br> Explicit restrictions: Poor. The data is often out of date or missing restrictions all together.
Start time|Yes|Good|All time-dependent data|variable|
Time-dependent turn-restrictions|Yes|Functionally good but demo only due to poor data|Time-dependent turn restrictions from ITN|Poor. There are many missing time-dependent turn-restrictions in the data.
Turn costs|Yes|Demo only; not tuned for realism and double penalizing in divided intersections|Turn cost estimates by<br>turn type (left,right,straight)<br>traffic impactor (yield, stop, light)<br> intersection approach/departure (slowing down,speeding up)| acceptable
Designated truck routes|Yes|Demo only; Only supports a basic designated truck route, not weight or height corridors hazardous material routes, or evacuation routes|Designated truck routes from ITN| Poor; only a handful of truck route segments in the province (a few in Ft St John)
Scheduled Road Events|Yes|Demo only; no real-time event monitoring, only full road closure events, not lane closures or info events| Scheduled road closure events from static Open511 file loaded on reboot|Good for some events; too much descriptive text, not enough structured time intervals
Historic traffic congestion|Yes|Demo only|historic traffic data for each road segment in urban areas|Poor; In the absense of real data, simulated traffic peaks were generated for rush hour only.
Hard road restrictions|Yes|Demo only; no support for height/weight restrictions by lane or rig type|road minimum height and weight(GVW) from ITN<br>road width|Height, weight: Unknown; very few values<br>width: no data in ITN, demo data only
Ferry schedules|Yes|Demo only due to lack of data; loads ferry schedules from GTFS files|Suitable for demo only; BC Ferries doesn't provide GTFS so we created GTFS file for winter schedules of two ferry routes

Note: Features can be enabled/disabled on a per route planner request basis.
