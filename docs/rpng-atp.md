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

The Route Planner Test Framework will be the primary tool for defining, storing, and executing test cases, and analysing test results.
The Brython Router Probe will also be used for additional testing as needed.
The Location Services in Action application can be used for interactive router validation and bash testing.

## Testing Process
