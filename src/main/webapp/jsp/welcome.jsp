<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Welcome page</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/main.css">
</head>

<body>
<p>ver. 1.1.2</p>
<div>
    <div class="horizon-wrapper">
        <div class="inner-div-1">
            <div><p></p>
            <p> Task attribute: ${pageContext.session.getAttribute('tasks')}</p>
            <p>paramValues: ${paramValues}</p>
            <p>pageContext: ${pageContext}</p>
            <p>request attributes: ${request.attributeNames}</p>
            <p>request params: ${request.parameterNames}</p>
            <p> Attributes: </p>
            <c:forEach items="${param}" var="item">
                                        <span><c:out value="${item}"/></span><br/>
                                    </c:forEach>
                <c:choose>
                    <c:when test="${not empty tasks}">
                        <h3>Tracked articles:</h3><br>
                        <c:forEach items="${tasks}" var="task">
                            <span><c:out value="${task.name}"/></span><br/>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <p>Tracked articles not found.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="inner-div-2">
            <div>
                <form id="tracked" method="get" action="controller">
                    <input type="url" name="tracked"
                           title="Enter onliner's article URL and it will be added to watch list for moderated comments"/>
                    <button type="submit">Track article</button>
                </form>
                <form id="task" method="get" action="controller">
                    <input type="hidden" name="shutdownTasks"/>
                    <button type="submit">Shutdown Tasks</button>
                </form>
            </div>
        </div>
      </div>
  <div class="inner-div-3">
      <p>Hello boomer.</p>
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
                    <c:out value="${comment}"/>
                    <br/>
                </c:forEach>
            </c:if>
        </div>
    </div>
</div>
</body>
</html>
