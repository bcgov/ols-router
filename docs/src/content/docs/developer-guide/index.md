---
title: Developer Guide
description: Developer guide for the BC Route Planner API.
---

# BC Route Planner Developer Guide
This guide is aimed at developers who would like to incorporate the BC Route Planner into their applications, websites and scripts.

## Introduction
The BC Route Planner REST API lets you integrate routing between locations in BC into your own applications. This document expands on aspects of the REST API that are covered in the [API Specification](https://raw.githubusercontent.com/bcgov/api-specs/master/router/router.json). You can test and explore the API in the [API Console](https://openapi.apps.gov.bc.ca/?url=https://raw.githubusercontent.com/bcgov/api-specs/master/router/router.json). For a list of the latest changes to the Route Planner API, see [Route Planner Release Notes](rpng-release-notes)

Your application can store router results or display them on any web map. The BC Route Planner API supports GET and POST requests. POST should be used when you have many waypoints to visit.

## Technical Overview

Access to the BC Route Planner API is mediated by the Data Systems and Services branch [API Services Portal](https://api.gov.bc.ca/).

Source data for the BC Route Planner comes from a variety of sources outlined in data flow #1 and #3 of the [data pipeline](https://github.com/bcgov/ols-geocoder/blob/gh-pages/address-data-pipeline.md). Data is updated on a monthly basis. The BC Route Planner loads this data from files into in-memory data structures at startup. A small configuration file of global parameters is also loaded at startup from a key-value Datastore.

The BC Route Planner is written in Java and uses the jSprit open source libraries. The [Location Services in Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html) web application, which demonstrates the features of the BC Route Planner, is written in JavaScript and uses jQuery and Leaflet libraries and plugins.

## Latest updates to the Route Planner API
For a list of the latest updates to the BC Route Planner API, see the [release notes](rpng-release-notes).

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

5. Length and time of the fastest route in km for a truck with specific dimensions and weight. This route will also arrive on the correct side of the road and use the default values for the enable and disable parameters. <br> https://router.api.gov.bc.ca/truck/distance.json?points=-123.70794,48.77869,-123.53785,48.38200&outputSRS=4326&criteria=fastest&distanceUnit=km&roundTrip=false&correctSide=true&height=4.5&width=3&length=10&weight=18000&enable=gdf,ldf,tr,xc,tc&disable=td,ev,sc,tf,tl&snapDistance=1000&routeDescription=Truck_routing_example

6. Length and time of shortest route in km between pairs fromPoints and toPoints. In this example, we use the maxPairs parameter to control the maximum number of pairs to return for each toPoint. Pairs are ordered by distance/time from fromPoint<br>
https://router.api.gov.bc.ca/distance/betweenPairs.json?routeDescription=betweenPairs%20test%20case&fromPoints=-123.70794%2C48.77869%2C-123.53785%2C48.38200&toPoints=-124.972951%2C49.715181%2C-123.139464%2C49.704015&criteria=shortest&maxPairs=1<br>

**The betweenPairs request can also be submitted to follow a truck route by changing '/distance' to '/truck/distance'**


### HTTP Response
The distance resource will return the following representation:

Attribute Name | Type
---------------------: | --- |
routeDescription | String
searchTimestamp | Datetime
executionTime | Real
version | String
disclaimer | String
privacyStatement | String
copyrightNotice | String
copyrightLicense | String
srsCode | Integer
criteria | String
enable | String
distanceUnit | String
dataProcessingTimestamp | String
roadNetworkTimestamp | String
points | list of Point
routeFound | Boolean
distance | String
time | Integer
timeText | String


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

7. Fastest route for a truck following truck routes in Vancouver with partitioning of best route by truck route, ferry, locality and ownership<br>
https://router.api.gov.bc.ca/truck/route.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality,ownership<br>

8. Fastest route in km and json between Williams Lake and a mine. In this case, we are trying to navigate to a location that is not found on the road network. By default, a route cannot be found to a point that is beyond 1000 metres from the road network. Using the snapDistance parameter we can override this default.<br>
https://router.api.gov.bc.ca/route.json?points=-122.14%2C52.1288889%2C-121.61941%2C52.54039&snapDistance=1200&apikey=JKM6YZujMf93wbQQVxAHM7XIWAWeaFX4<br>

9. Length and time of the fastest route in km for a truck with specific dimensions and weight. This route will also arrive on the correct side of the road and use the default values for the enable and disable parameters. This example will also include route partitions and restriction IDs <br> https://router.api.gov.bc.ca/truck/route.json?points=-123.70794,48.77869,-123.53785,48.38200&outputSRS=4326&criteria=fastest&distanceUnit=km&roundTrip=false&correctSide=true&height=4.5&width=3&length=10&weight=18000&partition=isTruckRoute,isFerry,locality,ownership&enable=gdf,ldf,tr,xc,tc&disable=td,ev,sc,tf,tl&snapDistance=1000&listRestrictions=true&restrictionSource=ITN&routeDescription=Truck_routing_example


### HTTP response
The route resource will return the following representation:

Attribute Name | Type
---------------------: | --- |
routeDescription | String
searchTimestamp | Datetime
executionTime | Real
version | String
disclaimer | String
privacyStatement | String
copyrightNotice | String
copyrightLicense | String
srsCode | Integer
criteria | String
enable | String
distanceUnit | String
dataProcessingTimestamp | String
roadNetworkTimestamp | String
points | list of Point
routeFound | Boolean
distance | String
time | Integer
timeText | String
partition | String
partitions | String
route | List of Point


Here is a request for fastest route in vancouver with partitions in json:

https://router.api.gov.bc.ca/truck/route.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality,ownership


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


## Directions Resource
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

7. Directions and fastest route for a truck following truck routes in Vancouver with partitioning of best route by truck route, ferry, locality and ownership<br>
https://router.api.gov.bc.ca/truck/directions.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality,ownership<br>

8. Directions and fastest route in km and json between Williams Lake and a mine. In this case, we are trying to navigate to a location that is not found on the road network. By default, a route cannot be found to a point that is beyond 1000 metres from the road network. Using the snapDistance parameter we can override this default.<br>
https://router.api.gov.bc.ca/directions.json?points=-122.14%2C52.1288889%2C-121.61941%2C52.54039&snapDistance=1200&apikey=JKM6YZujMf93wbQQVxAHM7XIWAWeaFX4<br>

9. Length and time of the fastest route in km for a truck with specific dimensions and weight. This route will also arrive on the correct side of the road and use the default values for the enable and disable parameters. This example will also include route partitions and restriction IDs <br> https://router.api.gov.bc.ca/truck/directions.json?points=-123.70794,48.77869,-123.53785,48.38200&outputSRS=4326&criteria=fastest&distanceUnit=km&roundTrip=false&correctSide=true&height=4.5&width=3&length=10&weight=18000&partition=isTruckRoute,isFerry,locality,ownership&enable=gdf,ldf,tr,xc,tc&disable=td,ev,sc,tf,tl&snapDistance=1000&listRestrictions=true&restrictionSource=ITN&routeDescription=Truck_routing_example


### HTTP response
The directions resource will return the following representation:

Attribute Name | Type
---------------------: | --- |
routeDescription | String
searchTimestamp | Datetime
executionTime | Real
version | String
disclaimer | String
privacyStatement | String
copyrightNotice | String
copyrightLicense | String
srsCode | Integer
criteria | String
enable | String
distanceUnit | String
dataProcessingTimestamp | String
roadNetworkTimestamp | String
points | List of Point
routeFound | Boolean
distance | String
time | Integer
timeText | String
partition | String
partitions | String
route | List of Point
notifications | String
directions | String


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


## optimalRoute Resource

The optimalRoute resource represents the shortest or fastest route between a start point and a series of end points reordered to minimize total route distance or time. Here are some examples:

1. Shortest optimal route in km and json between the following addresses in Victoria, BC:

1200 Douglas St, 1020 View St, 851 Broughton St, 835 Fisgard St, and 707 Fort St

https://router.api.gov.bc.ca/optimalRoute.json?criteria=shortest&points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465

2. Fastest optimal route in km and kml between same addresses as example 1

https://router.api.gov.bc.ca/optimalRoute.kml?points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465

### HTTP response
The optimalRoute resource will return the following representation:


Attribute Name | Type
---------------------: | --- |
routeDescription | String
searchTimestamp | Datetime
executionTime | Real
routeExecutionTime | Real
optimizationExecutionTime | Real
version | String
disclaimer | String
privacyStatement | String
copyrightNotice | String
copyrightLicense | String
srsCode | Integer
criteria | String
enable | String
distanceUnit | String
dataProcessingTimestamp | String
roadNetworkTimestamp | String
points | list of Point
routeFound | Boolean
distance | String
time | Integer
timeText | String
visitOrder | List of Integer
route | List of Point


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


## optimalDirections Resource
The optimalDirections resource represents the turn-by-turn directions, shortest or fastest route between given points and the length and duration of that route. Here are some examples:

1. Shortest optimal route and directions in km and json between the following addresses in Victoria, BC:

1200 Douglas St, 1020 View St, 851 Broughton St, 835 Fisgard St, and 707 Fort St

https://router.api.gov.bc.ca/optimalDirections.json?criteria=shortest&points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465

2. Fastest optimal route and directions in km and kml between same addresses as example 1

https://router.api.gov.bc.ca/optimalDirections.kml?points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465

### HTTP response
The optimalDirections resource will return the following representation:

Attribute Name | Type
---------------------: | --- |
routeDescription | String
searchTimestamp | Datetime
executionTime | Real
routingExecutionTime | Real
optimizationExecutionTime | Real
version | String
disclaimer | String
privacyStatement | String
copyrightNotice | String
copyrightLicense | String
srsCode | Integer
criteria | String
enable | String
distanceUnit | String
dataProcessingTimestamp | String
roadNetworkTimestamp | String
points | list of Point
routeFound | Boolean
distance | String
time | Integer
timeText | String
visitOrder | List of Integer
route | List of Point
notifications | String
directions | String
