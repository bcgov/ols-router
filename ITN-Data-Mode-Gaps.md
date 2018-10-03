ITN Data Model Gaps

1.       Turn restrictions model time implicitly as AM,PM,AMPM.

a.       Exact interval on turn restriction is lost in source conversion from interval to AM/PM designation. Both 1600-1730 and 1600-1800 become PM

b.      Can’t represent all-day restrictions such as 0600-1800 which is an example from a No Left Turn restriction from Violet to Interurban



2.       Turn restrictions specify turn directions which may be ambiguous.

a.       There may be two left turns from a given road segment end which makes No Left Turn ambiguous

b.      There may be no straight through which makes No Straight Through ambiguous



Recommend going to Edge-Node-Edge model



3.       Ferry routes crossing time is computed by multiplying maxSpeed by route segment length.

a.       Crossing time is an independent variable issued by BC Ferries.

b.      Crossing time is brittle because it can change if route segment length is altered without appropriately adjusting maxSpeed


Recommend adding an explicit crossingTime  property to ferry route segments.

4.       Ferry routes have no average waiting time property

a.       Need to account for a waiting cost at departure terminal in time-dependent routing

Recommend adding avgWaitTime property to ferry route segments



5.       Multiple intersections can’t be associated with a single traffic control

a.       Multiple intersection traversal costs are incurred (e.g., 3 times instead of once in the case of a left-turn at a divided road traffic light.



Recommend adding the concept of a traffic control and a one:many relationship between a traffic control and an intersection

6. One-ways not consistently used for turn lanes and ramps; without a one-way, extra turn-restrictions are required

7. Truck turn restrictions not supported.

8. Tight maneuvres (S-curves) not supported.

9. Overweight corridors are not supported. A road segment can only belong to one designated truck route but overweight corridors run on designated routes.

10. Traffic congestion on road segments not supported.

11. Seasonal load restrictions not supported. Weight is supported but not a current adjustment factor. Each jurisdiction may have its own max GVW.

12. Grade restrictions not supported (e.g., 9% grade).

13. Road closed flag.

14. Road ramp doesn't have an associated slope property so can't tell if a truck is allowed to turn onto it from a sharp angle (e.g., < 70 degrees).

5. Only one of four weights is represented in ITN. Truck permitting requires four: GVW, Single Axle, Tandem Axle, Tridem Axle. Recommend treating current weight  property as GVW and adding other three weights.  
