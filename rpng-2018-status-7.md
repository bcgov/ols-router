# RouterNG, Status Report #7
Date: 2018-10-10 <br>
Sprint: 3<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.*
# Overview
The main focus for the past couple of weeks continued to be in making improvements and adding features to bring the application into a demo-ready state. In that timeframe, two software releases to our development environment were deployed and tested (versions 7 and 8).
The following items have been completed since the last report:
* Extensions to the testing framework
* Fixes to correct-side routing
* Separation of visualization layers
* Support for length- and angle-based turn restrictions
* Integration of event-based functionality and Open511 parsing
* Process and load of Oct ITN data
* Generation of new divided-end turn restrictions
* Support for Brian re: Geocoder data refresh
Traffic congestion flow modelling is the last major feature scheduled to be implemented this sprint. Work on this feature is just getting underway.
Overall, we have progressed faster than expected. It is possible that we will complete all major items as well as a few unscheduled high priority additions ahead of schedule. If all goes well this week, that could leave a window of up to a week for either final demo tweaks (e.g. adding waypoint drag & drop) or for getting an early start on the next sprint. Either way, a team meeting next week would probably be timely.
## Details
### Improvements to Existing Functionality
In terms of the testing approach, some of the tabular information on the PHP screens was extended to assist with review of routes. This, along with the addition of new test cases, led to discovering a bug with correct-side routing. The problem sometimes caused incorrect results to be interpolated the start and/or end route segments. The issue has now been fixed.
For the visualization tools, there are now several layers displaying generated icons that a user can turn on and off. Additional layers will continue to be required (e.g. event icons, traffic theming), and the original one-off approach to accommodate these was becoming less manageable. To standardize the layer organization and simplify the addition process, Chris reworked parts of the code. Now each visual layer data structure is isolated into its own object
class, completely separate from any required to generate route information.
Much of the conversion has now been completed, though there may be some
additional minor refinements in the future.
### Commercial Vehicle Length Restriction
Length- and angle-based turn-restrictions are now supported in the latest
development release. This is the last high priority MoT feature identified for
development this fiscal, though it has only been implemented in a fairly
simple manner.
When an input truck length exceeds a threshold (12.5m), then routes are no
longer permitted to traverse through intersection angles less of than 30
degrees. There are other more complex restrictions related to long vehicles
(s-curves, turns onto steep ramps) however at this time there is insufficient
data (e.g. 3D geometry, MoT ramp-classes) to support any further
extensions.
### Events
Another new feature added and supported in the latest v8 release is based
on Open511 events. A JSON file with current and future events can be
acquired from a call to the MoT DriveBC Open511 API. This is like taking a
snapshot of the event feed that is displayed on their webmaps and in the
online data tables. The RouterNG software is now capable of reading and
parsing a complete file export, including recognizing all Open511
enumerations.
While it can ingest the full file, the current implementation only handles
effects based on a small subset of event types:
* status = ACTIVE
* event_type = CONSTRUCTION or INCIDENT
* direction = BOTH
* state = CLOSED
* time period = recurring or interval
* geography: type Point
Events are time-dependent so a relevant start time is required to trigger the
effects. The visual layer in the UI can be turned on and off, and the icons
are based on those used in the DriveBC system.
Enhancements for handling other event types, states, geography types, etc.
are on the radar but not yet scheduled. There is an additional tuning
improvement that would also be worth considering once scheduling support
has been properly modelled (targeted for next sprint). Currently the routing
algorithm does not yet support time delays (akin to a ferry wait). So if a
potentially fastest path tries to traverse a segment during an event closure
period, it then considers this path completely invalid based on the user’s
start time. In the future, a better approach would be to determine if waiting
until the next opening window for that segment would produce a faster path
than using an alternate route.
### Other
The recently released October ITN data has now been processed and loaded
into the router. A few of the test cases are now more realistic based on
tickets and fixes completed by GeoBC. Also, the ITN has incorporated a Road
Yield Lane class has been added as a new road type based on our feedback.
For now nothing special is being done with these features other than
permitting routes to path on them.
As a companion to the road base updates, Graeme wrote another FME script
to detect cases where a divided road converges back to a single line
representation. When the names and angles are suitable in 3-valent and 5-
valent cases, a generated turn restriction entry is created. This restriction
effectively prevents a route backtracking up the opposite divided edge
(similar to a u-turn case). An additional 830 restrictions have been added to
the route planner (accommodated as a new input file read at startup). A few
of our test cases were impacted by this change, in a positive (more realistic)
way.
Outside of RouterNG activities, a small effort was spent offering support for
Brian. We provided answers to a few issues and minor script fixes necessary
to facilitate Geocoder data refresh processing in DataBC’s environment.
### Next Steps
Traffic congestion flow modelling is the last major feature scheduled to be
implemented this sprint and is just underway. Once this is complete there
are a few UI tweaks that can be tackled to support the demo, such as waypoint
drag & drop. Optionally some effort could be spent modelling/tweaking
the most significant areas where routes could be made more realistic (e.g.
slight delay for each right/left turn).
With the sprint nearing an end, it is a good time to show off to the team
what has been accomplished and to plan out our goals for the next work
phase.
