# RouterNG, Status Report #9
Date: 2018-11-16<br>
Sprint: 4<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.*

## Overview
We have completed almost four weeks of sprint 4 activities, with two more weeks scheduled before the end of sprint. The team has recently had more availability and has completed several enhancements related to ferry scheduling.
Ferry logic is now in place to support crossing times, minimum wait delays and arrival & departure timings. Currently only a limited number of routes have been modelled with detailed data covering scheduled timings. The remaining routes will still have delay and crossing times applied but no departure information. If a user chooses to disable the Ferry Schedules feature then only the crossing times are used for route calculation.
To help visualize the ferry functionality, a new layer has been added which shows icons on the map at each port. Clicking on these results in basic information about the ferry route as well as a table of the upcoming departures (if that data is available). Also, the directions have been slightly restructured to include notifications and icons. The latest release (Version 10) is live with these features and ready for the team to test. An additional improvement included with this build is a bug fix to the core routing algorithm which only occurred in rare cases.
For the remainder of the sprint we will work to complete two more features. The first involves extending the recently created concepts of ferry delay and windows of active travel time to also apply for events. Once that is done, we will convert all of the FME prep scripts that generate implicit turn restrictions into equivalent Java routines suitable for the router pre-processing application.
Heidi has scheduled a team meeting for Dec. 4 when we can demonstrate the new features and plan activities for sprint 5. The terms Routable BC and Routable BC Maker have been coined, so we can also discuss what these concepts mean.
## Details
Logic was added to the pre-processing application (Routable BC Maker) to group together same-named ITN ferry segments into a single port-to-port logical graph edge. This was done to provide a simpler framework upon which we can associate initial delays, apply end-to-end crossing times (and interpolate mid-crossing starts), and link to input files that model individual ferry routes.
Cases that have no port at one end of a route (e.g. sailing out-of-province) are now dropped from the router input. Because the Namu port in the Inside Passage does not currently have a road segment in ITN, the ferries into and
out of this location are also dropped (Bella Coola-Namu Ferry and Port
Hardy-Namu Ferry). Michael is going to determine if this is still an active
port (there is no active mention of Namu on BC Ferries sites) as the ITN may
need to be updated in this area. Conversely there also may be other areas
(e.g. Gulf Islands) where some of the direct island-to-island links are not
currently modelled in ITN.
In terms of modelling the schedule and crossing data, the goal is to support
inputs that meet the GTFS specification. Since there is no current feed in
this format from BC Ferries (estimated availability is 2020) we have hand
created regular sailing times on the following routes:
* Tsawwassen to Swartz Bay & Swartz Bay to Tsawwassen
* Tsawwassen to Duke Point & Duke Point to Tsawwassen
* Horseshoe Bay to Departure Bay & Departure Bay to Horseshoe Bay
* Brentwood Bay to Mill Bay & Mill Bay to Brentwood Bay
These cases are sufficient for testing, however without adjacent routes being
similarly scheduled can have unusual effects due to incomplete data. These
oddities occur around the Gulf Island ferries as there is an alternative islandhopping
multi-leg route between Swartz Bay to Tsawwassen. Since we do
not yet have scheduled data for these paths, the routing algorithm may
(correctly based on all available data) choose the milk run depending on
timings and departures on the direct route between Swartz Bay and
Tsawwassen. Note that there is also no restriction on when unscheduled
ferries run, so 2am trips on these legs are valid.
The first scheduling feature applied against the logical edges is the concept of
a minimum lead time to reach the ticket gate before a departure. In the
router, this is factored into arrival time calculations at the start of the ferry
edge (not necessarily the exact gate location) and has been associated for all
ferry crossings in ITN. The duration can vary from route to route and is
typically between 2 and 10 minutes. Because this is not part of the GTFS
specification, we are modelling this extra attribute information in a separate
file.
Also in the ancillary file is an attribute we’ve created for the sailing time on
each route. This value (based on what is posted by BC Ferries) provides a
much better measure for duration of the leg than simply using the constant
5km/h boat speed limit in ITN. A third support attribute is a flag as to
whether a ferry is scheduled or on-demand. For GTFS-modelled routes this
is not used, however for other routes we can add an additional delay as we
don’t know any departure times. This delay simply models extra time likely
needed to wait for a boat to arrive (instead of just instantly driving onto a
ferry). For on-demand routes the additional wait value is set to the crossing
time, and for schedulable routes it is set at half the crossing time.
So far all of these new scheduling and delay features seem to be standing up
to testing in areas where we have complete data inputs. As noted earlier,
alternative routes for which we don’t yet have any scheduling information are
sometimes selected instead of main routes. However, once we have a
complete set of GTFS inputs for all scheduled routes, the functionality should
produce the expected outcomes. If BC Ferries is willing to provide us with an
export of their scheduling data, it is possible we could develop a means to
convert it into GTFS format a couple of years earlier than they have
suggested.
To facilitate testing and demonstrations, a Ferry Schedules layer was added
to the Location Services In Action web app. MoT Open511 ferry icons are
shown at each port. Clicking on them pops up additional information about
route name, ferry type, crossing time. If available, it also displays a table of
scheduled departure times in the 24 hours following the currently selected
start date/time in the route options. The directions panel has also been
tweaked to show a list of notifications for each leg (yellow background) as
well as support an optional icon with each direction. For ferry legs, the ferry
icon is now displayed along with customized text. Ferry Schedule
functionality can also be disabled through a checkbox in the UI. It may also
be useful to add another ancillary attribute indicating if a ferry route requires
a fare payment. Those that do could have an additional icon or notification
displayed.
One final note is related to a bug that caused routes to not always select the
optimal path. This only occurred in rare situations around segments with
multi-edge turn restrictions (uturns). If a shortest walk to the destination
first tried to traverse the uturn, the related intersection edge would be
removed from the visiting queue. We want this to occur under normal
traverses to prevent checking the same edge twice, however not for this
scenario. If slower alternative (legal) routes exist through that intersection
(e.g. from a different approach) they should not be pre-restricted as their
path may actually be part of the optimal route. To fix this, the path-walking
logic was changed to prevent flagging an edge visit if it was simply a
downstream portion of a multi-edge turn restriction.
