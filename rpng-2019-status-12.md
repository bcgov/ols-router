# RouterNG, Status Report #12

Date: 2019-01-04<br>
Sprint: 5<br>
Prepared by: Graeme Leeming (Refractions Research Inc.)

## Overview
There are less than two weeks remaining in Sprint 5. Chris has had time for limited interaction with Leo on deployment strategies. As a result, he has updated the code/modules to work with Java 11. He is also getting file headers and directory structures sorted out.
Late last year we had a discussion about the use of OpenStreetCam to support data validation. A corresponding URL link has been added to the turn restriction review tool, however its usefulness is quite limited by both data quality and quantity.
Through testing we discovered that there is currently no logic in place to prevent the router from selecting paths into or out of a segment end with an ITN barricade traffic impactor flag. We should enhance the router this fiscal to better handle barricades. After discovering the issue, Darrin performed a comprehensive review of all ferry approaches because this limitation as well as incorrect ITN configurations led to ticket-gate skipping at some terminals.
One of potential tasks for this sprint was to convert/integrate TransLink and BC Ferries data into a useful input for the router. However, that work will have to be deferred since the source data has not yet been procured.
As the sprint is nearing an end, I suggest we set up a team review and planning meeting for late next week or the week after.
