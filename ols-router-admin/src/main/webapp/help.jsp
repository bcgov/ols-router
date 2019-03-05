<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ include file="header.jsp" %>
<div class="bodyContent">
<h1>Location Services Administration Help</h1>
<hr><h2>General Information</h2>
<p>For all of the editable values you can change through this Admin Interface, they will be change in the database configuration tables. This will not update any existing, running services. Restarting Tomcat and reloading the CPF plug-in will be required before the parameters are used in the live services. This is because the services have all the required configuration in memory and do not interact with the configuration database on a live basis for performance reasons.</p>	 
<hr><h2>Parameter Defaults Page</h2>

<p>There are a number of general parameters that can be edited using this page. To edit them simply change the field values and press 'Save all Values'.

<h3>apiUrl</h3>
<p>The URL to the API documentation.</p>

<h3>copyrightLicense</h3>
<p>The URL link to the copyright license information.</p>

<h3>copyrightNotice</h3>
<p>The text of the copyright notice.</p>

<h3>defaultLookAtRange</h3>
<p>The default lookAtRange for KML output</p>

<h3>defaultSetBack</h3>
<p>This parameter is used once a geocoded point has been found. The setback is the perpendicular distance from the road centerline the final returned co-ordinate will be, in meters.</p> 

<h3>disclaimer</h3>
<p>The URL link to the disclaimer information.</p>

<h3>glossaryBaseURL</h3>
<p>.The Base URL of the documentation glossary file.</p>

<h3>kmlStylesUrl</h3>
<p>The url where the kml styles are located.</p>

<h3>moreInfoURL</h3>
<p>The URL link to the documentation.</p>

<h3>privacyStatement</h3>
<p>The URL link to the privacy statement</p>

</div>
<%@ include file="footer.jsp" %>