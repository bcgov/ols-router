<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ page import="ca.bc.gov.ols.router.admin.AdminApplication" %>
<%@ page language="java" import="com.datastax.driver.core.Session" %>
<%@ page language="java" import="com.datastax.driver.core.ResultSet" %>
<%@ page language="java" import="com.datastax.driver.core.PreparedStatement" %>
<%@ page language="java" import="com.datastax.driver.core.BoundStatement" %>
<%@ page language="java" import="com.datastax.driver.core.Row" %>
<%@ page language="java" import="java.util.List" %>
<%@ page language="java" import="java.util.Collections" %>
<%@ page language="java" import="java.util.Comparator" %>

<%@ include file="header.jsp"%> 

<%
AdminApplication adminApp = AdminApplication.adminApplication();
Session sess = adminApp.getSession();
String keyspace = adminApp.getKeyspace();
String appId = "ROUTER";

if(request.getParameter("submitBtn") != null && !request.getParameter("submitBtn").isEmpty()
		&& appId != null){

	PreparedStatement ps = sess.prepare("UPDATE " + keyspace + ".bgeo_configuration_parameters set config_param_value = ? WHERE app_id = ? AND config_param_name = ?" );

	ResultSet rs = sess.execute("SELECT config_param_name FROM " + keyspace + ".bgeo_configuration_parameters WHERE app_id = '" + appId + "'");
	for (Row row : rs) {
		String param_name = row.getString("config_param_name");
		if(param_name.startsWith("fault.") 
				|| param_name.startsWith("precision.")) {
			continue;
		}
		sess.execute(ps.bind(request.getParameter(param_name), appId, param_name));
	}

	out.println("<br><br>Parameters Saved Successfully, <a href='params.jsp'>click here to make more changes</a>.");
} else {	

%>
<div class="bodyContent">

<h1>Router General Parameters</h1>

<form id="config_form" method="POST">
<table class="alternating">
<% 
	ResultSet rs = sess.execute("SELECT * FROM " + keyspace + ".bgeo_configuration_parameters WHERE app_id = '" + appId + "'");
	List<Row> rows = rs.all();
	Collections.sort(rows, new Comparator<Row>(){
		public int compare(Row r1, Row r2) {
			return r1.getString("config_param_name").toLowerCase().compareTo(r2.getString("config_param_name").toLowerCase());
		}
	});
	for(Row row : rows) { 
		String name = row.getString("config_param_name");
		String value = row.getString("config_param_value");
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
<script>
document.getElementById('config_form').app_id.onchange = function() {
	document.getElementById('config_form').submit();
};
</script>
<%
}
%>
<%@ include file="footer.jsp" %>
