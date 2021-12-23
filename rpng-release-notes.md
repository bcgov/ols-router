# Route Planner 2.1 Release Notes

## Overview
Route Planner 2.1 is the second release of Route Planner Next Generation. We took what we learned from using the excellent [Graph Hopper](https://github.com/graphhopper/graphhopper) open source library and built a new, open-source route planner from scratch. RPNG focusses on time-dependent routing and commercial vehicle routing. We still use the open-source [Jsprit](https://github.com/graphhopper/jsprit) library for route optimization.

You must be a provincial government agency or an Integrated Transportation Network partner to use this API in your applications. Note that we currently restrict the http origin of router requests to the gov.bc.ca domain so if your government business area is outside of that domain, let us know your domain and we will add it to our whitelist. 

You will need an apikey header in your requests. Feel free to use a demo api key available from our rest api console (see link in the guide above).
 
To see the new API in action, visit [Location Services in Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html). Feel free to monitor network traffic in your browser to see the new route planner requests.

## API Changes

### Route Planner 2.1.4
1. Route Planner 2.1.4 is backward compatible with Route Planner 2.x.
2. Support for vehicle-type specific turn restrictions. For example, a road that only commercial vehicles are permitted to use.
3. Support for timed notifications. A timed notification is a notice that has an effective time period. During this period, the notice should be displayed on the appropriate leg of the best route in turn-by-turn directions. A notification doesn't affect the computation of best route.

### Route Planner 2.1.3
1. Route Planner 2.1.3 is backward compatible with Route Planner 2.x.
2. Truck resources now have default for followTruckRoute set to true.

### Route Planner 2.1
1. Route Planner 2.1 is backward compatible with Route Planner 2.x.

2. Enable intersection crossing costs to more accurately model all combinations of major and minor road crossings.

3. Enable the global distortion field to allow the router to prefer major roads over minor ones. Global distortion fields only affect truck routing (e.g. requests that specify the /truck resource).

4. Enable local distortion fields to steer trucks away from particular roads or truck routes. Local distortion fields only affect truck routing (e.g., requests that specify the /truck resource). Local distortion fields are defined by road authorities.

5. Use the *enable* parameter to turn on optional features including honouring route start time, turn restrictions, turn costs, intersection crossing costs, global and local distortion fields, ferry schedules (demo use only), historic traffic congestion (demo use only), and road events(demo use only). For further details, see https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable

6. The disable parameter is now deprecated. Use the enable parameter instead.

7. Use the partition parameter to partition the best route by truck/non-truck segments, ferry/non-ferry segments, and locality. 
The partitions are returned in a separate partitions parameter; the route output parameter is unchanged. Partitions give your application enough information to treat truck route portions of a route differently than non-truck route portions (e.g., different styles). Same for ferry segments. Locality partitioning gives your application a complete list of municipalities traversed by the best route which is useful in determining appropriate truck bylaws for example.

8. One-way weight restrictions are now supported. This is necessary to prevent heavy trucks from turning onto a road with a steep downward grade.

9. Route directions now include notifications for oversize commercial vehicles (in the Greater Vancouver area)


### Route Planner 2.0
1. Route Planner 2.0 is backward compatible with Route Planner 1.x .

2. Use the new *departure* parameter to specify departure date and time as in 2019-02-28T11:36:00-08:00  

3. Use the new *correctSide* parameter to specify if origin and destination should begin and end on the correct side of the street. For example, 1175 Douglas St, Victoria, BC is on the east side of Douglas St. To start or end on this side of the street, set correctSide to True.

4. All new features are grouped into modules that can be turned on or off with each routing request using the new *disable* parameter as follows:
   * td – time-dependency; disabling this disables sc, tf, and ev modules
   * tr – turn restrictions; if td is disabled, time-dependent turn restrictions are ignored
   * tc - turn costs (e.g., left turns take longer than right turns)
   * sc – ferry schedules; disabled by default; <b>uses dummy data so is only suitable for demos</b>
   * tf – historic traffic congestion; disabled by default; <b>uses dummy data so is only suitable for demos</b>
   * ev – road events; disabled by default; <b>uses dummy data so is only suitable for demos</b>

5. You can specify truck dimensions with the *height* (OAH in metres), *width* (OAW in metres), and *weight* (GVW in kg) parameters. Note that truck routing <b>uses dummy road height/weight data so is only suitable for demos</b>

6. Use the *truckRouteMultiplier* parameter to specify how strongly a route should be attracted to truck routes; 10 is good, 100 simulates a black hole, o is a meander that’s fun to watch. Thanks to open data from TransLink, you will find many truck routes in the Greater Vancouver area but nowhere else in the province.

For more information about the API, consult the [Route Planner Developer Guide](https://bcgov.github.io/ols-router/router-developer-guide.html)


## Feature Matrix

Feature                | Enabled<br>by Default| Feature Quality | Data Needed            |Data Quality          
|----------------------|:---------:|------------------|-----------------------|----------------------|
Correct-side routing|Yes|Good|Road geometry from ITN and address ranges from BC Address Geocoder|Excellent|
Time independent turn restrictions|Yes|Supports restrictions on U-Turns,left-turns, right-turns, and straight-throughs.|[Implicit restrictions](https://www.mapbox.com/mapping/mapping-for-navigation/implicit-restrictions/)<br>Explicit restrictions from ITN (e.g., based on observed road signs)|Implicit restrictions: high<br> Explicit restrictions: Good in Metropolitan Vancouver area. Poor everywhere else
Start time|No|Good|All time-dependent data|Variable|
Time-dependent turn-restrictions|No|Functionally good but demo only due to poor data|Time-dependent turn restrictions from ITN|Poor. There are many missing time-dependent turn-restrictions in the data.
Turn costs|Yes|Acceptable|Turn cost estimates by<br>turn type (left,right,straight)<br>traffic impactor (yield, stop, light)<br> intersection approach/departure (slowing down,speeding up)|Acceptable, turn-costs have been coarsely-tuned. 
Truck routes|Yes|Good; Only supports a basic truck route, not weight or height corridors, hazardous material routes, or evacuation routes|Truck routes from ITN| Good in Metropolitan Vancouver area, all highways in province are truck routes but no local truck routes outside Metro Vancouver
Scheduled Road Events|No|Demo only; no real-time event monitoring, only full road closure events, not lane closures or info events| Scheduled road closure events from static Open511 file loaded on reboot|Good for some events; too much descriptive text, not enough structured time intervals
Historic traffic congestion|No|Demo only|historic traffic data for each road segment in urban areas|Poor; In the absense of real data, simulated traffic peaks were generated for rush hour only.
Hard road restrictions|Yes|Acceptable; basic minimum height and weight restrictions (including directional weight restrictions) are supported; no support for height/weight restrictions by lane or rig type|road minimum height and weight(GVW) from ITN<br>road width|height and weight: Acceptable in Metro Vancouver, poor elsewhere<br>width,length: no data in ITN, demo data only
Ferry schedules|No|Good|GTFS files from BC Ferries and Ministry of Transportation|Suitable for demo only; BC Ferries doesn't provide GTFS so we created a GTFS file for winter schedule of Bowen Island Ferry.<br><br>Added support for Barnston Island Ferry (MOTI) and you don't need to enable ferry schedules to route over it because it is unscheduled.
