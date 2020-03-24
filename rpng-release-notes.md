# Route Planner 2.1 Release Notes

## Overview
Route Planner 2.1 is the second release of Route Planner Next Generation. We took what we learned from using the excellent [Graph Hopper](https://github.com/graphhopper/graphhopper) open source library and built a new, open-source route planner from scratch. RPNG focusses on time-dependent routing and commercial vehicle routing. We still use the open-source [Jsprit](https://github.com/graphhopper/jsprit) library for route optimization.

You must be a provincial government agency or an Integrated Transportation Network partner to use this API in your applications. Note that we currently restrict the http origin of router requests to the gov.bc.ca domain so if you're government business area is outside of that domain, let us know your domain and we will add it to our whitelist. 

You will need an apikey header in your requests. Feel free to use a demo api key available from our rest api console (see link in the guide above).
 
To see the new API in action, visit [Location Services in Action](https://ols-demo.apps.gov.bc.ca/index.html). Feel free to monitor network traffic in your browser to see the new route planner requests.

## API Changes

### Route Planner 2.1
1. Route Planner 2.1 is backward compatible with Route Planner 2.0.

2. Enable intersection crossing costs (see item 5) to more accurately model all combinations of major and minor road crossings.

3. Enable the global distortion field (see item 5) to allow the router to prefer major roads over minor ones. Global distortion fields only affect truck routing (e.g. requests that specify the /truck resource).

4. Enable local distortion fields (see item 5) to allow particular truck routes to be favoured over others nearby. Local distortion fields only affect truck routing (e.g., requests that specify the /truck resource). Local distortion fields are defined by road authorities.

5. Use the *enable* parameter to specify feature options as follows:
   * td – time-dependency; disabling this disables sc, tf, and ev modules
   * tr – turn restrictions; if td is disabled, time-dependent turn restrictions are ignored
   * tc - turn costs (e.g., left turns take longer than right turns)
   * xc - crossing costs(e.g., crossing a major road on a minor road takes longer than the other way around)
   * gdf - global distortion field; applies friction factors to road segments by ITN road class; this makes major roads more attractive than minor ones
   * ldf - local distortion field; applies friction factors to specific road segments to make specific truck routes more attractive than others   
   * sc – ferry schedules; disabled by default; <b>uses dummy data so is only suitable for demos</b>
   * tf – historic traffic congestion; disabled by default; <b>uses dummy data so is only suitable for demos</b>
   * ev – road events; disabled by default; <b>uses dummy data so is only suitable for demos</b>
   
   The default value of enable is gdf,ldf,tr,xc,tc

6. The disable parameter is now deprecated. Use the enable parameter instead.

7. Use the partition parameter to partition the best route by truck/non-truck segments, ferry/non-ferry segments, and locality. 
The partitions are returned in a separate partitions parameter; the route output parameter is unchanged. Partitions give your application enough information to treat truck route portions of a route differently than non-truck route portions (e.g., different styles). Same for ferry segments. Locality partitioning gives your application a complete list of municipalities traversed by the best route which is useful in determining appropriate truck bylaws for example.

8. One-way weight restrictions are now supported. This is necessary to prevent heavy trucks from turning onto a road with a steep downward grade.

9. Route directions now include notifications for oversize truck vehicles (in the Greater Vancouver area)


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

6. Use the *truckRouteMultiplier* parameter to specify how strongly a route should be attracted to designated truck routes; 10 is good, 100 simulates a black hole, o is a meander that’s fun to watch. Thanks to open data from TransLink, you will find many designated truck routes in the Greater Vancouver area but nowhere else in the province.

For more information about the API, consult the [Route Planner Developer Guide](https://bcgov.github.io/ols-router/router-developer-guide.html)


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
