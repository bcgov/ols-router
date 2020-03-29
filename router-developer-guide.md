# BC Route Planner
# Developer Guide
This guide is aimed at developers and web masters who would like to incorporate the BC Route Planner into their applications and websites.
<br>
## Introduction
The BC Route Planner REST API lets you integrate basic routing between BC locations into your own applications. This document defines aspects of the REST API that are not covered in the [Swagger definition](https://raw.githubusercontent.com/bcgov/api-specs/master/router/router.json). You can explore the API in the [API Console](https://catalogue.data.gov.bc.ca/dataset/bc-route-planner/resource/82cd3194-0955-4d7e-b35a-78a98fda153a/view/80721e92-1a39-4300-ac76-6cfb09493d81). For a list of the latest changes to the Route Planner API, see [Route Planner Release Notes](https://bcgov.github.io/ols-router/rpng-release-notes.html)
<br>

Your application can store router results or display them on any web map. The BC Route Planner API supports GET and POST requests. POST should be used when you have many waypoints to visit.

## Limitations of Route Planner API v2.1
Route Planner API v2.1 is backward-compatible with v2.0. There are still severe limitations on the new features and data in Route Planner v2.1 . For complete details, see the [release notes](https://github.com/bcgov/ols-router/blob/gh-pages/rpng-release-notes.md).

## API Key
Use of the BC Route Planner REST API is currently restricted to government. If you are working on a government application that needs routing, please visit [here](https://github.com/bcgov/gwa/wiki/Developer-Guide#developer-api-keys) to find out how to get an API key.

Every route planner request needs an apiKey header that contains your api key as follows:
```
apiKey: <myapikey>
```	
Do not put the apiKey in the request URL because it will expose your api key.


## Distance Resource
The distance resource represents the length and duration of the shortest or fastest route between given points. Here are some examples:

1. Length of shortest route in km and json between Duncan and Metchosin<br>https://router.api.gov.bc.ca/distance.json?routeDescription=shortest%20distance%20in%20km%20and%20json&points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&criteria=shortest&distanceUnits=km<br>
   
2. Length of shortest route in km and kml between Duncan and Metchosin<br>https://router.api.gov.bc.ca/distance.kml?routeDescription=shortest%20distance%20in%20km%20and%20kml&points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&criteria=shortest&distanceUnits=km<br>

3. Length of fastest route in miles and html between Duncan and Metchosin<br>https://router.api.gov.bc.ca/distance.html?routeDescription=fastest%20distance%20in%20km%20and%20html&points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&criteria=fastest&distanceUnit=mi<br>



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
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | list of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String


Here is a sample json request for the distance of fastest route in km:
```
https://router.api.gov.bc.ca/truck/distance.json?routeDescription=fastest%20distance%20in%20km%20and%20json&points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&criteria=fastest&distanceUnit=km
```

and here is the response:

```
{
  "routeDescription": "fastest distance in km and json",
  "searchTimestamp": "2020-03-29 23:27:48",
  "executionTime": 51,
  "version": "2.1.0",
  "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
  "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
  "copyrightNotice": "Copyright 2020 Province of British Columbia - Open Government License",
  "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
  "srsCode": 4326,
  "criteria": "fastest",
  "enable": "gdf,ldf,tc,tr,xc",
  "distanceUnit": "km",
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
  "distance": 56.118,
  "time": 3189.2243494173135,
  "timeText": "53 minutes 9 seconds"
}
```


## Route Resource
The route resource represents the shortest or fastest route between given points and the length and duration of that route. Here are some examples:

1. Shortest route in km and json between Duncan and Metchosin<br>https://router.api.gov.bc.ca/route.json?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&criteria=shortest&distanceUnits=km&apikey=myapikey<br>
   
2. Shortest route in km and kml between Duncan and Metchosin<br>https://router.api.gov.bc.ca/route.kml?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&criteria=shortest&distanceUnits=km&apikey=myapikey<br>

3. Fastest route in miles and html between Duncan and Metchosin<br>https://router.api.gov.bc.ca/route.html?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&distanceUnit=mi&apikey=myapikey<br>

4. Fastest route from 976 Meares St, Victoria to 1200 Douglas St, Victoria on the correct side of the street<br>
   https://router.api.gov.bc.ca/route.json?points=-123.3575846%2C48.4233118%2C-123.3651354%2C48.4255742&correctSide=true&apikey=myapikey<br>

5. Fastest route around a bridge for an overheight truck following truck routes<br>
https://router.api.gov.bc.ca/truck/route.json?points=-123.392803%2C48.4330137%2C-123.3940682%2C48.4360118&height=5.1&followTruckRoute=true&apikey=myapikey<br>

6. Fastest route around a bridge for an overweight truck following truck routes<br>
https://router.api.gov.bc.ca/truck/route.json?points=-116.80488%2C49.69928%2C-116.8053633591626%2C49.6953321774235&weight=30001&followTruckRoute=true&apikey=myapikey<br>
   
7. Fastest route for a truck following truck routes in Vancouver with partitioning of best route by truck route, ferry, and locality<br>
https://router.api.gov.bc.ca/truck/route.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality&apikey=myapikey<br>
   


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
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | list of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String
[partition](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#partition) | String
[partitions](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#partitions) | String
[route](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#route) | List of Point

Here is a sample json response above:

```
{
  "routeDescription": null,
  "searchTimestamp": "2020-03-27 22:54:30",
  "executionTime": 2,
  "version": "2.1.0",
  "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
  "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
  "copyrightNotice": "Copyright 2020 Province of British Columbia - Open Government License",
  "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
  "srsCode": 4326,
  "criteria": "fastest",
  "enable": "gdf,ldf,tc,tr,xc",
  "distanceUnit": "km",
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
  "time": 131.30227738040392,
  "timeText": "2 minutes 11 seconds",
  "partition": "isFerry,isTruckRoute,locality",
  "partitions": [
    {
      "index": 0,
      "isFerry": false,
      "isTruckRoute": false,
      "locality": "Vancouver"
    },
    {
      "index": 3,
      "isFerry": false,
      "isTruckRoute": true,
      "locality": "Vancouver"
    },
    {
      "index": 9,
      "isFerry": false,
      "isTruckRoute": false,
      "locality": "Vancouver"
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
      49.2653
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

1. Directions and shortest route in km and json between Duncan and Metchosin<br>https://router.api.gov.bc.ca/directions.json?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&criteria=shortest&distanceUnits=km&apikey=myapikey<br>
   
2. Directions and shortest route in km and kml between Duncan and Metchosin<br>https://router.api.gov.bc.ca/directions.kml?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&criteria=shortest&distanceUnits=km&apikey=myapikey<br>

3. Directions and fastest route in miles and html between Duncan and Metchosin<br>https://router.api.gov.bc.ca/route.html?points=-123.707942%2C48.778691%2C-123.537850%2C48.382005&outputSRS=4326&distanceUnit=mi&apikey=myapikey<br>

4. Directions and fastest route from 976 Meares St, Victoria to 1200 Douglas St, Victoria on the correct side of the street<br>
   https://router.api.gov.bc.ca/directions.json?points=-123.3575846%2C48.4233118%2C-123.3651354%2C48.4255742&followTruckRoute=true&correctSide=true&apikey=myapikey<br>

5. Directions and fastest route around a bridge for an overheight truck<br>
https://router.api.gov.bc.ca/truck/directions.json?points=-123.392803%2C48.4330137%2C-123.3940682%2C48.4360118&followTruckRoute=true&height=5.1&apikey=myapikey<br>

6. Directions and fastest route around a bridge for an overweight truck<br>
https://router.api.gov.bc.ca/truck/directions.json?points=-116.80488%2C49.69928%2C-116.8053633591626%2C49.6953321774235&followTruckRoute=true&weight=30001&apikey=myapikey<br>
   
7. Directions and fastest route for a truck following truck routes in Vancouver with partitioning of best route by truck route, ferry, and locality<br>
https://router.api.gov.bc.ca/truck/directions.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality&apikey=myapikey<br>


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

Here is a sample json request for a route that includes multiple partitions:

```
https://router.api.gov.bc.ca/truck/directions.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality

https://router-dev.pathfinder..gov.bc.ca/truck/directions.json?points=-123.1138889%2C49.2611111%2C-123.11165904393421%2C49.26551411372797&followTruckRoute=true&partition=isTruckRoute,isFerry,locality
```

and here's the response:

```
{
  "routeDescription": null,
  "searchTimestamp": "2020-03-29 22:52:31",
  "executionTime": 3,
  "version": "2.1.0",
  "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
  "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
  "copyrightNotice": "Copyright 2020 Province of British Columbia - Open Government License",
  "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
  "srsCode": 4326,
  "criteria": "fastest",
  "enable": "gdf,ldf,tc,tr,xc",
  "distanceUnit": "km",
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
  "time": 131.30227738040392,
  "timeText": "2 minutes 11 seconds",
  "partition": "isFerry,isTruckRoute,locality",
  "partitions": [
    {
      "index": 0,
      "isFerry": false,
      "isTruckRoute": false,
      "locality": "Vancouver"
    },
    {
      "index": 3,
      "isFerry": false,
      "isTruckRoute": true,
      "locality": "Vancouver"
    },
    {
      "index": 9,
      "isFerry": false,
      "isTruckRoute": false,
      "locality": "Vancouver"
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
      49.2653
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
      "name": "Broadway",
      "distance": 0.138,
      "time": 28,
      "text": "Turn left onto Broadway for 150 m (28 seconds)",
      "point": [
        -123.11289,
        49.26318
      ]
    },
    {
      "type": "TURN_RIGHT",
      "name": "Cambie St",
      "distance": 0.285,
      "time": 44,
      "text": "Turn right onto Cambie St for 300 m (44 seconds)",
      "point": [
        -123.11478,
        49.2632
      ]
    },
    {
      "type": "TURN_RIGHT",
      "name": "6th Ave",
      "distance": 0.221,
      "time": 30,
      "text": "Turn right onto 6th Ave for 200 m (30 seconds)",
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


Here's a sample json request for directions and a route in Vancouver that has notifications:

https://router.api.gov.bc.ca/truck/directions.json?points=-123.0739278%2C49.284965%2C-123.0277521%2C49.3155266&followTruckRoute=true&partition=isTruckRoute

and here's the response:

```
{
  "routeDescription": null,
  "searchTimestamp": "2020-03-29 20:54:12",
  "executionTime": 96,
  "version": "2.1.0",
  "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
  "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
  "copyrightNotice": "Copyright 2020 Province of British Columbia - Open Government License",
  "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
  "srsCode": 4326,
  "criteria": "fastest",
  "enable": "gdf,ldf,tc,tr,xc",
  "distanceUnit": "km",
  "points": [
    [
      -123.07393,
      49.28496
    ],
    [
      -123.02775,
      49.31553
    ]
  ],
  "routeFound": true,
  "distance": 8.441,
  "time": 746.5539234474583,
  "timeText": "12 minutes 27 seconds",
  "partition": "isTruckRoute",
  "partitions": [
    {
      "index": 0,
      "isTruckRoute": false
    },
    {
      "index": 1,
      "isTruckRoute": true
    },
    {
      "index": 88,
      "isTruckRoute": false
    }
  ],
  "route": [
    [
      -123.07371,
      49.28424
    ],
    [
      -123.0758,
      49.28397
    ],
    [
      -123.07595,
      49.28455
    ],
    [
      -123.07605,
      49.28469
    ],
    [
      -123.07623,
      49.28478
    ],
    [
      -123.07644,
      49.2848
    ],
    [
      -123.07653,
      49.28478
    ],
    [
      -123.07666,
      49.28475
    ],
    [
      -123.07687,
      49.28461
    ],
    [
      -123.07703,
      49.28436
    ],
    [
      -123.07705,
      49.28422
    ],
    [
      -123.07709,
      49.28381
    ],
    [
      -123.07711,
      49.28306
    ],
    [
      -123.07713,
      49.2822
    ],
    [
      -123.07711,
      49.28181
    ],
    [
      -123.07711,
      49.28158
    ],
    [
      -123.0771,
      49.28134
    ],
    [
      -123.07412,
      49.28131
    ],
    [
      -123.07263,
      49.28129
    ],
    [
      -123.0704,
      49.28125
    ],
    [
      -123.06829,
      49.28124
    ],
    [
      -123.06557,
      49.28121
    ],
    [
      -123.06363,
      49.28119
    ],
    [
      -123.06169,
      49.28118
    ],
    [
      -123.05976,
      49.28117
    ],
    [
      -123.05784,
      49.28117
    ],
    [
      -123.05654,
      49.28117
    ],
    [
      -123.05458,
      49.28117
    ],
    [
      -123.05197,
      49.28116
    ],
    [
      -123.04934,
      49.28116
    ],
    [
      -123.04666,
      49.28115
    ],
    [
      -123.04403,
      49.28116
    ],
    [
      -123.03884,
      49.28116
    ],
    [
      -123.03619,
      49.28116
    ],
    [
      -123.03414,
      49.28116
    ],
    [
      -123.03233,
      49.28115
    ],
    [
      -123.03191,
      49.28115
    ],
    [
      -123.03177,
      49.28115
    ],
    [
      -123.03168,
      49.28114
    ],
    [
      -123.03152,
      49.28114
    ],
    [
      -123.03148,
      49.28114
    ],
    [
      -123.03128,
      49.28114
    ],
    [
      -123.03128,
      49.28119
    ],
    [
      -123.0313,
      49.28138
    ],
    [
      -123.03142,
      49.28303
    ],
    [
      -123.03137,
      49.28371
    ],
    [
      -123.03142,
      49.28551
    ],
    [
      -123.03139,
      49.28583
    ],
    [
      -123.03116,
      49.28646
    ],
    [
      -123.03089,
      49.28686
    ],
    [
      -123.03081,
      49.28694
    ],
    [
      -123.03073,
      49.28701
    ],
    [
      -123.03068,
      49.28705
    ],
    [
      -123.03053,
      49.2872
    ],
    [
      -123.03,
      49.28771
    ],
    [
      -123.02843,
      49.28905
    ],
    [
      -123.02831,
      49.28916
    ],
    [
      -123.02654,
      49.29064
    ],
    [
      -123.02631,
      49.29096
    ],
    [
      -123.0262,
      49.29119
    ],
    [
      -123.02613,
      49.29158
    ],
    [
      -123.02609,
      49.2924
    ],
    [
      -123.02616,
      49.29535
    ],
    [
      -123.02638,
      49.30257
    ],
    [
      -123.0264,
      49.30306
    ],
    [
      -123.02661,
      49.30368
    ],
    [
      -123.02667,
      49.30386
    ],
    [
      -123.02716,
      49.30459
    ],
    [
      -123.02732,
      49.30482
    ],
    [
      -123.02736,
      49.30488
    ],
    [
      -123.0274,
      49.30498
    ],
    [
      -123.02763,
      49.30544
    ],
    [
      -123.0278,
      49.30602
    ],
    [
      -123.02785,
      49.30679
    ],
    [
      -123.0278,
      49.3081
    ],
    [
      -123.0278,
      49.30953
    ],
    [
      -123.02768,
      49.31011
    ],
    [
      -123.02756,
      49.31057
    ],
    [
      -123.02741,
      49.3109
    ],
    [
      -123.02731,
      49.31104
    ],
    [
      -123.02713,
      49.31118
    ],
    [
      -123.02689,
      49.31129
    ],
    [
      -123.0263,
      49.31142
    ],
    [
      -123.02583,
      49.31162
    ],
    [
      -123.02551,
      49.31184
    ],
    [
      -123.02526,
      49.31229
    ],
    [
      -123.02518,
      49.31239
    ],
    [
      -123.02506,
      49.31271
    ],
    [
      -123.02436,
      49.31393
    ],
    [
      -123.0245,
      49.31396
    ],
    [
      -123.02486,
      49.31405
    ],
    [
      -123.02516,
      49.31412
    ],
    [
      -123.02552,
      49.31413
    ],
    [
      -123.02566,
      49.31414
    ],
    [
      -123.02629,
      49.31399
    ],
    [
      -123.02688,
      49.31366
    ],
    [
      -123.02722,
      49.31345
    ],
    [
      -123.02774,
      49.31311
    ],
    [
      -123.02773,
      49.31393
    ],
    [
      -123.0277,
      49.31553
    ]
  ],
  "notifications": [],
  "directions": [
    {
      "type": "START",
      "name": "Stewart St",
      "distance": 0.155,
      "time": 15,
      "heading": "WEST",
      "text": "Head west on Stewart St for 150 m (15 seconds)",
      "point": [
        -123.07371,
        49.28424
      ],
      "notifications": [
        {
          "type": "TruckRestriction",
          "message": "Restricted Access - Port of Vancouver"
        }
      ]
    },
    {
      "type": "TURN_RIGHT",
      "name": "Clark Dr",
      "distance": 0.519,
      "time": 50,
      "text": "Turn right onto Clark Dr for 500 m (50 seconds)",
      "point": [
        -123.0758,
        49.28397
      ]
    },
    {
      "type": "TURN_LEFT",
      "name": "Hastings St",
      "distance": 3.337,
      "time": 297,
      "text": "Turn left onto Hastings St for 3.3 km (4 minutes 57 seconds)",
      "point": [
        -123.0771,
        49.28134
      ]
    },
    {
      "type": "TURN_LEFT",
      "name": "Hastings St Onramp",
      "distance": 0.944,
      "time": 106,
      "text": "Turn left onto Hastings St Onramp for 950 m (1 minute 46 seconds)",
      "point": [
        -123.03128,
        49.28114
      ]
    },
    {
      "type": "CONTINUE",
      "name": "Trans-Canada Hwy",
      "distance": 0.262,
      "time": 13,
      "text": "Continue onto Trans-Canada Hwy for 250 m (13 seconds)",
      "point": [
        -123.02843,
        49.28905
      ]
    },
    {
      "type": "CONTINUE",
      "name": "Ironworkers Memorial Bridge",
      "distance": 1.418,
      "time": 73,
      "text": "Continue onto Ironworkers Memorial Bridge for 1.4 km (1 minute 13 seconds)",
      "point": [
        -123.02631,
        49.29096
      ]
    },
    {
      "type": "CONTINUE",
      "name": "Trans-Canada Hwy",
      "distance": 0.665,
      "time": 34,
      "text": "Continue onto Trans-Canada Hwy for 650 m (34 seconds)",
      "point": [
        -123.02661,
        49.30368
      ]
    },
    {
      "type": "CONTINUE",
      "name": "Lillooet Rd Offramp",
      "distance": 0.299,
      "time": 33,
      "text": "Continue onto Lillooet Rd Offramp for 300 m (33 seconds)",
      "point": [
        -123.0278,
        49.30953
      ]
    },
    {
      "type": "CONTINUE",
      "name": "Lillooet Rd",
      "distance": 0.281,
      "time": 52,
      "text": "Continue onto Lillooet Rd for 300 m (52 seconds)",
      "point": [
        -123.02583,
        49.31162
      ],
      "notifications": [
        {
          "type": "TruckRestriction",
          "message": "No Vehicles with a GVW of 30,000 kg or more are allowed to operate or be present on this road"
        }
      ]
    },
    {
      "type": "TURN_LEFT",
      "name": "Old Lillooet Rd",
      "distance": 0.291,
      "time": 36,
      "text": "Turn left onto Old Lillooet Rd for 300 m (36 seconds)",
      "point": [
        -123.02436,
        49.31393
      ]
    },
    {
      "type": "TURN_SHARP_RIGHT",
      "name": "Premier St",
      "distance": 0.269,
      "time": 37,
      "text": "Turn sharp right onto Premier St for 250 m (37 seconds)",
      "point": [
        -123.02774,
        49.31311
      ]
    },
    {
      "type": "FINISH",
      "text": "Finish!",
      "point": [
        -123.0277,
        49.31553
      ]
    }
  ]
}
```


 
## optimalRoute Resource

The optimalRoute resource represents the shortest or fastest route between a start point and a series of end points reordered to minimize total route distance or time. Here are some examples:

1. Shortest optimal route in km and json between the following addresses in Victoria, BC:

1200 Douglas St, 1020 View St, 851 Broughton St, 835 Fisgard St, and 707 Fort St 

https://router.api.gov.bc.ca/optimalRoute.json?criteria=shortest&points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465&roundTrip=false&apikey=myapikey
   
2. Fastest optimal route in km and kml between same addresses as example 1

https://router.api.gov.bc.ca/optimalRoute.kml?criteria=fastest&points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465&roundTrip=false&apikey=myapikey

### HTTP response
The optimalRoute resource will return the following representation:


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
[distanceUnit](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distanceUnit) | String
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | list of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String
[visitOrder](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#visitOrder) | List of Integer
[route](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#route) | List of Point


Here is a sample json response:

    {
	"routeDescription": "",
	"searchTimestamp": "2018-02-20 16:36:39.254",
	"executionTime": 332,
	"routingExecutionTime": 16,
	"optimizationExecutionTime": 313,
	"version": "2.0.0",
	"disclaimer": "http://www2.gov.bc.ca/gov/content/home/disclaimer",
	"privacyStatement": "http://www2.gov.bc.ca/gov/content/home/privacy",
	"copyrightNotice": "Copyright 2015 Province of British Columbia - Open Government License",
	"copyrightLicense": "http://www.data.gov.bc.ca/local/dbc/docs/license/OGL-vbc2.0.pdf",
	"srsCode": 4326,
	"criteria": "fastest",
	"distanceUnit": "km",
	"points": [
		[-123.36517, 48.42545],
		[-123.35587, 48.42445],
		[-123.36057, 48.42323],
		[-123.36002, 48.42915],
		[-123.36479, 48.42455]
	],
	"routeFound": true,
	"distance": 1.916,
	"time": 157,
	"timeText": "2 minutes 37 seconds",
	"visitOrder": [0, 3, 2, 4, 1],
	"route": [
		[-123.36517, 48.42545],
		[-123.36508, 48.42544],
		[-123.36533, 48.42465],
		[-123.36478, 48.42459],
		[-123.36478, 48.42459],
		[-123.36249, 48.42432],
		[-123.36269, 48.42352],
		[-123.36056, 48.42327],
		[-123.36056, 48.42327],
		[-123.35992, 48.42319],
		[-123.35985, 48.42352],
		[-123.35972, 48.42401],
		[-123.3569, 48.42373],
		[-123.35674, 48.42451],
		[-123.35588, 48.42442],
		[-123.35588, 48.42442],
		[-123.35674, 48.42451],
		[-123.35611, 48.42709],
		[-123.35618, 48.42732],
		[-123.35606, 48.42833],
		[-123.35599, 48.42884],
		[-123.35885, 48.42901],
		[-123.35928, 48.42914],
		[-123.36002, 48.42919]
	]
    }

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

https://router.api.gov.bc.ca/optimalDirections.json?criteria=shortest&points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465&roundTrip=false&apikey=myapikey
   
2. Fastest optimal route and directions in km and kml between same addresses as example 1

https://router.api.gov.bc.ca/optimalDirections.kml?criteria=fastest&points=-123.3651694%2C48.4254488%2C-123.3558749%2C48.4244505%2C-123.3605707%2C48.4232329%2C-123.3600244%2C48.4291533%2C-123.3647879%2C48.4245465&roundTrip=false&apikey=myapikey

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
[distanceUnit](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distanceUnit) | String
[points](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#points) | list of Point
[routeFound](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#routeFound) | Boolean
[distance](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#distance) | String
[time](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#time) | Integer
[timeText](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#timeText) | String
[visitOrder](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#visitOrder) | List of Integer
[route](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#route) | List of Point
[directions](https://github.com/bcgov/ols-router/blob/gh-pages/glossary.md#route) | List

Here is a sample json response:

    {
	"routeDescription": "",
	"searchTimestamp": "2018-02-20 16:09:36.298",
	"executionTime": 325,
	"routingExecutionTime": 15,
	"optimizationExecutionTime": 304,
	"version": "2.1.0",
	"disclaimer": "http://www2.gov.bc.ca/gov/content/home/disclaimer",
	"privacyStatement": "http://www2.gov.bc.ca/gov/content/home/privacy",
	"copyrightNotice": "Copyright 2015 Province of British Columbia - Open Government License",
	"copyrightLicense": "http://www.data.gov.bc.ca/local/dbc/docs/license/OGL-vbc2.0.pdf",
	"srsCode": 4326,
	"criteria": "fastest",
	"distanceUnit": "km",
	"points": [
		[-123.36517, 48.42545],
		[-123.35587, 48.42445],
		[-123.36057, 48.42323],
		[-123.36002, 48.42915],
		[-123.36479, 48.42455]
	],
	"routeFound": true,
	"distance": 1.916,
	"time": 157,
	"timeText": "2 minutes 37 seconds",
	"visitOrder": [0, 3, 2, 4, 1],
	"route": [
		[-123.36517, 48.42545],
		[-123.36508, 48.42544],
		[-123.36533, 48.42465],
		[-123.36478, 48.42459],
		[-123.36478, 48.42459],
		[-123.36249, 48.42432],
		[-123.36269, 48.42352],
		[-123.36056, 48.42327],
		[-123.36056, 48.42327],
		[-123.35992, 48.42319],
		[-123.35985, 48.42352],
		[-123.35972, 48.42401],
		[-123.3569, 48.42373],
		[-123.35674, 48.42451],
		[-123.35588, 48.42442],
		[-123.35588, 48.42442],
		[-123.35674, 48.42451],
		[-123.35611, 48.42709],
		[-123.35618, 48.42732],
		[-123.35606, 48.42833],
		[-123.35599, 48.42884],
		[-123.35885, 48.42901],
		[-123.35928, 48.42914],
		[-123.36002, 48.42919]
	],
	"directions": [{
		"text": "Continue onto View St for 7 m (0 seconds)",
		"point": [-123.36517, 48.42545]
	}, {
		"text": "Turn right onto Douglas St for 90 m (6 seconds)",
		"point": [-123.36508, 48.42544]
	}, {
		"text": "Turn left onto Fort St for 40 m (3 seconds)",
		"point": [-123.36533, 48.42465]
	}, {
		"text": "Stopover 1",
		"point": [-123.36478, 48.42459]
	}, {
		"text": "Continue onto Fort St for 150 m (15 seconds)",
		"point": [-123.36478, 48.42459]
	}, {
		"text": "Turn right onto Blanshard St for 90 m (6 seconds)",
		"point": [-123.36249, 48.42432]
	}, {
		"text": "Turn left onto Broughton St for 150 m (14 seconds)",
		"point": [-123.36269, 48.42352]
	}, {
		"text": "Stopover 2",
		"point": [-123.36056, 48.42327]
	}, {
		"text": "Continue onto Broughton St for 50 m (4 seconds)",
		"point": [-123.36056, 48.42327]
	}, {
		"text": "Turn left onto Quadra St for 95 m (11 seconds)",
		"point": [-123.35992, 48.42319]
	}, {
		"text": "Turn right onto Fort St for 200 m (15 seconds)",
		"point": [-123.35972, 48.42401]
	}, {
		"text": "Turn left onto Vancouver St for 90 m (6 seconds)",
		"point": [-123.3569, 48.42373]
	}, {
		"text": "Turn right onto View St for 65 m (5 seconds)",
		"point": [-123.35674, 48.42451]
	}, {
		"text": "Stopover 3",
		"point": [-123.35588, 48.42442]
	}, {
		"text": "Continue onto View St for 65 m (5 seconds)",
		"point": [-123.35588, 48.42442]
	}, {
		"text": "Turn right onto Vancouver St for 500 m (39 seconds)",
		"point": [-123.35674, 48.42451]
	}, {
		"text": "Turn left onto Balmoral Rd for 200 m (15 seconds)",
		"point": [-123.35599, 48.42884]
	}, {
		"text": "Turn slight right onto Fisgard St for 90 m (6 seconds)",
		"point": [-123.35885, 48.42901]
	}, {
		"text": "Finish!",
		"point": [-123.36002, 48.42919]
	}]
    }
