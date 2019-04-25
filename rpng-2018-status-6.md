# RouterNG, Status Report #6
Date: 2018-09-20<br>
Sprint: 2<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.*
## Overview
Over the past two weeks, there has been very significant progress in implementing new features. The main areas of improvement include: extending the testing framework, supporting time dependent restrictions, preventing u-turns at generated locations, adding same-side routing and implementing initial MoT restriction logic.
For testing, thousands of additional random routes have been generated for Victoria by pairing every ITN segment in the locality. Additionally, the structure of the testing database has been extended to accommodate groupings of route types. Finally, there has also been the creation of a user interface to inspect, compare and view the results of any test runs. This tool has already proven to be extremely valuable as it internalizes all of the manual database queries previously required, and it displays comparison route results in a web map. It has been released in a beta version, so it will be extended over time. Please check it out at http://office.refractions.net/~chodgson/gc/test/
Other major areas of progress involve the completion of time-dependent routing support. This includes enhancements to the visualizer (purple restriction icons) and the addition of user input fields for time & date. We determined that changing to a one-way graph model was necessary to properly model edge costs in each direction. These modifications are now complete, and have the added bonus of providing correct-side routing functionality for free.
The end of the second sprint culminated with a team meeting last week to determine priorities for work. We were able to identify the most important features for this fiscal, as well as setting a goal for the new sprint of creating a demo that can highlight some of the MoT requirements. This third sprint runs from Sept 17 – Oct. 19. The plan is very ambitious, but with the first week now finished we have already made good progress on the first two features: hard restriction support (height/weight/width) and length-based restrictions.
## Details
### Testing
A new FME script was created to filter and then randomly pair all streets within a single locality. From these pairings, routes were created based on the segment midpoints. The intention was to build up a more comprehensive set of test cases that would be forced to route on at least part of every segment in a more localized area. The previous cross-province locality
Status Report #6 Page 2 of 4
pairing tests remain useful for high level analysis and common corridor
testing, while the locality-intensive approach is much more suitable for
denser route coverage.
The database framework was also extended to include a test group name so
that runs can be performed on specific categories of cases instead of all
cases (e.g. custom, local pairings, regional pairings). The FME script noted
above was run for Victoria, and resulted in over one thousand new test route
cases. As we build further samples, the grouping enhancement will simplify
the testing against specific categories of cases. We could also consider
selecting other localities in which to generate more clustered test routes.
At our team meeting we discussed the usefulness of having a more
automated means to query, compare and view results of test runs. Chris has
created a basic PHP web UI that displays a table of each run, along with a
button to compare two different runs. Results are displayed in tabular
format (-1s indicate no result) with a link to view on a map. The first
candidate dataset is shown in red, and the second is in yellow (overlaps show
as orange). Clicking on a route brings up some basic info for that run.
Overall, this is a basic framework that could be extended and made more
visually appealing in the future if that is considered a reasonable use of
budget and time.
### Turn Restrictions
In order to implement time dependent turn restrictions, day and time entry
fields were first added to the UI. Then the route walking logic was updated
to track the time at each segment so that turns with restrictions within a
time window could be handled properly. Due to the lack of duration
granularity in ITN, we have mapped their morning and afternoon rush hour
ranges to fixed time periods: 7:00am – 9:00am and 4:00pm – 6:00pm.
The Navigation Info layer in the visualizer was extended to also show timedependent
turn restriction cases using purple icons, as well as u-turns. Logic
for handling multi-segment restrictions (e.g. u-turns:
edge/node/edge/node/edge) is now in place.
With regards to the data, there are likely several additional restriction cases
that can be generated from configurations in ITN data (e.g. double line to
single line transitions). The priority of further analysis of such cases will
depend on how much they negatively impact routing realism. Also, GeoBC
has started to adopt a new transport line type called right yield lane. This
should be available on some segments in their October data release. It is
likely that we will be able to refine our handling (restricting) of traffic flow off
of these features.
### Correct Side Routing
The internal graph structure previously modelled segments representing twoway
traffic flow as single bi-directional edge. In order to properly reference
different edge costs in each direction, we determined that this could be
accommodated by modifying the graph structure. This modification is now
complete, resulting in each edge only representing a single direction of travel
between two nodes. Many parts of the code had to be tweaked to support
this change. Beyond special case simplification, an additional benefit is that we now have
support for correct side routing. There is a new check box in the UI to
enable this feature, which ensures that route properly starts and ends on the
correct block face. One downside of the change is that the performance has
been reduced as there are significantly more edges in the graph (almost
doubled). As a result of the network expansion, route calculation times have
typically increased by 50% - 75%.
### Hard Restrictions
The top priority feature identified by MoT was hard restrictions. This is now
supported in the latest release, and can be observed through a new tick box
in the UI called Truck Options. This allows inputs of numerical values for
truck dimensions: height, weight and width. The ITN data contains attributes
representing posted maximum limits for these dimensions on each segment’s
from and to ends. We have remodelled these end pairs as a single value
representing the restrictions to the segment as a whole. Note that currently
there are no width restrictions recorded in ITN.
The second feature we have started to implement is also based on a truck
dimension: its length. This will require an additional input box in the UI and
logic to identify configurations in ITN that would restrict long vehicle
movement. MoT has provided two scenarios that are based on turn angles
and road gradient. Until we have a 3D network, we are limited to restricting
trucks over a certain length from performing overly tight turns.
The visualizer has been extended to include two new layers: Hard
Restrictions and Internal Segment IDs. The former allows for display of icons
representing width/weight/height restrictions in ITN. The internal segment
IDs are a debugging layer as these are the internal graph IDs used in our
model.
