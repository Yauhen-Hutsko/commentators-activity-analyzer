<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
  <head>
    <title>Welcome page</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/main.css">
  </head>

  <body>
  <div>    
      <div>
        <div class="inner-div-1">inner div1</div>
        <div class="inner-div-2">inner div2</div>
      </div>
  <div class="inner-div-3">
      <p>Hello boomer.</p>
      <p>ver. 1.1.2</p>
      <jsp:useBean id="api" scope="page" class="com.hutsko.impl.CommentParserApiImpl"/>
      <strong>
  	<jsp:text>Old value:</jsp:text>
  	</strong>${api.hello}<br>
      <jsp:setProperty name="api" property="hello" value="Hello boomer"/>
  	<strong><jsp:text>New value: </jsp:text></strong>
      ${api.hello}

  	<div>
  	<form id="messages" method="get" action="controller">
  		<input type="url" name="articleURL" title="Enter onliter's article URL"/>
  		<button type="submit">Show statistic</button>
  	</form>
  	</div>

  	<div></div>
  	<div>
  	    <c:if test="${not empty comments}">
  	    <h2>All comments below</h2>
  	    <br>
  	    <c:forEach items="${comments}" var="comment">
  	        <c:out value="${comment}"/><br/>
  	    </c:forEach>
  	    </c:if>
  	</div>
  	</div>
  	</div>
    </body>
</html>
