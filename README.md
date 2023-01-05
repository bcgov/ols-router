# BC Route Planner
BC Route Planner is an open source route planner with support for time-dependent routing and truck routing on the BC Integrated Transportation Network. [Source code](https://github.com/bcgov/ols-router) is licensed under [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0). BC Route Planner version 2.1.7 was released on October 11, 2022. To see it in use by an application, visit [Location Services in Action](https://ols-demo.apps.gov.bc.ca/index.html).

The BC Route Planner was developed by the Province of British Columbia. 

In year one (ending March 2019), we investigated the feasibility of supporting multiple complex routing constraints such as time-dependent routing including historic traffic congestion, scheduled road events and construction, and ferry schedules. The first release includes components for which there is insufficient or no real data so that a stable API is available for developers sooner than later. Such components are turned off by default. Creating and managing data sources is out of scope for this project. Getting data custodians to put usable data APIs in place was not expected in year one. The performance goal of the Route Planner is under two seconds execution time for a single, two-point route.

In year two (ending March 2020), we added support for truck routing which takes vehicle and load dimensions and road restrictions into account. This version of the BC Route Planner is now incorporated into the [TransLink Route Planner](https://translink.apps.gov.bc.ca/trp/) which is an interactive, easy to understand, trip-planning application for goods movement in Metro Vancouver that improves on existing regional way-finding and trip planning tools.

In year three (ending March 2021), we added support for vehicle-type specific turn restrictions and timed notifications. A timed notification is a notice that has an effective time period. During this period, the notice is displayed on the appropriate leg of the best route in turn-by-turn directions. A notification doesn't affect the computation of best route.

Details on recent and future releases can be found on the [BC Route Planner What's New page](https://www2.gov.bc.ca/gov/content?id=51364680561947AA9372AF1817EC2ACD), the [BC Route Planner release notes](rpng-release-notes.md) or the [BC Route Planner Roadmap](https://github.com/bcgov/ols-router/blob/gh-pages/route-planner-roadmap.md).


[Route Planner 2.1.7 in action](https://ols-demo.apps.gov.bc.ca/index.html)

[Route Planner release notes](rpng-release-notes.md)

[Route Planner NG Acceptance Test Plan](rpng-atp.md)

[Draft Integrated Transportation Network Data Model Gaps](ITN-Data-Mode-Gaps.md)

[Draft Integrated Transportation Network Data Issues](itn-data-issues.md)

[Current milestones for this year](https://github.com/bcgov/ols-router/milestones)

[Promising routing algorithms](https://github.com/bcgov/ols-router/issues/25)

[Comparison between ITN and OSM](osm-itn-stats.md)

## Phase 1 System Architecture
The BC Route Planner is a web service (aka API) running behind Kong, our API Gateway. Kong is an NGINX plugin that provides security and access control, metering, and load balancing. Multiple route planner nodes are deployed across multiple data centres to ensure service is not interrupted during scheduled maintenance or data centre failure.

On startup, a Route Planner node reads the latest static road network into a cached road network. Route Planner nodes will be restarted monthly when updated ITN data is received and prepared for use. Historic traffic congestion, road events, and ferry schedules are all demonstration data designed solely for proof-of-concept.

![](BC-RPNG-Phase-1-Architecture.png)

## Draft Phase 2 System Architecture
In phase 2, the system architecture will be enhanced to support real-time changes to BC's road network.

On startup, the Road Watcher will read in the latest static road network then read in the latest events from real-time APIs on a periodic schedule and assign them to the appropriate segments in the network. It will then convert the integrated events into a form that is easily digestable by the Route Planner and write them out to a shared datastore that is accessible to all Route Planner nodes.  Individual Route Planner nodes will pull events from the shared datastore on a periodic schedule. 

The Road Watcher will be restarted on a schedule that keeps up with new road construction and changes to road signs and traffic controls.

![](BC-RPNG-Phase-2-Architecture.png)
