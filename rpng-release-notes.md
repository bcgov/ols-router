# Route Planner Release Notes

## Overview
We took what we learned from using the excellent [Graph Hopper](https://github.com/graphhopper/graphhopper) open source library and built a new, open-source route planner from scratch. Future enhancements will focus on time-dependent routing and commercial vehicle routing. We still use the open-source [Jsprit](https://github.com/graphhopper/jsprit) library for route optimization.

You must be a B.C. Government Ministry to use this API.

You will need an apikey header in your requests. To acquire an API key, submit a request using the [API Services Portal](https://api.gov.bc.ca/).
 
To see the new API in action, visit [Location Services in Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html). Feel free to monitor network traffic in your browser to see the new route planner requests.

## API Changes

### Route Planner 2.2.1
1. Route Planner 2.2.1 is backward compatible with Route Planner 2.x.
2. The BC Route Planner response now includes timestamps indicating when the data was last processed (dataProcessingTimestamp) as well as the road network data vintage (roadNetworkTimestamp).
3. Use the *enable* parameter to include a list of Transport Line ID's in the BC Route Planner response that are associated with the route.
   * tl - transportation line IDs; disabled by default
5. Use the *snapDistance* parameter to adjust the maximum distance (in metres) away from the road that a point can be located within and still find a route. The default value is 1000.
6. Simplify driving directions using the *simplifyDirections* and *simplityThreshold* parameters. The default values are false and 250 respectively.
7. Enhanced route partitioning to include distance per partition.
8. Use the *listRestrictions* parameter to include a list of all restriction IDs impacting the direction of travel along a route.
9. Specify the source of road restrictions using the *restrictionSource* parameter. Options include ITN (Integrated Transportation Network) or RDM (Restriction Data Manager). The default value is ITN.
10. Use the *excludeRestrictions* parameter to exclude specific restrictions. Provided as a comma-separated list of restriction IDs.
11. Restriction types can now be passed as parameters using the *restrictionValues* parameter
12. New restriction types have been added (see [API console](https://openapi.apps.gov.bc.ca/?url=https://raw.githubusercontent.com/bcgov/api-specs/master/router/router.json)).

### Route Planner 2.1.8
1. Route Planner 2.1.8 is backward compatible with Route Planner 2.x.
2. Configuration updated to apply the Global Distortion Field (gdf) to cars. To avoid impacting existing routes, friction factors will only be applied to the following road classes to avoid their use where possible (recreation, resource, restricted, service)

### Route Planner 2.1.7
1. Route Planner 2.1.7 is backward compatible with Route Planner 2.x.
2. The betweenPairs request was updated to better handle situations where a route is not found. The updated version will return route information for all valid points and a -1 time and distance value as well as a routeFound:false value for failure cases.

### Route Planner 2.1.4
1. Route Planner 2.1.4 is backward compatible with Route Planner 2.x.
2. Support for vehicle-type specific turn restrictions. For example, a road that only commercial vehicles are permitted to use.
3. Support for timed notifications. A timed notification is a notice that has an effective time period. During this period, the notice is displayed on the appropriate leg of the best route in turn-by-turn directions. A notification doesn't affect the computation of best route.

### Route Planner 2.1.3
1. Route Planner 2.1.3 is backward compatible with Route Planner 2.x.
2. Truck resources now have default for followTruckRoute set to true.

### Route Planner 2.1
1. Route Planner 2.1 is backward compatible with Route Planner 2.x.

2. Enable intersection crossing costs to more accurately model all combinations of major and minor road crossings.

3. Enable the global distortion field to allow the router to prefer major roads over minor ones. Global distortion fields only affect truck routing (e.g. requests that specify the /truck resource).

4. Enable local distortion fields to steer trucks away from particular roads or truck routes. Local distortion fields only affect truck routing (e.g., requests that specify the /truck resource). Local distortion fields are defined by road authorities.

5. Use the *enable* parameter to turn on optional features including honouring route start time, turn restrictions, turn costs, intersection crossing costs, global and local distortion fields, ferry schedules (**demo use only**), historic traffic congestion (**demo use only**), and road events(**demo use only**). For further details, see https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable

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
   * sc – ferry schedules; disabled by default; **uses dummy data so is only suitable for demos**
   * tf – historic traffic congestion; disabled by default; **uses dummy data so is only suitable for demos**
   * ev – road events; disabled by default; **uses dummy data so is only suitable for demos**

5. You can specify truck dimensions with the *height* (OAH in metres), *width* (OAW in metres), and *weight* (GVW in kg) parameters. **Note that dimension restrictions are only suitable for demos**</b>

6. Use the *truckRouteMultiplier* parameter to specify how strongly a route should be attracted to truck routes; 10 is good, 100 simulates a black hole, o is a meander that’s fun to watch. Thanks to open data from TransLink, you will find many truck routes in the Greater Vancouver area but nowhere else in the province.

For more information about the API, consult the [Route Planner Developer Guide](https://bcgov.github.io/ols-router/router-developer-guide.html)


## Feature Matrix

Feature                | Enabled<br>by Default| Feature Quality | Data Needed            |Data Quality          
|----------------------|:---------:|------------------|-----------------------|----------------------|
Correct-side routing|Yes|Good|Road geometry from ITN and address ranges from BC Address Geocoder|Excellent|
Hard road restrictions|Yes|Acceptable; basic minimum height and weight restrictions (including directional weight restrictions) are supported;|road minimum height and weight from ITN|height and weight: Acceptable in Metro Vancouver, poor elsewhere<br>width,length: no data in ITN, **demo data only**
Time-dependent turn-restrictions|No|**Functionally good but demo only due to poor data**|Time-dependent turn restrictions from ITN|Poor. There are many missing time-dependent turn-restrictions in the data.
Time independent turn restrictions|Yes|Supports restrictions on U-Turns,left-turns, right-turns, and straight-throughs.|[Implicit restrictions](https://www.mapbox.com/mapping/mapping-for-navigation/implicit-restrictions/)<br>Explicit restrictions from ITN (e.g., based on observed road signs)|Implicit restrictions: high<br> Explicit restrictions: Good in Metropolitan Vancouver area. Poor everywhere else
Start time|No|Good|All time-dependent data|Variable|
Truck routes|Yes|Good; Only supports a basic truck route, not weight or height corridors, hazardous material routes, or evacuation routes|Truck routes from ITN| Good in Metropolitan Vancouver area, all highways in province are truck routes but no local truck routes outside Metro Vancouver
Turn costs|Yes|Acceptable|Turn cost estimates by<br>turn type (left,right,straight)<br>traffic impactor (yield, stop, light)<br> intersection approach/departure (slowing down,speeding up)|Acceptable, turn-costs have been coarsely-tuned. 
Ferry schedules|No|Good|GTFS files from BC Ferries and Ministry of Transportation|**Suitable for demo only**; BC Ferries doesn't provide GTFS so we created a GTFS file for winter schedule of Bowen Island Ferry.<br><br>Added support for Barnston Island Ferry (MOTI) and you don't need to enable ferry schedules to route over it because it is unscheduled.
Historic traffic congestion|No|**Demo data only**|historic traffic data for each road segment in urban areas|Poor; In the absense of real data, simulated traffic peaks were generated for rush hour only.
Scheduled Road Events|No|**Demo data only**; no real-time event monitoring, only full road closure events, not lane closures or info events| Scheduled road closure events from static Open511 file loaded on reboot|Good for some events; too much descriptive text, not enough structured time intervals
