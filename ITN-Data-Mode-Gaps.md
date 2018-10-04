# ITN Data Model Gaps Draft v0.1

|Issue Id| Issue                        | Implications                 | Recommendation
|---|------------------------------|------------------------------|-------------------------------------------------------------------|
|1|Turn restrictions model time periods implicitly as AM,PM,and AMPM | 1. Exact interval on turn restriction is lost in source conversion from interval to AM/PM designation. Both 1600-1730 and 1600-1800 become PM <br>2. Can’t represent all-day restrictions such as 0600-1800 which is an example from a No Left Turn restriction from Violet to Interurban |
|2|Turn restrictions specify turn directions which may be ambiguous|1. There may be two left turns from a given road segment end which makes No Left Turn ambiguous<br>2. There may be no straight through which makes No Straight Through ambiguous|Adopt Edge-Node-Edge model
|3|Multiple intersections can’t be associated with a single traffic control|Multiple intersection traversal costs are incurred (e.g., 3 times instead of once in the case of a left-turn at a divided road traffic light|Add the concept of a traffic control and a one:many relationship between a traffic control and an intersection
|4|One-ways not consistently used for turn lanes and ramps|Without a one-way, extra turn-restrictions are required|Make data consistent
|5|Truck turn restrictions not supported|Needed for truck routing|Can add support in model or in Router road prep
|6|Tight maneuvres (S-curves) not supported.|Needed for truck routing| Add hasTightTurns property to road segment
|7|Overweight corridors not supported|A road segment can only belong to one designated truck route but overweight corridors run on designated routes.|?
|8|Traffic congestion on road segments not supported|Needed for support of historic and real-time traffic congestion|?
|9|Seasonal load restrictions not supported|0. Four load factors may be involved, one for each type of weight: GVW, Single Axle, Tandem Axle, and Tridem Axle<br>1. Weight is supported but not a seasonal load factor.<br>2. Each jurisdiction may have its own max GVW<br>3. Need to associate load factor with an effective time-period|?
|10|Grade restrictions not supported (e.g., 9% grade)|Can't avoid steep grades in routes for big rigs|?
|11|No road closed flag.|?|?
|12|Road ramp doesn't have an associated slope property| Insufficient information to determine if a truck can turn onto a given road ramp from a sharp angle (e.g., less than seventy degrees)|Add slope property to road ramps
|13|Only one of four truck weights is represented in ITN.|Truck permitting requires four: GVW, Single Axle, Tandem Axle, Tridem Axle. Recommend treating current weight  property as GVW and adding other three weights.|?
