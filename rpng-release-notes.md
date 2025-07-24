# Route Planner Release Notes

## Overview
We took what we learned from using the excellent [Graph Hopper](https://github.com/graphhopper/graphhopper) open source library and built a new, open-source route planner from scratch. Future enhancements will focus on time-dependent routing and commercial vehicle routing. We still use the open-source [Jsprit](https://github.com/graphhopper/jsprit) library for route optimization.

You must be a B.C. Government Ministry to use this API.

You will need an apikey header in your requests. To acquire an API key, submit a request using the [API Services Portal](https://api.gov.bc.ca/).
 
To see the new API in action, visit [Location Services in Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html). Feel free to monitor network traffic in your browser to see the new route planner requests.

## API Changes

### Route Planner 2.4.1
1. Route Planner 2.4.1 is backward compatible with Route Planner 2.x.
2. New minRoutingDistance parameter added to specify a minimum distance between start and end points that must exist to calculate a route.
3. Resolved an issue where a route may not be correct if both the start and end points are located on a bi-directional loop segment.
4. Bug fix for distance calculation when an RDM (Restriction Data Manger) height restriction causes a permitted lane warning in the turn-by-turn directions.
5. Bug fix for an issue where restrictions on the first segment of a route were ignored. This bug fix also resolved the issue of unclear messaging in route directions with lane-based restrictions.  
6. Bug fix for route optimization in KML format, leading to a 500 error.

### Route Planner 2.3.2
1. Route Planner 2.3.2 is backward compatible with Route Planner 2.x.
2. Updated to resolve an issue where a *route not found* response was provided when using the parameter *correctSide=true* due to road restrictions or one-way streets.
3. Updated to query the Restriction Data Manager (RDM) more frequently.

### Route Planner 2.3.0
1. Route Planner 2.3.0 is backward compatible with Route Planner 2.x.
2. Support for Restriction Data Manager (RDM) restrictions of type LENGTH
3. All input parameters are included in all output formats
4. New status API for RDM error messages - /status/RDM

### Route Planner 2.2.1
1. Route Planner 2.2.1 is backward compatible with Route Planner 2.x.
2. The BC Route Planner response now includes timestamps indicating when the data was last processed ([*dataProcessingTimestamp*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#dataProcessingTimestamp)) as well as the road network data vintage ([*roadNetworkTimestamp*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#roadNetworkTimestamp)).
3. Use the [*enable*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) parameter to include a list of Transport Line ID's in the BC Route Planner response that are associated with the route.
   * tl - transportation line IDs; disabled by default
5. Use the [*snapDistance*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#snapDistance) parameter to adjust the maximum distance (in metres) away from the road that a point can be located within and still find a route. The default value is 1000.
6. Simplify driving directions using the [*simplifyDirections*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#simplifyDirections) and [*simplifyThreshold*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#simplifyThreshold) parameters. The default values are false and 250 respectively.
7. Enhanced route partitioning to include distance per partition.
8. Use the [*listRestrictions*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#listRestrictions) parameter to include a list of all restriction IDs impacting the direction of travel along a route.
9. Specify the source of road restrictions using the [*restrictionSource*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#restrictionSource) parameter. Options include ITN (Integrated Transportation Network) or RDM (Restriction Data Manager). **Note that the RDM option is only suitable for demos**. The default value is ITN. 
10. Use the [*excludeRestrictions*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#excludeRestrictions) parameter to exclude specific restrictions. Provided as a comma-separated list of restriction IDs.
11. Restriction types can now be passed as parameters using the [*restrictionValues*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#restrictionValues) parameter (**only suitable for demos**).

### Route Planner 2.1.8
1. Route Planner 2.1.8 is backward compatible with Route Planner 2.x.
2. Configuration updated to apply the Global Distortion Field ([gdf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) to cars. To avoid impacting existing routes, friction factors will only be applied to the following road classes to avoid their use where possible (recreation, resource, restricted, service)

### Route Planner 2.1.7
1. Route Planner 2.1.7 is backward compatible with Route Planner 2.x.
2. The [betweenPairs](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#betweenPairs) request was updated to better handle situations where a route is not found. The updated version will return route information for all valid points and a -1 time and distance value as well as a routeFound:false value for failure cases.

### Route Planner 2.1.4
1. Route Planner 2.1.4 is backward compatible with Route Planner 2.x.
2. Support for vehicle-type specific turn restrictions. For example, a road that only commercial vehicles are permitted to use.
3. Support for timed notifications. A timed notification is a notice that has an effective time period. During this period, the notice is displayed on the appropriate leg of the best route in turn-by-turn directions. A notification doesn't affect the computation of best route.

### Route Planner 2.1.3
1. Route Planner 2.1.3 is backward compatible with Route Planner 2.x.
2. Truck resources now have a default value of followTruckRoute=true.

### Route Planner 2.1
1. Route Planner 2.1 is backward compatible with Route Planner 2.x.

2. Enable intersection crossing costs ([xc](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) to more accurately model all combinations of major and minor road crossings.

3. Enable the global distortion field ([gdf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) to allow the router to prefer major roads over minor ones. Global distortion fields ([gdf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) only affect truck routing (e.g. requests that specify the /truck resource).

4. Enable local distortion fields ([ldf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) to steer trucks away from particular roads or truck routes. Local distortion fields ([ldf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) only affect truck routing (e.g., requests that specify the /truck resource). Local distortion fields are defined by road authorities.

5. Use the [*enable*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) parameter to turn on optional features including honouring route start time, turn restrictions ([tr](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)), turn costs ([tc](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)), intersection crossing costs ([xc](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)), global distortion fields ([gdf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) and local distortion fields ([ldf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)), ferry schedules ([sc](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) (**only suitable for demos**), historic traffic congestion ([tf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) (**only suitable for demos**), and road events ([ev](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable)) (**only suitable for demos**).

6. The [*disable*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#disable) parameter is now deprecated. Use the [enable](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) parameter instead.

7. Use the [*partition*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#partition) parameter to partition the best route by truck/non-truck segments, ferry/non-ferry segments, and locality. 
The partitions are returned in a separate partitions parameter; the route output parameter is unchanged. [*Partitions*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#partitions) give your application enough information to treat truck route portions of a route differently than non-truck route portions (e.g., different styles). Same for ferry segments. Locality partitioning gives your application a complete list of municipalities traversed by the best route which is useful in determining appropriate truck bylaws for example.

8. One-way weight restrictions are now supported. This is necessary to prevent heavy trucks from turning onto a road with a steep downward grade.

9. Route directions now include ([notifications](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#notifications) for oversize commercial vehicles (in the Greater Vancouver area))


### Route Planner 2.0
1. Route Planner 2.0 is backward compatible with Route Planner 1.x .

2. Use the new [*departure*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#departure) parameter to specify departure date and time as in 2019-02-28T11:36:00-08:00  

3. Use the new [*correctSide*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#correctSide) parameter to specify if origin and destination should begin and end on the correct side of the street. For example, 1175 Douglas St, Victoria, BC is on the east side of Douglas St. To start or end on this side of the street, set correctSide to True.

4. All new features are grouped into modules that can be turned on or off with each routing request using the new [*disable*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#disable) parameter as follows:
   * [td](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) – time-dependency; disabling this disables sc, tf, and ev modules
   * [tr](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) – turn restrictions; if td is disabled, time-dependent turn restrictions are ignored
   * [tc](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) - turn costs (e.g., left turns take longer than right turns)
   * [sc](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) – ferry schedules; disabled by default; (**only suitable for demos**)
   * [tf](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) – historic traffic congestion; disabled by default; (**only suitable for demos**)
   * [ev](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) – road events; disabled by default; (**only suitable for demos**)

5. You can specify truck dimensions with the [*height*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#height) (OAH in metres), [*width*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#width) (OAW in metres), and [*weight*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#weight) (GVW in kg) parameters. (**only suitable for demos**)</b>

6. Use the [*truckRouteMultiplier*](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#truckRouteMultiplier) parameter to specify how strongly a route should be attracted to truck routes; 9 is good, 100 simulates a black hole, o is a meander that’s fun to watch. Thanks to open data from TransLink, you will find many truck routes in the Greater Vancouver area but nowhere else in the province.

For more information about the API, consult the [Route Planner Developer Guide](https://bcgov.github.io/ols-router/router-developer-guide.html)


## Feature Matrix

Feature                | Enabled<br>by Default| Feature Quality | Data Needed            |Data Quality          
|----------------------|:---------:|------------------|-----------------------|----------------------|
Correct-side routing|No|Good|Road geometry from ITN and address ranges from BC Address Geocoder|Excellent|
Hard road restrictions|Yes|Acceptable; basic minimum height and weight restrictions (including directional weight restrictions) are supported;|road minimum height and weight from ITN|[height](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#height) and [weight](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#weight): Acceptable in Metro Vancouver, poor elsewhere<br>[width](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#width) and [length](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#length): minimal data in ITN. (**only suitable for demos**)
Time-dependent turn-restrictions|No|**Functionally good but demo only due to poor data**|Time-dependent turn restrictions from ITN|Poor. There are many missing time-dependent turn-restrictions in the data.
Time independent turn restrictions|Yes|Supports restrictions on U-Turns,left-turns, right-turns, and straight-throughs.|[Implicit restrictions](https://www.mapbox.com/mapping/mapping-for-navigation/implicit-restrictions/)<br>Explicit restrictions from ITN (e.g., based on observed road signs)|Implicit restrictions: high<br> Explicit restrictions: Good in Metropolitan Vancouver area. Poor everywhere else
Start time|No|Good|All time-dependent data|Variable|
Truck routes|Yes|Good; Only supports a basic truck route, not weight or height corridors, hazardous material routes, or evacuation routes|Truck routes from ITN| Good in Metropolitan Vancouver area, all highways in province are truck routes but no local truck routes outside Metro Vancouver
Turn costs|Yes|Acceptable|Turn cost estimates by<br>turn type (left,right,straight)<br>traffic impactor (yield, stop, light)<br> intersection approach/departure (slowing down,speeding up)|Acceptable, turn-costs have been coarsely-tuned. 
Ferry schedules|No|Good|GTFS files from BC Ferries and Ministry of Transportation|BC Ferries doesn't provide GTFS so we created a static GTFS file.<br><br>Added support for Barnston Island Ferry (MOTI) and you don't need to enable ferry schedules to route over it because it is unscheduled. (**only suitable for demos**)
Historic traffic congestion|No|**Demo data only**|historic traffic data for each road segment in urban areas|Poor; In the absense of real data, simulated traffic peaks were generated for rush hour only.
Scheduled Road Events|No|**Demo data only**; no real-time event monitoring| Scheduled road closure events from static Open511 file loaded on reboot|Good for some events; too much descriptive text, not enough structured time intervals
