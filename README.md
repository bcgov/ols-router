Router
======

Release: 1.5.1
Date: May 24, 2018
Author: Chris Hodgson, Refractions Research

---

This file describes the steps to install the Router application on a Tomcat 7/8 
Application Server.

These instructions are for the Java Application Delivery Specialist.

OVERVIEW
--------
This document describes the installation process for the delivery of the Router
application.


REQUIREMENTS
------------
Prior to installing the Router application, the following requirements must be verified:
- Java 8+
- Apache Tomcat 7/8
- The application uses up to 700MB of Java heap space; it is recommended that 1GB of heap space
  be allocated.


INSTALLATION INSTRUCTIONS
-------------------------

1. ENSURE DATA FILE IS AVAILABLE

1.1 The router uses the data files produced during the geocoder data prep. The files:

	* street_load_street_segments.json
	* street_load_street_intersections.json
	* street_load_street_names.json
	* street_load_street_name_on_seg_xref.json
	  
	must be available at the location specified by the property "dataSource.baseFileUrl"
	Note that the "baseFileUrl" includes the prefix "street_load" but the application automatically
	adds the suffix "_{*}.json".
	
	
2. DEPLOY THE WEB APPLICATION USING JENKINS
   
2.1. Login to Jenkins

2.2. Click the "Schedule a Build" button next to the "refractions-router-deploy" project.


3. TEST THE APPLICATION

3.1. Test the application:
	Prod:		http://apps.gov.bc.ca/pub/router/distance.html?points=-124.9958333,49.6894445,-122.6972222,58.8055556
	Test:		http://test.apps.gov.bc.ca/pub/router/distance.html?points=-124.9958333,49.6894445,-122.6972222,58.8055556


5. NOTIFICATION

Alert the Project Manager, the Application Manager, and the Business Analyst that the delivery is complete.