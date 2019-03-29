# RouterNG, Status Report #2
Date: 2018-07-13<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.*
## Overview
Work continues on researching modern algorithms and optimization techniques. A feasibility meeting was held with the team to propose a two-pronged approach for the next steps. There was agreement that the project should proceed as follows:
* Develop a simple Dijkstra-based routing algorithm that would support all of the desired routing features except optimal performance.
* Continue evaluating algorithms that can improve performance
Since that meeting we have already implemented a basic routing framework upon which each feature can be developed and tested.
We have also created a work plan spreadsheet to detail tasks for the first five-week “sprint” running from July 5 – August 10. The implementation and testing sections below describe our progress over the last 6 business days.
## Details

### Feasibility
Through our continued research, several candidate algorithms have been identified. It is not yet obvious how suitable their implementations would be in supporting all the desired features for the project. Details about various approaches along with their potential strengths and weaknesses can be found in the Router NG Feasibility document circulated to the team. This Google document formed the basis for the feasibility meeting. It also presents the two accepted recommendations (see above in the overview) for proceeding with the next phase of the project.
A suggestion that came out of the meeting was to aim for a second feasibility meeting in the fall. At that time we would evaluate the status of our basic feature-full router, candidate data sources, and effectiveness of any speed-up routines to determine if the project goals could still be met.
Basic Implementation
Chris has created a Java-based framework for the application. One goal is for the code to be compatible with Open Java. This may require very minor modifications to be applied to existing code.
In our first week of the sprint we have been able to develop a basic graph model and populate it with BC ITN data. Progress has also been made on implementing a Dijkstra-based router that supports direction of travel, road-class filters (e.g. ignoring trails), ignoring virtual segments, preventing
unwanted flow (e.g. overpass/underpass and ferry “crossings”), and using
speed limits for travel time estimates.
In our development environment, the Location Services In Action web UI
framework now has capabilities to interact with the new router code to
visually display paths, accept (or geocode) input points, etc. Some of the
built-in Graphopper functions (e.g. directions) need to be reworked into our
application. Once directions and the addition of support for multi-point
routes is complete (next week), this will for the most part replicate the
features of the current route planner in production.
### Testing
An FME script was created to subset the router GeoJSON files based on a
custom polygon shapefile (e.g. to create a small testing zone or multiple
corridors). This will be useful for verifying localized effects such as traffic
congestion, and to significantly speed up response/calculation times.
Currently the application is fast enough (seconds) to initialize and load the
entire ITN dataset. Cross-BC route queries are also very quick (milliseconds)
in the basic implementation, so using a reduced dataset is not yet necessary.
A second FME script has been created to facilitate bulk testing. Several
PostGIS database tables were designed to record test routes, environments,
data inputs and the results. Response information such as processing time,
distance, etc. are recorded to the database.
The concept of the bulk testing is to have a repertoire of candidate routes
that can be run against our development environment, then compared with
the same routes calculated in delivery/test/production instances and Google.
These records can be used to observe effects of feature implementation, and
as a comparison against reference results. As speed-up routines are
implemented, checks against previous run results will assist with validating
the approach. Our initial route cases have already demonstrated
improvements as Chris creates new functionality.
### Project Management
We are proceeding with an Agile management strategy, which lends itself
well to projects where there is a fluidity to the work ahead. Tasks for a first
sprint have been identified in a Google spreadsheet shared with Heidi. This
will be a useful tool for tracking progress, assigning resources and estimating
costs. We are just starting week two of five for the sprint.
The team currently has high availability, so we are proceeding such that
maximal progress can be made. The limiting factor is Chris’ time, so we are
looking for any opportunities to offload work from him (e.g. setting up the
test framework) to Graeme, Darrin or others. This has been very effective so
far. However, it should be noted that there is not sufficient budget (or later
on availability) to assign Chris full time work continuously until the end of
fiscal. Planning for future sprints will have to take this into account.
