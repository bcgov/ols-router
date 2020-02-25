<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ page language="java" import="ca.bc.gov.ols.router.admin.AdminApplication" %>
<%@ page language="java" import="ca.bc.gov.ols.config.ConfigurationStore" %>
<%@ page language="java" import="ca.bc.gov.ols.config.ConfigurationParameter" %>
<%@ page language="java" import="java.util.List" %>
<%@ page language="java" import="java.util.stream.Collectors" %>
<%@ page language="java" import="java.util.Collections" %>
<%@ page language="java" import="java.util.Comparator" %>

<%@ include file="header.jsp"%> 

<%
AdminApplication adminApp = AdminApplication.adminApplication();
ConfigurationStore configStore = adminApp.getConfigStore();
List<ConfigurationParameter> params = configStore.getConfigParams().collect(Collectors.toList());

if(request.getParameter("submitBtn") != null && !request.getParameter("submitBtn").isEmpty()){

	for (ConfigurationParameter param : params) {
		String paramName = param.getConfigParamName();
		param.setConfigParamValue(request.getParameter(paramName));
		configStore.setConfigParam(param);
	}	
	
	out.println("<br><br>Parameters Saved Successfully, <a href='params.jsp'>click here to make more changes</a>.");
} else {
 %>
<div class="bodyContent">

<h1>Router General Parameters</h1>

<form id="config_form" method="POST">
<table class="alternating">
<%
	Collections.sort(params);
	for(ConfigurationParameter param : params) { 
		String name = param.getConfigParamName();
		String value = param.getConfigParamValue();
		if(name.startsWith("fault.") || name.startsWith("precision.")) {
			continue;
		}
		out.println("<tr>");
		out.println("<td>" + name + ":</td>");
		out.println("<td><input name='" + name + "' type=text size=60 value='" + value + "'></td>");
		out.println("</tr>");
	}
%>
</table>

<br><br>
<input name="submitBtn" type="submit" value="Save All Values">
<br>
<br>
</form>
</div>
<%
}
%>
<%@ include file="footer.jsp" %>