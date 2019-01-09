# RouterNG, Status Report #5
Sprint: 2<br>
Date: 2018-09-06<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.* <br>
## Overview
The second sprint is well underway and ends at the end of next week. Over the last couple of weeks we have designed FME scripts to perform validation checks, generate turn restrictions, and map logical intersections. After investigation of some of the more significant findings, dozens of JIRA tickets have been reported to GeoBC for ITN data fixes.
Last week the fourth build of RouterNG was released. This included a basic intersection cost model, support for generated turning restrictions, and several new visualization tools. Related to the latter item, there are now one-way indicators, turn restriction arrows and traffic impactor icons displayable in the location services demo. The restriction arrows are also interactive, and on mouse-over can show the related segments. These improvements have greatly enhanced our ability to understand and test the system.
Very recent development work involves modelling and starting to implement the concept of time dependent turn restrictions. UI changes have been made to support inputs of starting time and date. We will also need to change the turn cost/restriction input file to accommodate time ranges. After that, enhancements will be made to prevent routing over u-turn configurations.
A team meeting will be held next week to review and prioritize what to do next and for the remainder of fiscal.
## Details
### FME Scripting
The first in a series of scripts was a process to identify segments name “turning lane” and coded with a bi-directional travel direction in ITN. Each of the roughly one hundred cases were reviewed (often using Google orthos or street view) to determine if these features supported two-way travel, or if they should be changed to one-ways. Many JIRA tickets were reported, as an exercise under our GeoBC ITN Value Added contract. Once fixed and processed for future Router releases, these cases will no longer support illegal pathing.
Another script was created to review the results of passing our test routes (forward and reverse) through Google’s router. Each vertex of each path is measured to the nearest ITN road and output if beyond a minimum tolerance (larger tolerance for ferry connections). Our ~800 test cases resulted in over 10 million vertices to compare, and the larger urban differences resulted in several tickets being reported (realignments, rebuilt bridges, new construction). In the future, some of the missing rural forest service road
Status Report #5 Page 2 of 3
connections may be worth identifying for ITN, provided they are active and
publicly accessible roads. Due to the large volume of points, it is not clear
how much this approach could scale if we added thousands more test routes,
but for now it is a valuable tool on a limited number of examples.
A third script was developed to analyse the collective interaction of one-way
features with names “turning lane” or “* Onramp” or “* Offramp”. The
angles of connection, junction valency and a variety of other spatial
interactions are evaluated at candidate intersections. The goal is to identify
consecutive lanes/ramps that connect at a steep angle (e.g. Saanich/Douglas
case). Combinations deemed invalid will then have time independent turn
restrictions generated and output. However, we need to be careful not to
apply logic against angle separations that are too broad, as these represent
legitimate travel between a split segment configuration (e.g. Burnside turnoff
northbound from Douglas split at the Goose trail). When run against the May
ITN, over 700 cases were created to supplement (or duplicate) the turn
restrictions from ITN. A limitation of this approach is that other segments
related to the lane junctions are not currently analysed. So in the
Douglas/Saanich case these restrictions will not prevent a very hard left
south on Saanich Rd from the northbound turning lane. Support for a small
number of exception cases has been coded. There are still several multi-way
crossings detected that will need a course of action (e.g. hack in desired
result, ignore because restrictions already exist in ITN, defer to later).
Another script analyses proximity of intersection nodes. Each ITN segment
end point (valency 3+) is buffered to a certain radius depending on the type
of road. Divided and highway junctions are modelled with larger buffers.
These buffers are then overlaid and dissolved (keeping at grade and heightseparated
cases distinct). The resulting polygons are filtered to drop isolated
cases where a buffer does not intersect with any other polygons (a solo
intersection). Anything remaining is given a unique group ID per polygon
and tied back to the intersection nodes. The output is a CSV file of rough
logical intersection relationships that are grouped on node id. This
information could be useful for future tuning of the turn restriction cost
model by preventing the application of multiple delays for each junction point
within a single logical intersection.
A fifth script uses the intersection relationship information to facilitate a
search for particular segment configurations. First, all short segments with
divided segments touching each end (and permitting legal traffic flow across
all three in sequence) are identified as triplet candidates. The inner angles,
divided segment spread angles, and ITN names are then analysed to
determine if the pattern likely mimics a u-turn. Grade separated crossing
configurations are ignored because the router already prevents overpass
jumping. Cases with excessive angle deviations are output for review while
more conformant ones are turned into an auxiliary turn restriction input file
(as edge|node|edge|node|edge style). Once the router has logic in place to
support multi-edge restrictions, these roughly 6,000 entries can be added to
the data inputs.
Overall these scripts have already proven to be very useful. Dozens of data
deficiencies have been reported, and thousands of turning restrictions have
been generated to improve the router’s realism. Now that approaches for
these concepts have been proven, they could potentially be extended and
Status Report #5 Page 3 of 3
translated into Java (e.g. route planner pre-processing code). A discussion
will be required to decide if further refinements are worth pursuing at this
time.
### Visualization Support
A new resource has been created to provide new visual layers on the DataBC
Location Services in Action web application. This navigation info can be
toggled on and off. It becomes visible at a relatively close zoom level.
The first layer is a simple green arrow in the middle of each one-way ITN
street segment, representing the flow direction. Another layer is a set of
icons representing traffic impactors (lights, yields, barricades, stop signs)
offset back from each intersection point. Perhaps deadends, cul-de-sacs,
overpass/underpass, roundabouts and other impactors would be useful in the
future. Finally, a layer representing turning restrictions is displayed through
stacking red directional arrow icons. These are offset to the side of a
junction, and on hover-over with the mouse indicate the segments involved
in the restriction (blue line for approach vector, red for illegal continuation
vector).
These improvements have provided very valuable supporting information for
understanding what is in ITN, and how the data may influence route paths.
