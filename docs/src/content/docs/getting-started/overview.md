---
title: Overview
description: Overview of the BC Route Planner.
---

## Phase 1 System Architecture
The BC Route Planner is a web service (aka API) running behind Kong, our API Gateway. Kong is an NGINX plugin that provides security and access control, metering, and load balancing. Multiple route planner nodes are deployed across multiple data centres to ensure service is not interrupted during scheduled maintenance or data centre failure.

On startup, a Route Planner node reads the latest static road network into a cached road network. Route Planner nodes will be restarted monthly when updated ITN data is received and prepared for use. Historic traffic congestion, road events, and ferry schedules are all demonstration data designed solely for proof-of-concept.

![Phase 1 Architecture](/ols-router/BC-RPNG-Phase-1-Architecture.png)

## Draft Phase 2 System Architecture
In phase 2, the system architecture will be enhanced to support real-time changes to BC's road network.

On startup, the Road Watcher will read in the latest static road network then read in the latest events from real-time APIs on a periodic schedule and assign them to the appropriate segments in the network. It will then convert the integrated events into a form that is easily digestable by the Route Planner and write them out to a shared datastore that is accessible to all Route Planner nodes. Individual Route Planner nodes will pull events from the shared datastore on a periodic schedule.

The Road Watcher will be restarted on a schedule that keeps up with new road construction and changes to road signs and traffic controls.

![Phase 2 Architecture](/ols-router/BC-RPNG-Phase-2-Architecture.png)
