# BC Route Planner
# Developer Guide
This guide is aimed at developers who would like to incorporate the BC Route Planner into their applications, websites and scripts.
<br>
## Introduction
The BC Route Planner REST API lets you integrate routing between locations in BC into your own applications. This document expands on aspects of the REST API that are covered in the [API Specification](https://raw.githubusercontent.com/bcgov/api-specs/master/router/router.json). You can test and explore the API in the [API Console](https://openapi.apps.gov.bc.ca/?url=https://raw.githubusercontent.com/bcgov/api-specs/master/router/router.json). For a list of the latest changes to the Route Planner API, see [Route Planner Release Notes](https://bcgov.github.io/ols-router/rpng-release-notes.html)
<br>

Your application can store router results or display them on any web map. The BC Route Planner API supports GET and POST requests. POST should be used when you have many waypoints to visit.

## Technical Overview

Access to the BC Route Planner API is mediated by the Data Systems and Services branch [API Services Portal](https://api.gov.bc.ca/).

Source data for the BC Route Planner comes from a variety of sources outlined in data flow #1 and #3 of the [data pipeline](https://github.com/bcgov/ols-geocoder/blob/gh-pages/address-data-pipeline.md). Data is updated on a monthly basis. The BC Route Planner loads this data from files into in-memory data structures at startup. A small configuration file of global parameters is also loaded at startup from a key-value Datastore.  

The BC Route Planner is written in Java and uses the jSprit open source libraries. The [Location Services in Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html) web application, which demonstrates the features of the BC Route Planner, is written in JavaScript and uses jQuery and Leaflet libraries and plugins.

## Latest updates to the Route Planner API
For a list of the latest updates to the BC Route Planner API, see the [release notes](https://github.com/bcgov/ols-router/blob/gh-pages/rpng-release-notes.md).

## API Key
Use of the BC Route Planner REST API is currently restricted to use by BC government applications. If you are working on a government application that needs routing, please submit a request for access via the [API Services Portal](https://api.gov.bc.ca/)

Every route planner request needs an apikey header that contains your api key as follows:
```
apikey: <myapikey>
```	
Do not put the apikey in the request URL because it will expose your api key.


## Distance Resource
The distance resource represents the length and duration of the shortest or fastest route between given points. Here are some examples:

1. Length of shortest route in km and json between Duncan and Metchosin. Also includes an explicit request for a BC Albers output spatial reference system (e.g., 3005). By default, the output SRS is Geographics WGS 84 (e.g., 4326)<br>https://router.api.gov.bc.ca/distance.json?routeDescription=shortest%20distance%20in%20km%20and%20json&points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&criteria=shortest&outputSRS=3005<br>
   
2. Length of shortest route in km and kml between Duncan and Metchosin<br>https://router.api.gov.bc.ca/distance.kml?routeDescription=shortest%20distance%20in%20km%20and%20kml&points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&criteria=shortest<br>

3. Length of fastest route in miles and html between Duncan and Metchosin<br>https://router.api.gov.bc.ca/distance.html?routeDescription=fastest%20distance%20in%20km%20and%20html&points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&criteria=fastest&distanceUnit=mi<br>

4. Length and time of shortest route in km between all pairs of fromPoints and toPoints.<br>
https://router.api.gov.bc.ca/distance/betweenPairs.json?routeDescription=betweenPairs%20test%20case&fromPoints=-123.70794%2C48.77869%2C-123.53785%2C48.38200&toPoints=-124.972951%2C49.715181%2C-123.139464%2C49.704015&criteria=shortest<br>

5. Length and time of shortest route in km between pairs fromPoints and toPoints. In this example, we use the maxPairs parameter to control the maximum number of pairs to return for each toPoint. Pairs are ordered by distance/time from fromPoint<br>
https://router.api.gov.bc.ca/distance/betweenPairs.json?routeDescription=betweenPairs%20test%20case&fromPoints=-123.70794%2C48.77869%2C-123.53785%2C48.38200&toPoints=-124.972951%2C49.715181%2C-123.139464%2C49.704015&criteria=shortest&maxPairs=1<br>

**The betweenPairs request can also be submitted to follow a truck route by changing '/distance' to '/truck/distance'**



### HTTP Response
The distance resource will return the following representation:


Attribute Name |	Type
---------------------: | --- |
[routeDescription](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeDescription) | String
[searchTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#searchTimestamp) | Datetime
[executionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[version](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#version) | String 
[disclaimer](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#disclaimer) | String
[privacyStatement](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#privacyStatement) | String
[copyrightNotice](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightNotice) | String
[copyrightLicense](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightLicense) | String
[srsCode](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#srsCode) | Integer
[criteria](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#criteria) | String
[enable](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) | String
[distanceUnit](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distanceUnit) | String
[dataProcessingTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#dataProcessingTimestamp) | String
[roadNetworkTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#roadNetworkTimestamp) | String
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | list of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String


Here is a sample request for the distance of fastest route in km and in json:

https://router.api.gov.bc.ca/truck/distance.json?routeDescription=fastest%20distance%20in%20km%20and%20json&points=-123.707942%2C48.778691%2C-123.537850%2C48.382005

and here is the json response:

```json
{
   "routeDescription": "fastest distance in km and json",
   "searchTimestamp": "2024-01-17T23:54:17.171470456",
   "executionTime": 128,
   "version": "2.2.1-RELEASE",
   "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
   "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
   "copyrightNotice": "Copyright 2024 Province of British Columbia",
   "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
   "srsCode": 4326,
   "criteria": "fastest",
   "enable": "gdf,ldf,tc,tr,xc",
   "distanceUnit": "km",
   "dataProcessingTimestamp": "2023-12-18T23:05:54Z",
   "roadNetworkTimestamp": "2023-09-29T21:28:38Z",
   "points": [
      [
         -123.70794,
         48.77869
      ],
      [
         -123.53785,
         48.38201
      ]
   ],
   "routeFound": true,
   "distance": 68.046,
   "time": 3707.0444450034292,
   "timeText": "1 hour 1 minute"
}
```


## Route Resource
The route resource represents the shortest or fastest route between given points and the length and duration of that route. Here are some examples:

1. Shortest route in km and json between Duncan and Metchosin. Also includes an explicit request for a BC Albers output spatial reference system (e.g., 3005). By default the output SRS is Geographics WGS 84 (e.g., 4326<br>https://router.api.gov.bc.ca/route.json?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=3005&criteria=shortest<br>
   
2. Shortest route in km and kml between Duncan and Metchosin<br>https://router.api.gov.bc.ca/route.kml?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&criteria=shortest<br>

3. Fastest route in miles and html between Duncan and Metchosin<br>https://router.api.gov.bc.ca/route.html?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&distanceUnit=mi<br>

4. Fastest route from 976 Meares St, Victoria to 1200 Douglas St, Victoria on the correct side of the street<br>
   https://router.api.gov.bc.ca/route.json?points=-123.3575846%2C48.4233118%2C-123.3651354%2C48.4255742&correctSide=true<br>

5. Fastest route around a bridge for an overheight truck following truck routes<br>
https://router.api.gov.bc.ca/truck/route.json?points=-123.392803%2C48.4330137%2C-123.3940682%2C48.4360118&height=5.1&followTruckRoute=true<br>

6. Fastest route around a bridge for an overweight truck following truck routes<br>
https://router.api.gov.bc.ca/truck/route.json?points=-116.80488%2C49.69928%2C-116.8053633591626%2C49.6953321774235&weight=30001&followTruckRoute=true<br>
   
7. Fastest route for a truck following truck routes in Vancouver with partitioning of best route by truck route, ferry, and locality<br>
https://router.api.gov.bc.ca/truck/route.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality<br>

8. Fastest route in km and json between Williams Lake and a mine. In this case, we are trying to navigate to a location that is not found on the road network. By default, a route cannot be found to a point that is beyond 1000 metres from the road network. Using the snapDistance parameter we can override this default.<br>
https://router.api.gov.bc.ca/route.json?points=-122.14%2C52.1288889%2C-121.61941%2C52.54039&snapDistance=1200&apikey=JKM6YZujMf93wbQQVxAHM7XIWAWeaFX4<br>
   


### HTTP response
The route resource will return the following representation:

Attribute Name |	Type
---------------------: | --- |
[routeDescription](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeDescription) | String
[searchTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#searchTimestamp) | Datetime
[executionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[version](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#version) | String 
[disclaimer](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#disclaimer) | String
[privacyStatement](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#privacyStatement) | String
[copyrightNotice](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightNotice) | String
[copyrightLicense](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightLicense) | String
[srsCode](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#srsCode) | Integer
[criteria](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#criteria) | String
[enable](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) | String
[distanceUnit](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distanceUnit) | String
[dataProcessingTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#dataProcessingTimestamp) | String
[roadNetworkTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#roadNetworkTimestamp) | String
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | list of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String
[partition](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#partition) | String
[partitions](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#partitions) | String
[route](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#route) | List of Point


Here is a request for fastest route in vancouver with partitions in json:

https://router.api.gov.bc.ca/truck/route.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality


and here's the json response:

```json
{
   "routeDescription": null,
   "searchTimestamp": "2024-01-18T00:02:12.905033425",
   "executionTime": 1,
   "version": "2.2.1-RELEASE",
   "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
   "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
   "copyrightNotice": "Copyright 2024 Province of British Columbia",
   "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
   "srsCode": 4326,
   "criteria": "fastest",
   "enable": "gdf,ldf,tc,tr,xc",
   "distanceUnit": "km",
   "dataProcessingTimestamp": "2023-12-18T23:05:54Z",
   "roadNetworkTimestamp": "2023-09-29T21:28:38Z",
   "points": [
      [
         -123.11389,
         49.26111
      ],
      [
         -123.11166,
         49.26551
      ]
   ],
   "routeFound": true,
   "distance": 0.874,
   "time": 131.71439858979718,
   "timeText": "2 minutes 12 seconds",
   "partition": "isFerry,isTruckRoute,locality,ownership",
   "partitions": [
      {
         "index": 0,
         "distance": 0.231,
         "isFerry": false,
         "isTruckRoute": false,
         "locality": "Vancouver",
         "ownership": null
      },
      {
         "index": 3,
         "distance": 0.422,
         "isFerry": false,
         "isTruckRoute": true,
         "locality": "Vancouver",
         "ownership": null
      },
      {
         "index": 9,
         "distance": 0.221,
         "isFerry": false,
         "isTruckRoute": false,
         "locality": "Vancouver",
         "ownership": null
      }
   ],
   "route": [
      [
         -123.11297,
         49.2611
      ],
      [
         -123.11297,
         49.26129
      ],
      [
         -123.11291,
         49.26221
      ],
      [
         -123.11289,
         49.26318
      ],
      [
         -123.11478,
         49.2632
      ],
      [
         -123.11476,
         49.26404
      ],
      [
         -123.11474,
         49.26493
      ],
      [
         -123.11473,
         49.26538
      ],
      [
         -123.11467,
         49.26576
      ],
      [
         -123.11279,
         49.26573
      ],
      [
         -123.11165,
         49.26569
      ]
   ]
}
```
 

##  Directions Resource
The directions resource represents the turn-by-turn directions, shortest or fastest route between given points and the length and duration of that route. Here are some examples:

1. Directions and shortest route in km and json between Duncan and Metchosin. Also includes an explicit request for an output spatial reference system of BC Albers (e.g., 3005). By default, the output SRS is Geographics WGS 84 (e.g., 4326)<br>https://router.api.gov.bc.ca/directions.json?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=3005&criteria=shortest<br>
   
2. Directions and shortest route in km and kml between Duncan and Metchosin<br>https://router.api.gov.bc.ca/directions.kml?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&criteria=shortest<br>

3. Directions and fastest route in miles and html between Duncan and Metchosin<br>https://router.api.gov.bc.ca/route.html?points=-123.707942%2C48.778691%2C-123.537850%2C48.38200&distanceUnit=mi<br>

4. Directions and fastest route from 976 Meares St, Victoria to 1200 Douglas St, Victoria on the correct side of the street<br>
   https://router.api.gov.bc.ca/directions.json?points=-123.3575846%2C48.4233118%2C-123.3651354%2C48.4255742&followTruckRoute=true&correctSide=true<br>

5. Directions and fastest route around a bridge for an overheight truck<br>
https://router.api.gov.bc.ca/truck/directions.json?points=-123.392803%2C48.4330137%2C-123.3940682%2C48.4360118&followTruckRoute=true&height=5.1<br>

6. Directions and fastest route around a bridge for an overweight truck<br>
https://router.api.gov.bc.ca/truck/directions.json?points=-116.80488%2C49.69928%2C-116.8053633591626%2C49.6953321774235&followTruckRoute=true&weight=30001<br>
   
7. Directions and fastest route for a truck following truck routes in Vancouver with partitioning of best route by truck route, ferry, and locality<br>
https://router.api.gov.bc.ca/truck/directions.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality<br>

8. Directions and fastest route in km and json between Williams Lake and a mine. In this case, we are trying to navigate to a location that is not found on the road network. By default, a route cannot be found to a point that is beyond 1000 metres from the road network. Using the snapDistance parameter we can override this default.<br>
https://router.api.gov.bc.ca/directions.json?points=-122.14%2C52.1288889%2C-121.61941%2C52.54039&snapDistance=1200&apikey=JKM6YZujMf93wbQQVxAHM7XIWAWeaFX4<br>


### HTTP response
The directions resource will return the following representation:

Attribute Name |	Type
---------------------: | --- |
[routeDescription](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeDescription) | String
[searchTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#searchTimestamp) | Datetime
[executionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[version](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#version) | String 
[disclaimer](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#disclaimer) | String
[privacyStatement](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#privacyStatement) | String
[copyrightNotice](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightNotice) | String
[copyrightLicense](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightLicense) | String
[srsCode](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#srsCode) | Integer
[criteria](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#criteria) | String
[enable](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) | String
[distanceUnit](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distanceUnit) | String
[dataProcessingTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#dataProcessingTimestamp) | String
[roadNetworkTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#roadNetworkTimestamp) | String
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | List of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String
[partition](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#partition) | String
[partitions](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#partitions) | String
[route](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#route) | List of Point
[notifications](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#notifications) | String
[directions](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#directions) | String


Here is a sample request for a route in json that includes multiple partitions:

https://router.api.gov.bc.ca/truck/directions.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality,ownership


and here's the json response:

```json
{
   "routeDescription": null,
   "searchTimestamp": "2024-01-18T00:07:14.093209488",
   "executionTime": 1,
   "version": "2.2.1-RELEASE",
   "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
   "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
   "copyrightNotice": "Copyright 2024 Province of British Columbia",
   "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
   "srsCode": 4326,
   "criteria": "fastest",
   "enable": "gdf,ldf,tc,tr,xc",
   "distanceUnit": "km",
   "dataProcessingTimestamp": "2023-12-18T23:05:54Z",
   "roadNetworkTimestamp": "2023-09-29T21:28:38Z",
   "points": [
      [
         -123.11389,
         49.26111
      ],
      [
         -123.11166,
         49.26551
      ]
   ],
   "routeFound": true,
   "distance": 0.874,
   "time": 131.71439858979718,
   "timeText": "2 minutes 12 seconds",
   "partition": "isFerry,isTruckRoute,locality,ownership",
   "partitions": [
      {
         "index": 0,
         "distance": 0.231,
         "isFerry": false,
         "isTruckRoute": false,
         "locality": "Vancouver",
         "ownership": null
      },
      {
         "index": 3,
         "distance": 0.422,
         "isFerry": false,
         "isTruckRoute": true,
         "locality": "Vancouver",
         "ownership": null
      },
      {
         "index": 9,
         "distance": 0.221,
         "isFerry": false,
         "isTruckRoute": false,
         "locality": "Vancouver",
         "ownership": null
      }
   ],
   "route": [
      [
         -123.11297,
         49.2611
      ],
      [
         -123.11297,
         49.26129
      ],
      [
         -123.11291,
         49.26221
      ],
      [
         -123.11289,
         49.26318
      ],
      [
         -123.11478,
         49.2632
      ],
      [
         -123.11476,
         49.26404
      ],
      [
         -123.11474,
         49.26493
      ],
      [
         -123.11473,
         49.26538
      ],
      [
         -123.11467,
         49.26576
      ],
      [
         -123.11279,
         49.26573
      ],
      [
         -123.11165,
         49.26569
      ]
   ],
   "notifications": [],
   "directions": [
      {
         "type": "START",
         "name": "Yukon St",
         "distance": 0.231,
         "time": 30,
         "heading": "NORTH",
         "text": "Head north on Yukon St for 250 m (30 seconds)",
         "point": [
            -123.11297,
            49.2611
         ]
      },
      {
         "type": "TURN_LEFT",
         "name": "W Broadway",
         "distance": 0.138,
         "time": 29,
         "text": "Turn left onto W Broadway for 150 m (29 seconds)",
         "point": [
            -123.11289,
            49.26318
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "Cambie St",
         "distance": 0.285,
         "time": 43,
         "text": "Turn right onto Cambie St for 300 m (43 seconds)",
         "point": [
            -123.11478,
            49.2632
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "W 6th Ave",
         "distance": 0.221,
         "time": 29,
         "text": "Turn right onto W 6th Ave for 200 m (29 seconds)",
         "point": [
            -123.11467,
            49.26576
         ]
      },
      {
         "type": "FINISH",
         "text": "Finish!",
         "point": [
            -123.11165,
            49.26569
         ]
      }
   ]
}
```


Here's a sample request for directions and a route in Vancouver partitioned by truck/non-truck route, in json, and known to have notifications:

https://router.api.gov.bc.ca/truck/directions.json?points=-123.1008354,49.273378%2C-123.12481,49.28455&followTruckRoute=true&partition=isTruckRoute

and here's the json response:

```json
{
   "routeDescription": null,
   "searchTimestamp": "2024-01-18T00:11:30.638192471",
   "executionTime": 2,
   "version": "2.2.1-RELEASE",
   "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
   "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
   "copyrightNotice": "Copyright 2024 Province of British Columbia",
   "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
   "srsCode": 4326,
   "criteria": "fastest",
   "enable": "gdf,ldf,tc,tr,xc",
   "distanceUnit": "km",
   "dataProcessingTimestamp": "2023-12-18T23:05:54Z",
   "roadNetworkTimestamp": "2023-09-29T21:28:38Z",
   "points": [
      [
         -123.10084,
         49.27338
      ],
      [
         -123.12481,
         49.28455
      ]
   ],
   "routeFound": true,
   "distance": 2.96,
   "time": 346.54662096533104,
   "timeText": "5 minutes 47 seconds",
   "partition": "isTruckRoute",
   "partitions": [
      {
         "index": 0,
         "distance": 2.96,
         "isTruckRoute": true
      }
   ],
   "route": [
      [
         -123.10096,
         49.27302
      ],
      [
         -123.10118,
         49.27305
      ],
      [
         -123.10164,
         49.27305
      ],
      [
         -123.10158,
         49.27382
      ],
      [
         -123.10167,
         49.27435
      ],
      [
         -123.10183,
         49.27491
      ],
      [
         -123.10186,
         49.27499
      ],
      [
         -123.10204,
         49.27577
      ],
      [
         -123.10212,
         49.27608
      ],
      [
         -123.1023,
         49.27661
      ],
      [
         -123.10254,
         49.27692
      ],
      [
         -123.10268,
         49.27703
      ],
      [
         -123.10284,
         49.27716
      ],
      [
         -123.10315,
         49.27738
      ],
      [
         -123.10325,
         49.27743
      ],
      [
         -123.10342,
         49.27751
      ],
      [
         -123.1037,
         49.27763
      ],
      [
         -123.10428,
         49.27775
      ],
      [
         -123.10616,
         49.27805
      ],
      [
         -123.10683,
         49.27826
      ],
      [
         -123.10764,
         49.27865
      ],
      [
         -123.10786,
         49.27871
      ],
      [
         -123.10808,
         49.27875
      ],
      [
         -123.10835,
         49.27874
      ],
      [
         -123.1087,
         49.27866
      ],
      [
         -123.11037,
         49.27789
      ],
      [
         -123.1108,
         49.27782
      ],
      [
         -123.11175,
         49.27787
      ],
      [
         -123.11215,
         49.27781
      ],
      [
         -123.11264,
         49.27763
      ],
      [
         -123.11326,
         49.27735
      ],
      [
         -123.11363,
         49.2771
      ],
      [
         -123.11381,
         49.27689
      ],
      [
         -123.11396,
         49.27644
      ],
      [
         -123.11405,
         49.27634
      ],
      [
         -123.11424,
         49.27614
      ],
      [
         -123.11475,
         49.27578
      ],
      [
         -123.11516,
         49.27623
      ],
      [
         -123.11558,
         49.27654
      ],
      [
         -123.11601,
         49.27681
      ],
      [
         -123.11659,
         49.27717
      ],
      [
         -123.11749,
         49.27777
      ],
      [
         -123.11798,
         49.27811
      ],
      [
         -123.11845,
         49.27844
      ],
      [
         -123.11898,
         49.27875
      ],
      [
         -123.11948,
         49.27905
      ],
      [
         -123.11992,
         49.27933
      ],
      [
         -123.12041,
         49.27965
      ],
      [
         -123.12087,
         49.27996
      ],
      [
         -123.12139,
         49.28033
      ],
      [
         -123.12191,
         49.28065
      ],
      [
         -123.12244,
         49.28099
      ],
      [
         -123.12337,
         49.28157
      ],
      [
         -123.12176,
         49.28264
      ],
      [
         -123.12223,
         49.28293
      ],
      [
         -123.1228,
         49.28328
      ],
      [
         -123.12479,
         49.28456
      ]
   ],
   "notifications": [],
   "directions": [
      {
         "type": "START",
         "name": "Terminal Ave",
         "distance": 0.049,
         "time": 11,
         "heading": "WEST",
         "text": "Head west on Terminal Ave for 50 m (11 seconds)",
         "point": [
            -123.10096,
            49.27302
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "Quebec St",
         "distance": 0.4,
         "time": 41,
         "text": "Turn right onto Quebec St for 400 m (41 seconds)",
         "point": [
            -123.10164,
            49.27305
         ]
      },
      {
         "type": "CONTINUE",
         "name": "Expo Blvd",
         "distance": 1.135,
         "time": 102,
         "text": "Continue onto Expo Blvd for 1.1 km (1 minute 42 seconds)",
         "point": [
            -123.1023,
            49.27661
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "Smithe St",
         "distance": 0.902,
         "time": 131,
         "text": "Turn right onto Smithe St for 900 m (2 minutes 11 seconds)",
         "point": [
            -123.11475,
            49.27578
         ],
         "notifications": [
            {
               "type": "TruckRestriction",
               "message": "Vehicles over 15.25 meters in length may only use this road between 7:00 am and 6:00 pm, Monday to Sunday"
            }
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "Hornby St",
         "distance": 0.167,
         "time": 24,
         "text": "Turn right onto Hornby St for 150 m (24 seconds)",
         "point": [
            -123.12337,
            49.28157
         ],
         "notifications": [
            {
               "type": "TruckRestriction",
               "message": "Vehicles over 15.25 meters in length may only use this road between 7:00 am and 6:00 pm, Monday to Sunday"
            }
         ]
      },
      {
         "type": "TURN_LEFT",
         "name": "Robson St",
         "distance": 0.307,
         "time": 39,
         "text": "Turn left onto Robson St for 300 m (39 seconds)",
         "point": [
            -123.12176,
            49.28264
         ],
         "notifications": [
            {
               "type": "TruckRestriction",
               "message": "Vehicles over 15.25 meters in length may only use this road between 7:00 am and 6:00 pm, Monday to Sunday"
            }
         ]
      },
      {
         "type": "FINISH",
         "text": "Finish!",
         "point": [
            -123.12479,
            49.28456
         ]
      }
   ]
}
```


 
## optimalRoute Resource

The optimalRoute resource represents the shortest or fastest route between a start point and a series of end points reordered to minimize total route distance or time. Here are some examples:

1. Shortest optimal route in km and json between the following addresses in Victoria, BC:

1200 Douglas St, 1020 View St, 851 Broughton St, 835 Fisgard St, and 707 Fort St 

https://router.api.gov.bc.ca/optimalRoute.json?criteria=shortest&points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465
   
2. Fastest optimal route in km and kml between same addresses as example 1

https://router.api.gov.bc.ca/optimalRoute.kml?points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465

### HTTP response
The optimalRoute resource will return the following representation:


Attribute Name |	Type
---------------------: | --- |
[routeDescription](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeDescription) | String
[searchTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#searchTimestamp) | Datetime
[executionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[routeExecutionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[optimizationExecutionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[version](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#version) | String 
[disclaimer](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#disclaimer) | String
[privacyStatement](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#privacyStatement) | String
[copyrightNotice](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightNotice) | String
[copyrightLicense](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightLicense) | String
[srsCode](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#srsCode) | Integer
[criteria](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#criteria) | String
[enable](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) | String
[distanceUnit](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distanceUnit) | String
[dataProcessingTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#dataProcessingTimestamp) | String
[roadNetworkTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#roadNetworkTimestamp) | String
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | list of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String
[visitOrder](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#visitOrder) | List of Integer
[route](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#route) | List of Point


Here is a sample json response:

```json
{
   "routeDescription": null,
   "searchTimestamp": "2024-01-18T00:16:46.497283541",
   "executionTime": 87,
   "routingExecutionTime": 2,
   "optimizationExecutionTime": 83,
   "version": "2.2.1-RELEASE",
   "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
   "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
   "copyrightNotice": "Copyright 2024 Province of British Columbia",
   "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
   "srsCode": 4326,
   "criteria": "fastest",
   "enable": "gdf,ldf,tc,tr,xc",
   "distanceUnit": "km",
   "dataProcessingTimestamp": "2023-12-18T23:05:54Z",
   "roadNetworkTimestamp": "2023-09-29T21:28:38Z",
   "points": [
      [
         -123.36517,
         48.42545
      ],
      [
         -123.35587,
         48.42445
      ],
      [
         -123.36057,
         48.42323
      ],
      [
         -123.36002,
         48.42915
      ],
      [
         -123.36479,
         48.42455
      ]
   ],
   "routeFound": true,
   "distance": 1.919,
   "time": 273.1071662398253,
   "timeText": "4 minutes 33 seconds",
   "visitOrder": [
      0,
      3,
      2,
      4,
      1
   ],
   "route": [
      [
         -123.36517,
         48.42545
      ],
      [
         -123.36508,
         48.42544
      ],
      [
         -123.36533,
         48.42465
      ],
      [
         -123.36478,
         48.42459
      ],
      [
         -123.36249,
         48.42432
      ],
      [
         -123.36269,
         48.42352
      ],
      [
         -123.36056,
         48.42327
      ],
      [
         -123.35992,
         48.42319
      ],
      [
         -123.35985,
         48.42352
      ],
      [
         -123.35972,
         48.42401
      ],
      [
         -123.3569,
         48.42373
      ],
      [
         -123.35674,
         48.42451
      ],
      [
         -123.35588,
         48.42442
      ],
      [
         -123.35674,
         48.42451
      ],
      [
         -123.35654,
         48.42537
      ],
      [
         -123.35631,
         48.42627
      ],
      [
         -123.35611,
         48.42709
      ],
      [
         -123.35618,
         48.42732
      ],
      [
         -123.35611,
         48.42795
      ],
      [
         -123.35606,
         48.42833
      ],
      [
         -123.35599,
         48.42884
      ],
      [
         -123.35885,
         48.42901
      ],
      [
         -123.35928,
         48.42914
      ],
      [
         -123.36002,
         48.42919
      ]
   ]
}
```

The visitOrder values need a bit more explanation. The points in the request in example 2 above are given in the following order:

0. 1200 Douglas St
1. 1020 View St
2. 851 Broughton St
3. 835 Fisgard St
4. 707 Fort St

The response above is the response to the request in example 2 and contains the visitOrder 0,3,2,4,1. visitOrder represents the position in the optimal order each input point should appear in as follows:

1200 Douglas St is zeroeth point (0)<br>
1020 View St is third point (3)<br>
851 Broughton St is second point (2)<br>
835 Fisgard St is fourth point (4)<br>
707 Fort St is first point (1)

Your application can then use the visitOrder to write out the stops in the optimal order:

0. 1200 Douglas St
1. 707 Fort St
2. 851 Broughton St
3. 1020 View St
4. 835 Fisgard St


##  optimalDirections Resource
The optimalDirections resource represents the turn-by-turn directions, shortest or fastest route between given points and the length and duration of that route. Here are some examples:

1. Shortest optimal route and directions in km and json between the following addresses in Victoria, BC:

1200 Douglas St, 1020 View St, 851 Broughton St, 835 Fisgard St, and 707 Fort St 

https://router.api.gov.bc.ca/optimalDirections.json?criteria=shortest&points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465
   
2. Fastest optimal route and directions in km and kml between same addresses as example 1

https://router.api.gov.bc.ca/optimalDirections.kml?points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465

### HTTP response
The optimalDirections resource will return the following representation:

Attribute Name |	Type
---------------------: | --- |
[routeDescription](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeDescription) | String
[searchTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#searchTimestamp) | Datetime
[executionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[routingExecutionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[optimizationExecutionTime](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#executionTime) | Real
[version](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#version) | String 
[disclaimer](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#disclaimer) | String
[privacyStatement](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#privacyStatement) | String
[copyrightNotice](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightNotice) | String
[copyrightLicense](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#copyrightLicense) | String
[srsCode](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#srsCode) | Integer
[criteria](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#criteria) | String
[enable](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#enable) | String
[distanceUnit](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distanceUnit) | String
[dataProcessingTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#dataProcessingTimestamp) | String
[roadNetworkTimestamp](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#roadNetworkTimestamp) | String
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | list of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String
[visitOrder](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#visitOrder) | List of Integer
[route](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#route) | List of Point
[notifications](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#notifications) | String
[directions](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#directions) | String

Here is a sample json response:

```json
{
   "routeDescription": null,
   "searchTimestamp": "2024-01-18T00:19:07.508034926",
   "executionTime": 80,
   "routingExecutionTime": 1,
   "optimizationExecutionTime": 77,
   "version": "2.2.1-RELEASE",
   "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
   "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
   "copyrightNotice": "Copyright 2024 Province of British Columbia",
   "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
   "srsCode": 4326,
   "criteria": "fastest",
   "enable": "gdf,ldf,tc,tr,xc",
   "distanceUnit": "km",
   "dataProcessingTimestamp": "2023-12-18T23:05:54Z",
   "roadNetworkTimestamp": "2023-09-29T21:28:38Z",
   "points": [
      [
         -123.36517,
         48.42545
      ],
      [
         -123.35587,
         48.42445
      ],
      [
         -123.36057,
         48.42323
      ],
      [
         -123.36002,
         48.42915
      ],
      [
         -123.36479,
         48.42455
      ]
   ],
   "routeFound": true,
   "distance": 1.919,
   "time": 273.1071662398253,
   "timeText": "4 minutes 33 seconds",
   "visitOrder": [
      0,
      3,
      2,
      4,
      1
   ],
   "route": [
      [
         -123.36517,
         48.42545
      ],
      [
         -123.36508,
         48.42544
      ],
      [
         -123.36533,
         48.42465
      ],
      [
         -123.36478,
         48.42459
      ],
      [
         -123.36249,
         48.42432
      ],
      [
         -123.36269,
         48.42352
      ],
      [
         -123.36056,
         48.42327
      ],
      [
         -123.35992,
         48.42319
      ],
      [
         -123.35985,
         48.42352
      ],
      [
         -123.35972,
         48.42401
      ],
      [
         -123.3569,
         48.42373
      ],
      [
         -123.35674,
         48.42451
      ],
      [
         -123.35588,
         48.42442
      ],
      [
         -123.35674,
         48.42451
      ],
      [
         -123.35654,
         48.42537
      ],
      [
         -123.35631,
         48.42627
      ],
      [
         -123.35611,
         48.42709
      ],
      [
         -123.35618,
         48.42732
      ],
      [
         -123.35611,
         48.42795
      ],
      [
         -123.35606,
         48.42833
      ],
      [
         -123.35599,
         48.42884
      ],
      [
         -123.35885,
         48.42901
      ],
      [
         -123.35928,
         48.42914
      ],
      [
         -123.36002,
         48.42919
      ]
   ],
   "notifications": [],
   "directions": [
      {
         "type": "START",
         "name": "View St",
         "distance": 0.007,
         "time": 9,
         "heading": "EAST",
         "text": "Head east on View St for 7 m (9 seconds)",
         "point": [
            -123.36517,
            48.42545
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "Douglas St",
         "distance": 0.089,
         "time": 15,
         "text": "Turn right onto Douglas St for 90 m (15 seconds)",
         "point": [
            -123.36508,
            48.42544
         ]
      },
      {
         "type": "TURN_LEFT",
         "name": "Fort St",
         "distance": 0.042,
         "time": 7,
         "text": "Turn left onto Fort St for 40 m (7 seconds)",
         "point": [
            -123.36533,
            48.42465
         ]
      },
      {
         "type": "STOPOVER",
         "text": "Stopover 1",
         "point": [
            -123.36478,
            48.42459
         ]
      },
      {
         "type": "START",
         "name": "Fort St",
         "distance": 0.173,
         "time": 23,
         "heading": "EAST",
         "text": "Head east on Fort St for 150 m (23 seconds)",
         "point": [
            -123.36478,
            48.42459
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "Blanshard St",
         "distance": 0.091,
         "time": 13,
         "text": "Turn right onto Blanshard St for 90 m (13 seconds)",
         "point": [
            -123.36249,
            48.42432
         ]
      },
      {
         "type": "TURN_LEFT",
         "name": "Broughton St",
         "distance": 0.16,
         "time": 18,
         "text": "Turn left onto Broughton St for 150 m (18 seconds)",
         "point": [
            -123.36269,
            48.42352
         ]
      },
      {
         "type": "STOPOVER",
         "text": "Stopover 2",
         "point": [
            -123.36056,
            48.42327
         ]
      },
      {
         "type": "START",
         "name": "Broughton St",
         "distance": 0.048,
         "time": 17,
         "heading": "EAST",
         "text": "Head east on Broughton St for 50 m (17 seconds)",
         "point": [
            -123.36056,
            48.42327
         ]
      },
      {
         "type": "TURN_LEFT",
         "name": "Quadra St",
         "distance": 0.093,
         "time": 19,
         "text": "Turn left onto Quadra St for 95 m (19 seconds)",
         "point": [
            -123.35992,
            48.42319
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "Fort St",
         "distance": 0.212,
         "time": 23,
         "text": "Turn right onto Fort St for 200 m (23 seconds)",
         "point": [
            -123.35972,
            48.42401
         ]
      },
      {
         "type": "TURN_LEFT",
         "name": "Vancouver St",
         "distance": 0.088,
         "time": 15,
         "text": "Turn left onto Vancouver St for 90 m (15 seconds)",
         "point": [
            -123.3569,
            48.42373
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "View St",
         "distance": 0.064,
         "time": 7,
         "text": "Turn right onto View St for 65 m (7 seconds)",
         "point": [
            -123.35674,
            48.42451
         ]
      },
      {
         "type": "STOPOVER",
         "text": "Stopover 3",
         "point": [
            -123.35588,
            48.42442
         ]
      },
      {
         "type": "START",
         "name": "View St",
         "distance": 0.064,
         "time": 15,
         "heading": "WEST",
         "text": "Head west on View St for 65 m (15 seconds)",
         "point": [
            -123.35588,
            48.42442
         ]
      },
      {
         "type": "TURN_RIGHT",
         "name": "Vancouver St",
         "distance": 0.485,
         "time": 62,
         "text": "Turn right onto Vancouver St for 500 m (1 minute 2 seconds)",
         "point": [
            -123.35674,
            48.42451
         ]
      },
      {
         "type": "TURN_LEFT",
         "name": "Balmoral Rd",
         "distance": 0.214,
         "time": 25,
         "text": "Turn left onto Balmoral Rd for 200 m (25 seconds)",
         "point": [
            -123.35599,
            48.42884
         ]
      },
      {
         "type": "CONTINUE",
         "name": "Fisgard St",
         "distance": 0.09,
         "time": 6,
         "text": "Continue onto Fisgard St for 90 m (6 seconds)",
         "point": [
            -123.35885,
            48.42901
         ]
      },
      {
         "type": "FINISH",
         "text": "Finish!",
         "point": [
            -123.36002,
            48.42919
         ]
      }
   ]
}
```

<br><br>
<a name=APIReponseErrorCodes></a>
## API reponse error codes
### KONG API gateway errors
We used Kong API gateway to manage all API calls. Here is a list of the gateway errors for reference. You will not see those error if you install your own version of the router.

|Response Code|Error Message|Error Description
|--|--|--|
|404|This page is not found|The path is not defined
|401|No API key found in reques|The API endpoints requires an API key
|401|Invalid authentication credentials|The provided API key is not found
|403|You cannot consume this service|The provided API key is invalid, unapproved or expired.
|429|API rate limit exceeded|Too many requests per minute

### Application specific errors 
Router can return a number of error response.
|Response Code|Error Message|Error Description
|--|--|--|
|400|Invalid parameter: Parameter "points" is required and must be in the format "x,y,x,y..."|The provided points are missing or invalid
|400|Invalid parameter values for the following parameters [detail]|The provided parameter is invalid
|404|Not Found|The path is not found. Please make sure it’s one in document
|500|Anything|This is a general internal error

In addition to above common error responses there are also a number of errors that can happen occasionally or during the initialization state. These errors usually come with 500s but could also be 400s.

- **Unable to load ConfigurationStore class:** check for invalid parcel API key.
- **Invalid arguments to create WeeklyTimeRange:** database related error.
- **Invalid DistanceUnit value:** database related error.
- **Unable to load specified dataSource.class:** database related error.
- **Unexpected error in coordinate reprojection:** unknown data error.
- **KML output not supported for RouterDistanceBetweenPairsResponse.:** unknown internal/data error.
- **Parameter must be in UUID format XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX:** invalid UUID
