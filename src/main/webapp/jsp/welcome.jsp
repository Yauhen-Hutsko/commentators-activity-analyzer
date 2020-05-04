<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
  <head>
    <title>Welcome page</title>
  </head>

  <body>
      <p>Hello boomer.</p>
      <p>ver. 1.1.1</p>
      <jsp:useBean id="api" scope="page" class="com.hutsko.impl.CommentParserApiImpl"/>
      <strong>
  	<jsp:text>Old value:</jsp:text>
  	</strong>${api.hello}<br>
      <jsp:setProperty name="api" property="hello" value="bye bye motherfucker"/>
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
  	    <jsp:text>comment</jsp:text>
  	    </c:forEach>
  	    </c:if>
  	</div>
    </body>
</html>
