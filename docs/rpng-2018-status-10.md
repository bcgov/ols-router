# RouterNG, Status Report #10
Date: 2018-11-30<br>
Sprint: 4<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.*
## Overview
Sprint 4 is almost complete and we are just wrapping up the final details.
Last week some additional southern island ferry crossings and schedules were added to the input data. Router directions have also been improved to support icons, notifications and better wording. The effect of both of these modifications is a much cleaner routing experience (especially for demos).
Open511 events that have periods of closure now have their time ranges properly considered for evaluating optimal routes. Also, the optional delay parameter in that data is now applied as an additional wait time.
The final work of substance this sprint has involved the conversion of the FME restriction generation logic into the Routable BC Maker pre-processing application. All of the base logic to support turning lanes, u-turns and divided ends has now been incorporated. There were also many special cases, overrides and exceptions Graeme had hacked into the FME scripts to fine-tune the results. In Routable BC Maker, hacks are not ideal for the long term so a framework is required to manage custom turn restrictions. We have designed and almost finished implementing a tool to support such a requirement.
There is an upcoming team meeting on Dec. 5. Before then we anticipate completing the turn restriction editing framework and releasing another version of the application.
## Details
### General Improvements
We completed a bug fix (GitHub #100) to no longer trigger an error response when assigning no-integer weight parameter values. This used to occur in router/distance, route, and directions resources, when a decimal value was entered by a user.
The router directions have been significantly enhanced. They now support the following features:
* optional icon (e.g. ferry)
* optional notification such as delay/wait time, or any other warnings we would want to add
* better word structure of the descriptions for each leg
* cardinal directions (GitHub #8)
In the future, the icon concept could easily be extended to display a directional turn arrow next to each description.
###Ferry Scheduling and Events
We manually added schedules to the following routes based off the BC
Ferries winter website:
* Crofton to Vesuvius Bay & Vesuvius Bay to Crofton
* Otter Bay to Swartz Bay & Swartz Bay to Otter Bay
* Fulford Harbour to Swartz Bay & Swartz Bay to Fulford Harbour
* Village Bay to Swartz Bay & Swartz Bay to Village Bay
To prevent ferry hopping through the Gulf Islands, these cases cover the
departure points from southern Vancouver Island. With them now modelled,
traversals across the strait are now much more realistic with schedules
turned on. Some minor code tweaks and data restructuring was also
performed to better model scheduling, minimum arrival times and transit
times.
Events now incorporate some of the delay and time window handling that
was implemented for ferry scheduling. Instead of route candidates always
being rejected if they reached an event during a closure, they now take into
consideration the time ranges when they are open (GitHub #83). In
addition, there is a parameter called delay in the Open511 specification. We
have extended the event handling logic to take this into account (GitHub
#88). Effectively this time value is added to the route calculation cost as an
additional wait time (analogous to the concept of minimum wait time for
boarding a ferry).
### Input Prep Conversion
Most of the recent work has involved the conversion of the prototype FME
input prep scripts into Java. We have replicated the following three implicit
turn restriction configurations (GitHub #86):
* One-way turning lanes & Onramps & Offramps
* Divided road ends
* U-turns
The base logic for generating these restrictions was redesigned and
implemented within the Routable BC Maker framework. FME uses a scripted
approach with data queried from the ITN value added database, while Java is
an object-oriented language using pre-processed file inputs. Because of
these very different environments, we performed in-depth comparisons of
the results between each implementation. This also helped to quickly
identify bugs in the conversion as well as identify better
methods/tolerances/computations to improve the prototype generation
algorithms. We are now confident that a suitably equivalent (or better) logic
has been implemented in Routable BC Maker.
Is it worth noting that there were a very small number of differences in the
results from the base logic conversion, all of which have been identified and
targeted for custom adjustments (and in a few cases now automatically by
improvements). Almost all of these special cases had been previously
hacked into the FME scripts as one-off exceptions for specific segments. To
properly replicate these without having to incorporate segment-specific hacks
directly in the code requires a framework for overriding and adjusting
generated turning restrictions.
The framework we designed is comprised of:
* a database containing all potentially valid edge-node-edge turning
combinations and tables with existing and custom restrictions
* a PHP and web interface to view and edit restrictions and potential
restrictions
* FME scripts to populate the database with the latest ITN data, and
export the custom restrictions into the standard input file used by
### Routable BC Maker
While the main goal of this tool was to facilitate the editing of custom cases
related to generated turn restrictions, it turns out to have much more value.
With almost no additional effort, this framework has been tweaked to support
manipulation of existing ITN turning restrictions. As a result, omissions and
errors from either ITN data or our generated cases can be overridden. These
updates can then be easily incorporated into the Router, and potentially in
the future shared with GeoBC.
The framework to do this is now in place, with the final editing logic still
being implemented. By the end of the sprint the tool should be completed,
and we should also have time to perform the manipulations already identified
to fix custom generated turn restriction cases. At that point we would be
happy to share the link to the site, which would allow other data experts on
the team the ability to modify any additional turn restriction issues.
A final version release early next week will contain the collective changes to
data and code described above, as well as a couple of minor bug
fixes/enhancements.
