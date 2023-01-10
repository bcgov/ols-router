# Route Planner NG Acceptance Test Plan

## Background
Route Planner Next Generation is a three year project to develop an open source route planner capable of time-dependent routing and commercial vehicle routing.

- Version 2.0, due for release in May, 2019 will support time-dependent routing. 
- Version 2.1 will support truck routing for use in height clearance applications.
- Version 3.0 will support truck routing as needed in core truck permitting.
- Version 4.0 will support ferry schedules, real-time road events, and truck routing as needed in advanced truck permitting.

## Purpose
This document defines the tools, methods, and approach to be used in acceptance testing the Route Planner Next Generation.

## Components to be Tested
1. Route Planner is an online API for route planning
2. Route Planner Administration Application
3. Routable BC Maker is an internal application used to take source data such as the ITN and enhanced turn-restrictions and automatically generate input datasets for the Route Planner.

## Test Tools

### Logging and tracking issues

GitHub and ZenHub are to be used for issue logging and tracking

### Defining, storing, and executing test cases and analysing results

- The *Route Planner Benchmark Route List* contains a collection of routing test cases. 

- The *Router Test Script* (python) will be used to process the *Route Planner Benchmark Route List*.

- The [Location Services in Action application](https://bcgov.github.io/ols-devkit/ols-demo/index.html?) will be used for interactive router validation.

## Test Planning Process

Route planning test cases will be stored in the *Route Planner Benchmark Route List*.

Define test cases for each feature of the router including:
 - correct-side routing
 - time-independent turn restrictions
 - time-dependent turn restrictions
 - truck-dimension based trun restrictions
 - start-time/date
 - turn costs
 - designated truck routes
 - scheduled road events
 - historic traffic congestion
 - hard road restrictions (e.g.,height,weight, width)
 - ferry schedules
 - multiple waypoints
 - route optimization

Define route planning test cases for various geographic scenarios including:
 - urban areas (high road density)
 - rural areas (low road density)
 - by geographic region (e.g., lower mainland, Vancouver Island, NW BC, SE BC, NE BC, Prince George area)
 - short distances
 - long distances

Define Route Planner Administration Application tests including:
 - export/import configuration values
 - reconfigure and reboot route planner


## Testing Process

### Preconditions

  * Route Planner code has been deployed

  * Test tools are configured and ready to access route planner on delivery and test platforms

  * Route planner tests run with no major issues (system test)
  
  * Route Planner Admin Application tests executed manually with no major issues (system test)

### Steps

1. On Test platform, use Routable BC Maker to prepare a routable road network using the latest ITN and turn-restrictions.

2. Run route planner tests in the test environment

3. Manually test the route planner using the Location Services in Action application

4. Manually test the Route Planner Administration application in the test environment


### Acceptance

The Ministry will accept the system if all acceptance test cases pass.
