<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="../../header.jsp" %>
<div class="bodyContent">
<h1>Validate Configuration</h1>
<p><strong>File Name:</strong> ${exportConfig.fileName}</p>
<p><strong>Export Date (from file contents):</strong> ${exportConfig.exportDate}</p> 
<c:choose>
  <c:when test="${!exportConfig.errors.isEmpty()}">
    <h2 class="red">Validation Failed</h2>
    <p><strong>Errors</strong></p>
    <ul>
    <c:forEach var="error" items="${exportConfig.errors}">
      <li>${error}</li>
    </c:forEach>  
    </ul>
  </c:when>
  <c:otherwise>
    <h2>Validation Successful</h2>
  </c:otherwise>
</c:choose>
<c:if test="${!exportConfig.messages.isEmpty()}">
	<p><strong>Messages</strong></p>
	<ul>
	<c:forEach var="msg" items="${exportConfig.messages}">
  		<li>${msg}</li>
	</c:forEach>
	</ul>
</c:if>
<h2>Record Counts</h2>
<table class="diffTable">
<tr><th>Table</th><th>Live Config Records</th><th>File Records</th><th>File Check Count</th></tr>
<tr><td>Configuration Parameters</td><td>${comparison.curConfigParamCount}</td><td>${comparison.otherConfigParamCount}</td><td>${exportConfig.configParamCount}</td></tr>
</table>
<h2>Comparison with Live Config</h2>
<h3>Configuration Parameters Differences</h3>
<c:choose>
  <c:when test="${comparison.configParamDiffs == null || comparison.configParamDiffs.isEmpty()}">
    <p><strong>No Differences</strong></p>
  </c:when>
  <c:otherwise>
    <table class="diffTable">
    <tr><th colspan="3">Live Config</th><th colspan="3">File</th></tr>
    <tr><th>APP_ID</th><th>CONFIG_PARAM_NAME</th><th>CONFIG_PARAM_VALUE</th>
      <th>APP_ID</th><th>CONFIG_PARAM_NAME</th><th>CONFIG_PARAM_VALUE</th></tr>
    <c:forEach var="diff" items="${comparison.configParamDiffs}">
      <tr>
      <c:choose>
        <c:when test="${diff.current == null}">
      	  <td colspan="3">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.current.appId}</td><td>${diff.current.configParamName}</td><td>${diff.current.configParamValue}</td>
        </c:otherwise>
      </c:choose>
      <c:choose>
        <c:when test="${diff.other == null}">
      	  <td colspan="3">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.other.appId}</td><td>${diff.other.configParamName}</td><td>${diff.other.configParamValue}</td>
        </c:otherwise>
      </c:choose>
      </tr>
	</c:forEach>
	</table>
  </c:otherwise>
</c:choose>  

</div>

<%@ include file="../../footer.jsp" %>