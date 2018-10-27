# BC Route Planner NG
BC Route Planner NG is a three year project of the Province of British Columbia to develop a state-of-the art route planner. The BC Route Planner NG is a vital component of the free and open DataBC Location Services Platform.

In year 1 (2018-19), we will investigate the feasibility of supporting multiple complex routing constraints such as time-dependent routing including historic traffic congestion, road incidents and closures, road construction, and ferry schedules. The first release will not exclude those components for which there is insufficient or no real data so that a stable API will be available for developers sooner than later. Creating and managing data sources is out of scope of this project. Getting data custodians to get usable data APIs in place is not expected for many months. The performance goal of the first release of RPNG is under two seconds execution time for a single, two-point route for an oversized truck.

In year 2 (2019-2020), we will add support for truck routing, a road watcher and new data sources. Truck routing takes vehicle dimensions and road restrictions into account. The road watcher will keep the cached road network in-synch with real-time events like accidents, wildfire, and mudslides.

In Year 3 (2020-2021), we will focus on incorporating user-requested enhancements, making routes more realistic, and improving performance.

[Current milestones for this year](https://github.com/bcgov/ols-router/milestones)

[Draft year 1 Route Planner release notes](https://github.com/bcgov/ols-router/issues/75)

[Draft Integrated Transportation Network Data Model Gaps](https://github.com/bcgov/ols-router/blob/master/docs/ITN-Data-Mode-Gaps.md)

## Draft Phase 1 System Architecture
The BC Route Planner NG is a web service (aka API) running behind Kong, our API Gateway. Kong is an NGINX plugin that provides security and access control, metering, and load balancing. Multiple route planner nodes will be deployed across multiple data centres to ensure service is not interrupted during scheduled maintenance or data centre failure.

On startup, a Route Planner node will read the latest static road network into a cached road network. Route Planner nodes will be restarted monthly when updated ITN data is received and prepared for use. Historic traffic congestion, road events, and ferry schedules are all cooked data designed solely for proof-of-concept.

![](https://github.com/bcgov/ols-router/blob/master/docs/BC-RPNG-Phase-1-Architecture.png)

## Draft Phase 2 System Architecture
In phase 2, the system architecture will be enhanced to support real-time changes to BC's road network.

On startup, the Road Watcher will read in the latest static road network into a cached, live road network. The Road Watcher will then update the live road network from real-time APIs on a periodic schedule (e.g., every five minutes). The Road Watcher will be restarted on a schedule that keeps up with new road construction and changes to road signs and traffic controls (e.g., every week). A candidate real-time database management system to manage the live road network is [RethinkDb](https://www.rethinkdb.com/)

![](https://github.com/bcgov/ols-router/blob/master/docs/BC%20RPNG-Phase-2-Architecture.png)
