# ITN Data Model Gaps

|Issue Id| Issue                        | Implications                 | Recommendation
|---|------------------------------|------------------------------|-------------------------------------------------------------------|
|1|Turn restrictions model time periods implicitly as AM,PM,and AMPM | 1. Exact interval on turn restriction is lost in source conversion from interval to AM/PM designation. Both 1600-1730 and 1600-1800 become PM <br>2. Can’t represent all-day restrictions such as 0600-1800 which is an example from a No Left Turn restriction from Violet to Interurban | Change from implicit to explicit (24hr) time intervals
|2|Turn restrictions specify turn directions which may be ambiguous|1. There may be two left turns from a given road segment end which makes No Left Turn ambiguous<br>2. There may be no straight through which makes No Straight Through ambiguous|Adopt Edge-Node-Edge model
|3|Multiple intersections can’t be associated with a single traffic control|Multiple intersection traversal costs are incurred (e.g., 3 times instead of once in the case of a left-turn at a divided road traffic light|Add the concept of a controlled intersection and a one:many relationship between a controlled intersection and an intersection
|4|One-ways not consistently used for turn lanes and ramps|Without a one-way, extra turn-restrictions are required|Make data consistent
|5|Truck turn restrictions not supported|Needed for truck routing|Add support for explicit turn restrictions based on truck dimensions. Consider generating tight-turn warnings based on truck dimensions and turn-angle in Routable BC Maker.
|6|Tight maneuvers (S-curves) not supported.|Needed for truck routing| Add hasTightManeuvers property to road segment
|7|Overweight corridors not supported|A road segment can only belong to one designated truck route but overweight corridors run on designated routes.|?
|8|Traffic congestion on road segments not supported|Needed for support of historic and real-time traffic congestion| Since this data will likely not be crowd-sourced in the foreseeable future, provide traffic congestion through another web service (e.g., WFS). 
|9|Seasonal load restrictions not supported|1. Seasonal load restrictions may involve axle weight load factor, noOverweight and noOverSize flags, geometric extent, and an effective time-period<br>2. Each district or region may have its own maximum GVW| Don't extend data model. This data can't be crowd-sourced so should be made available through another web service (e.g., OGC WFS).
|10|Grade restrictions not supported (e.g., 9% grade)|Can't avoid steep grades in routes for big rigs|?
|11|No way specify a road is closed|Need to specify a road is closed in both directions or a given direction| ?
|12|Road ramp doesn't have an associated slope property| Insufficient information to determine if a truck can safely turn onto a given road ramp from a sharp angle (e.g., less than seventy degrees)|Add slope property to road ramps
|13|Only one of four truck weights is represented in ITN|Truck permitting requires four: GVW, Single Axle, Tandem Axle, Tridem Axle.| Leave single weight value for now. Full dimension restrictions will come from a separate, MOTI restriction data service that ties road-restricting features to ITN road segments.
|14| Only one height restriction represented in ITN|Height restrictions vary by lane and travel direction.|Leave single height value to represent minimum max height across all lanes and both directions. Detailed height restrictions will come from a separate, MOTI restriction data service that ties road-restricting features to ITN road segments.
|15|No time-dependent max speed property|Can't represent school zone speed limits which are only in force during school hours| ?
|16|NO implicit flag in turn restrictions| Can't tell if a given turn-restriction should have a sign on the ground for QA|Add implicit flag to turn restrictions|
