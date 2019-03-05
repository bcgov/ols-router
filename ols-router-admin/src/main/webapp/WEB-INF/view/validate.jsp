<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="../../header.jsp" %>
<div class="bodyContent">
<h1>Validate Configuration</h1>
<p><b>File Name:</b> ${configuration.fileName}</p>
<p><b>Export Date (from file contents):</b> ${configuration.exportDate}</p> 
<c:choose>
  <c:when test="${!configuration.errors.isEmpty()}">
    <h2 class="red">Validation Failed</h2>
    <p><b>Errors</b></p>
    <ul>
    <c:forEach var="error" items="${configuration.errors}">
      <li>${error}</li>
    </c:forEach>  
    </ul>
  </c:when>
  <c:otherwise>
    <h2>Validation Successful</h2>
  </c:otherwise>
</c:choose>
<c:if test="${!configuration.messages.isEmpty()}">
	<p><b>Messages</b></p>
	<ul>
	<c:forEach var="msg" items="${configuration.messages}">
  		<li>${msg}</li>
	</c:forEach>
	</ul>
</c:if>
<h2>Record Counts</h2>
<table class="diffTable">
<tr><th>Table</th><th>Live Config Records</th><th>File Records</th><th>File Check Count</th></tr>
<tr><td>Configuration Parameters</td><td>${configuration.dbConfigParamCount}</td><td>${configuration.configParams.size()}</td><td>${configuration.configParamCount}</td></tr>
</table>
<h2>Comparison with Live Config</h2>
<h3>Configuration Parameters Differences</h3>
<c:choose>
  <c:when test="${configuration.configParamDiffs == null || configuration.configParamDiffs.isEmpty()}">
    <p><b>No Differences</b></p>
  </c:when>
  <c:otherwise>
    <table class="diffTable">
    <tr><th colspan="3">Live Config</th><th colspan="3">File</th></tr>
    <tr><th>APP_ID</th><th>CONFIG_PARAM_NAME</th><th>CONFIG_PARAM_VALUE</th>
      <th>APP_ID</th><th>CONFIG_PARAM_NAME</th><th>CONFIG_PARAM_VALUE</th></tr>
    <c:forEach var="diff" items="${configuration.configParamDiffs}">
      <tr>
      <c:choose>
        <c:when test="${diff.db == null}">
      	  <td colspan="3">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.db.appId}</td><td>${diff.db.configParamName}</td><td>${diff.db.configParamValue}</td>
        </c:otherwise>
      </c:choose>
      <c:choose>
        <c:when test="${diff.file == null}">
      	  <td colspan="3">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.file.appId}</td><td>${diff.file.configParamName}</td><td>${diff.file.configParamValue}</td>
        </c:otherwise>
      </c:choose>
      </tr>
	</c:forEach>
	</table>
  </c:otherwise>
</c:choose>  

</div>

<%@ include file="../../footer.jsp" %>