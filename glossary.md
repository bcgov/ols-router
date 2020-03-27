# BC Route Planner
# Glossary of Terms
Term | Definition
----: | -----------
<a name="copyrightNotice">copyrightNotice</a> | Copyright notice applicable to all responses of the BC Route Planner API
<a name="copyrightLicense">copyrightLicense</a> | Copyright license applicable to all responses of the BC Route Planner API
<a name="criteria">criteria</a> | Routing criteria to optimize. One of shortest or fastest. Default is shortest
<a name="directions">directions</a> | Turn-by-turn directions
<a name="disclaimer">disclaimer</a> | Legal disclaimer of the BC Route Planner API
<a name="distance">distance</a> | Route length (in distanceUnit units)
<a name="distanceUnit">distanceUnit</a> | Unit of measure used by distance property. Allowed values are km (kilometres) and mi (miles). Default is km.
<a name="executionTime">executionTime</a> | Request execution time in milliseconds
<a name="enable">enable</a> | Enable optional routing features; specified as a comma-separated list of feature codes which are as follows:<br><br>**td** – time-dependency including honouring route start time and time-dependent turn-restrictions<br><br>**tr** – turn restrictions including no-left, no-right, no-straight-through, no-U-turn; time-dependent turn restrictions only work if td is enabled too<br><br>**tc** - turn costs (e.g., left turns take longer than right turns)<br><br>**xc** - crossing costs (e.g., crossing a major road on a minor road takes longer than the other way around)<br><br>**gdf** - global distortion field; applies friction factors to road segments by ITN road class; this makes major roads more attractive than minor ones when routing trucks<br><br>**ldf** - local distortion field; applies friction factors to specific road segments to make them less attractive to trucks<br><br>**sc** – ferry schedules; only works if td is enabled too; only suitable for demos since it uses dummy data<br><br>**tf** – historic traffic congestion; only works if td is enabled too; only suitable for demos since it uses dummy data<br><br>**ev** – road events; only works if td is enabled too; only suitable for demos since it uses dummy data<br><br>The default value of enable is gdf,ldf,tr,xc,tc
<a name="fromPoints">fromPoints</a> | A list of origin points in geographic coordinates (lon/lat). Commas are used to separate coordinates and points as in the following list of two points: -124.972951,49.715181,-123.139464,49.704015
<a name="maxPairs">maxPairs</a> | Maximum number of (from,to) pairs to return for each point in fromPoints. pairs are selected in nearest to farthest order. For example, given 1 fromPoints, 10 toPoints, and maxPairs=1, betweenPairs will return the pair with the shortest distance or fastest time
<a name="outputFormat">outputFormat</a> | Format of representation. Allowed values are json, html, and kml. Default is json.
<a name="outputSRS">outputSRS</a> | The EPSG code of the spatial reference system used to state the coordination location of a named feature. It is ignored if KML output is specified since KML only supports 4326 (WGS84). Allowed values are:<br>3005: BC Albers<br>4326: WGS 84 (default)<br>26907-26911: NAD83/UTM Zones 7N through 11N<br>32607-32611: WGS84/UTM Zones 7N through 11N<br>26707-26711: NAD27/UTM Zones 7N through 11N
<a name="points">points</a> | A list of origin point, any number of waypoints, and destination point in visiting order. Points are specified as X1,Y1,...Xn,Yn where X and Y are values in the projection specified by the 'outputSRS' parameter. If no outputSRS is given, X is treated as longitude and Y is treated as latitude.<br>Here is an example:<br>-123.707942,48.778691,-123.537850,48.382005<br><br>To make a round trip, just add the start point as in:<br>-123.707942,48.778691,-123.537850,48.382005,-123.707942,48.778691
<a name="privacyStatement">privacyStatement</a> | Privacy statement associated with the BC Route Planner API
<a name="roundTrip">roundTrip</a> | true if a route should end back on its start point; false otherwise
<a name="routeFound">routeFound</a> | true if a route that connects all points has been found; false otherwise
<a name="route">route</a> | route geometry represented as a list of points.
<a name="routeDescription">routeDescription</a> | A short description of the nature of the requested route. This will be echoed in the returned route representation for use in your application. For example:<br>Fastest route from 1002 Johnson St, Victoria to 1105 Royal Ave, New Westminster
<a name="searchTimestamp">searchTimeStamp</a> | date and time request was processed
<a name="toPoints">toPoints</a> | A list of destination points in geographic coordinates (lon/lat). Commas are used to separate coordinates and points as in the following list of two points: -124.972951,49.715181,-123.139464,49.704015
<a name="srsCode">srsCode</a> | The EPSG code of the spatial reference system used to state the coordinate location of all geometry features in HTTP response. Allowed values are:<br>3005: BC Albers<br>4326: WGS 84 (default)<br>26907-26911: NAD83/UTM Zones 7N through 11N<br>32607-32611: WGS84/UTM Zones 7N through 11N<br>26707-26711: NAD27/UTM Zones 7N through 11N
<a name="time">time</a> | Route duration (in seconds)
<a name="timeText">timeText</a> | Route duration in structured English (e.g., 1 hour and 15 minutes)
<a name="toPoints">toPoints</a> | A list of destination points in geographic coordinates (lon/lat). Commas are used to separate coordinates and points as in the following list of two points: -124.972951,49.715181,-123.139464,49.704015
<a name="version">version</a> | Software version of the BC Route Planner API
<a name="visitOrder">visitOrder</a> | Represents the position in the optimal order each input point should appear in. For example, a visitOrder of [0,3,2,4,1] means input point 0 is output point 0, input point 1 is output point 3, input point 2 is output point 2, input point 3 is output point 4, and input point 4 is output point 1
