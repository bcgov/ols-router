# RouterNG, Status Report #15

Date: 2019-03-13<br>
Sprint: 6<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.*

## Overview
We are closing in on the end of Sprint 6 and starting to wrap up some of the final details related to phase 1 of the RouterNG project.
The turn restriction editor has undergone some minor adjustments, including a couple of bug fixes and intersection cases added for lower mainland truck routes. We have also spent some time reviewing and updating a selection of the more complicated intersection configurations.
The code base was modified in a couple of ways. First, the ols-admin code was forked into a router-specific ols-router-admin module of the ols-router code, and all Geocoder-related code was removed. Additionally, all external dependencies were updated to recent versions and relationships/dependencies cleaned up.
In terms of deployment to the government environment, Leo has assisted with getting the code built and running in Open Shift. We are still determining the best place to deploy the input data files. Once this is complete there will be a working version of the router that can be tested outside our development environment.
Finally, we have also spent time planning, designing and estimating work packages that might become part of future phases.
