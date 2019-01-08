
# RouterNG, Status Report #1
Date: 2018-06-15<br>
Prepared by: Graeme Leeming, *Refractions Research Inc.*
## Overview
A kick-off meeting was held with the team to plan our objectives leading up to the decision point. A target date of August 1 was proposed for Refractions to deliver the baseline system architecture. We have started to review relevant technical papers on time-dependent routing algorithms and evaluate potential data sources. Chris and I also recently did planning in a white-board session to create the foundation for a high-level project plan. We also discussed the usefulness of creating shared spreadsheets describing potential input data sources and a data dictionary.
Some minor enhancements have been completed on the existing Geocoder, Route Planner and OLS Dev Kit applications. Final updates have now all been deployed into the delivery environment for review and imminent migration to the test and production environments.
## Details

### Data Review
**Open Street Map:** We have started to evaluate OSM as a potential data source for the road network, traffic signals and restrictions. Of its two export formats, only the compressed PBF version is suitable given the data volumes required. Some tinkering in FME was attempted to try and coax the features into a useable format, but it was only partially successful. We will likely require guidance from resources familiar with OSM to proceed further. Of note is that less than 10% of segments in OSM have speed limits so this source will require significant data gaps to be filled before it is directly useable.
**onRouteBC:** The Restriction Data Management Business Requirements was reviewed to refresh our understanding of the types of restrictions that may need to be supported in future phases.
## System Architecture
Many papers on time-dependent routing algorithms have already been evaluated. It is clear that there is a lot of complexity required to support an efficient and effective approach to meet phase 1 goals. Ideally the methodology we start with can later be modified/extended to accommodate additional restrictions (e.g. to support truck permitting) without having to do a significant redesign. A variant of the Custom Contraction Hierarchy approach is currently the frontrunner.
## Other Product Deliveries
Several Geocoder enhancements have been introduced into version 3.4.2. A
companion router product has been updated to version 1.5.1. Also some
map interface changes have led to a new version of the development kit; this
is the framework for the DataBC Location Services in Action web application.
