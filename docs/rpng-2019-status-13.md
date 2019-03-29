# RouterNG, Status Report #13

Date: 2019-01-28<br>
Sprint: 5<br>
Prepared by: Graeme Leeming (Refractions Research Inc.)

## Overview

We completed the remainder of sprint 5 over the first half of January. Activities involved continuing on with deployment preparation and open source related tasks. The core team along with some additional participants then met on Jan 17th for a review of sprint activities. Much of the route planner functionality was demonstrated, with useful feedback provided on a few UI tweaks. At the same meeting we also determined and prioritized what should be completed for the current sprint 6 work. This will be the final sprint, and span about two months to the end of fiscal.
We have had success in getting an initial GitHub code release to build in the DataBC environment. Efforts continue on making the process work smoothly. Having a working version of the router on BC government infrastructure will be one of the more important goals of the sprint.

Recently we have worked on enhancements to the turn restriction editor tool and its various data inputs. The web application now supports OpenStreetCam photo links and additional visualization layers. The attribute interface has been extended to provide a framework for a guided intersection review process. Once the enhancements are tested and a set of refreshed data inputs are in place, a team of operators can use the tool to review and update turn restrictions at key intersections. More on the tool changes is described in the next section of this document.

Recently we were provided with a database export containing Translink road data in the lower mainland. The geometry is identical to a recent vintage of ITN, and consists of both demographic and non-demographic features with the transport line ID. Extended attribution (e.g. turn restrictions, height restrictions, truck routes) is currently being reviewed to determine if it might be a useful source for router support data.

## Turn Restriction Updates

We have recently participated in a number of iterative design and review meetings with Michael Ross. As a result, the existing turn restriction editor tool and backend have been significantly reworked and extended. Modifications are now almost complete, though this has taken more time to complete than anticipated due to accommodating features not originally planned. One of the main additions was to support an intersection review process. This allows an operator to locate prioritized review cases, then update (or remove) related turn restrictions with suitably attributed source information.
To assist with this review process, we have included a new visual layer showing OpenStreetCam photo positions. These show up as blue cone icons when the direction is known (though it is not always correct) or as a circle when unknown. When an icon is selected, the related dash-cam photo is displayed in a separate browser window. Other visual layers have been added or modified to better support the review process.
The attribute interface has also been reworked to capture and display a richer set of descriptors, such as validation source, time dependent ranges and comments. We designed an interactive intersection layer consisting of prioritized review cases. Test data for the initial study area was compiled and categorized from junctions located nearby the MVRD Major Road Network and provincial highways.

The complete editing framework will facilitate a process whereby an operator can select a category of cases to review (e.g. Hwy 17), then step through intersection candidates one at a time. The web forms support updates of related turn restriction information, and the map displays the intersections in the study area, themed by state (e.g. ready, validated, under review).
A production dataset will be built once the February ITN is released and processed. Existing custom restrictions will need to be reconciled against that latest vintage, as some related segments/nodes could be retired from ITN in the new data. Once tool enhancements are fully complete/tested and the updated inputs are loaded into the database, the review work can commence. DataBC is planning and documenting the related operational procedures.
