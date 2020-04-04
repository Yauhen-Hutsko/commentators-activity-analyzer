<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>Welcome page</title>
  </head>

  <body>
      <p>Hello boomer.</p>
      <p>ver. 1.1.0</p>
      <jsp:useBean id="api" scope="page" class="com.hutsko.impl.CommentParserApiImpl"/>
      <strong>
  	<jsp:text>Old value:</jsp:text>
  	</strong>${api.hello}<br>
      <jsp:setProperty name="api" property="hello" value="bye bye motherfucker"/>
  	<strong><jsp:text>New value: </jsp:text></strong>
      ${api.hello}

  	<form id="messages" method="get" action="controller">
  		<input type="url" name="articleURL" title="Enter onliter's article URL"/>
  		<button type="submit">Show statistic</button>
  	</form>
    </body>
</html>
