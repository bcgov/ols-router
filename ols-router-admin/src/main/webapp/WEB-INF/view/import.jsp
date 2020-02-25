<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="../../header.jsp" %>
<div class="bodyContent">
<h1>Import Configuration</h1>
<c:choose>
  <c:when test="${!errors.isEmpty()}">
  <h2>Import Failed</h2>
  <p><strong>Errors</strong></p>
  <ul>
  <c:forEach var="error" items="${errors}">
    <li>${error}</li>
  </c:forEach>  
  </ul>
  </c:when>
  <c:otherwise>
  <h2>Import Successful</h2>
  </c:otherwise>
</c:choose>
<c:if test="${!messages.isEmpty()}">
	<p><strong>Messages</strong></p>
	<ul>
	<c:forEach var="msg" items="${messages}">
  		<li>${msg}</li>
	</c:forEach>
	</ul>
</c:if>  
</div>
<%@ include file="../../footer.jsp" %>