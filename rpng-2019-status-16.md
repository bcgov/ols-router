# RouterNG, Status Report #16

Date: 2019-03-29<br>
Prepared by: Graeme Leeming (Refractions Research Inc.)<br>

## Overview

Sprint 6 is now complete, which effectively wraps up phase 1 of the RouterNG project. The core application and framework have some new features, including:

- modelling barricade traffic impactors, through generated turn restrictions onto or off of the barricaded segment end

- truck parameters as separate resources

- Location Services In Action app now uses gc (geocoder) and rt (router) parameters instead of the old env (environment)

Also accomplished in the last two weeks was further review and updates to
targeted intersections in the turn restriction tool. A user list was added, with
support for tracking each modification through the UI. This will allow
revisiting and tagging of changes by each operator.

A process has now been designed and tested to support refreshing of the
road vintage in the turn restriction database. This required some framework
modifications and development of ETL scripts. Inconsistencies get flagged
and currently need to be reconciled after a manual review and update
approach.

In terms of delivery and deployment, the functional code is now built and
running in Open Shift. We have also completed a comprehensive set of
testing, including verifying the test framework results between our local
development environment and those now produced from the government
setup. Documentation, readme files, and modelling approaches have been
updated where necessary and GitHub tickets checked.

There may still be some final tweaks related to data file locations,
documentation, data prep and configuration setup over the coming weeks
once the government team has a chance to evaluate and play with the
system. Additionally, our assistance may be needed to support migration to
the test and production environments. Testing may uncover bugs that would
require fixing under warranty.

We look forward to our retrospective meeting next week to review this final
sprint as well as various aspects of the first phase of RouterNG.
