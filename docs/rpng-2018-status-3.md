# RouterNG, Status Report #3
Date: 2018-07-26<br>
Sprint 1<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.*
## Overview
We are now approaching the end of the third week of our five-week first sprint. The basic Router NG implementation has been extended to accommodate unwanted classes and flow patterns. Beyond the data filters, several built-in functions from GraphHopper (e.g. directions) and extended features (e.g. round trip, optimize order, nearest stop) needed to be ported/reimplemented. At this time, the functionality of the Router NG application now matches that of the Route Planner in production.
A variety of testing has been performed, with comparisons showing very minimal differences against production results. A working version of the code has been released onto our server; this is available for the team to start testing. See https://ols-demo.apps.gov.bc.ca/index.html?env=rri
Features in progress include intersection costs and time independent turn restrictions. These are being modelled in a combined framework, with the costs pre-calculated to minimize on-demand processing for the routing algorithm. Next steps are to continue with the intersection cost model, then design a means to add the concept of time-dependency to route queries. Because the new functionality is now pushing beyond what is supported by the production route planner, we will likely switch over to using Google as our target reference for automated testing.
There is a bit of an inter-holiday window in early August coinciding with the end of the first sprint. I suggest that we aim to have a team meeting around Aug 9th or 10th to review work progress and testing results. Hopefully that can be set up in my absence (I’ll be back on Tuesday Aug. 6).
## Details
### Basic Implementation
The basic Dijkstra-based router now supports direction of travel, road-class filters (e.g. ignoring trails), ignoring virtual segments, preventing unwanted flow (e.g. overpass/underpass and ferry “crossings”), and using speed limits for travel time estimates. These were coded/modelled to match the logic in the production route planner.
Support for basic directions needed to be designed from scratch, as this used to be handled through GraphHopper. It will require review and future fine-tuning to suit our needs but is sufficient for now. We also extended the graph and query framework to support multi-point routes. This allowed the functionality of round trip, optimize order and nearest stop to be reworked into the new code base.
Status Report #3 Page 2 of 3
### Intersection Cost Modelling
The first functionality to get extended beyond what is in production relates to
intersection costs. Our initial modelling approach has involved creating three
groups of ITN road classes with rough estimates of intersection time delays
for various combinations. The Router Data Inputs Google Doc now has a
spreadsheet tab called Impactor Costs with a matrix of base values to use.
Related to the intersection impactor delays are time-independent turn
restrictions as these are akin to an infinite cost/delay. To support both of
these concepts, we have designed a CSV cost file with an input format
capable of modelling various restrictions. The structure consists of a unique
ID, pipe-separated EDGE_NODE_SET (e.g. 398768|1931626|398770),
DAY_TIME_CODE (e.g. SS-24), TRAVERSAL_COST (e.g. 20 seconds) and a
DESCRIPTION. The proposed format is flexible enough to also support
extended restrictions (e.g. edge|node|edge|node|edge) such as U-turn
cases.
A router preprocessing step will be designed to generate the cost file from
two sources. The first input is ITN, whereby segment/intersection mappings
need to be converted from the left/centre/right turn restrictions. ITN will
also provide contributions from end-node impactors that affect flow (e.g.
yield, stop sign, light, roundabout). The traversal cost values will be
calculated from the street configuration matched with the matrix of delay
entries. The second input will be a custom restriction input file with an
identical structure to the processed cost file. This custom file will contain
restriction cases to supplement those in ITN, e.g. independently generated
U-turns, missing impactors, additional turn restrictions.
On startup, RouterNG will read the cost file and map the values directly into
the graph. Because we have pre-processed intersection delay costs, queries
will not have to perform additional calculations on-the-fly to evaluate
complicated timing formulas. Note that in the future once traffic
flow/impedance functionality is modelled, real-time segment traversal
estimates will likely be preferable to flat intersection delay costs. Default
delays would be replaced by more accurate travel time information.
### Testing
A set of semi-random test routes was generated from the geocoder site input
file. For each of the ~800 localities containing at least one site, a single
record was selected. These were then paired into ~400 routes and loaded
into the testing framework database. As a result, the FME bulk testing script
has been able to not only run with custom routes, but also against the
generated cases. Leo was helpful in upgrading our API Key once we
exceeded our request limit.
All testing to date has been done by comparing shortest route results from
the new Router NG code against production. Early on, several minor issues
were detected (mostly edge cases) and fixed after a few release iterations.
For successful route cases, the current build total distance results now match
those in production within 0.5%. The only exceptions are from isolated
start/end point cases that are some distance from any ITN segment. Our
new code sometimes applies a longer reach than production when trying to
Status Report #3 Page 3 of 3
locate the closest nearby segment. When this happens a route that
previously would be flagged as an error actually returns with a result.
Performance remains generally fast, with shorter routes (under 100km)
usually being calculated in single-digit milliseconds. The full 400+ test cases
are averaging under 200ms, with none exceeding half a second. Slow cases
are expected to get worse as the intersection cost model is applied, but for
now there is no real impact on our automated testing or development
iterations.
