<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Welcome page</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/main.css">
</head>

<body>
<p>ver. 1.2.4</p>
<div>
    <div class="horizon-wrapper">
        <div class="inner-div-1">
            <div><p></p>
                <p>ParamValues: ${paramValues}</p>
                <p>Request attributes: ${request.attributeNames}</p>
                <p>Request params: ${request.parameterNames}</p>
                <p>Saved in ServletContext: ${servletContext.getAttribute('servCont')}
                <p>Params:</p>
                <c:forEach items="${param}" var="item">
                    <span><c:out value="${item}"/></span><br/>
                </c:forEach>
                <c:choose>
                    <c:when test="${not empty tasks}">
                    <h3>Tracked articles:</h3><br>
                    <c:forEach items="${tasks}" var="task">
                        <span>
                        <b>${task.value.name}</b>
                        <text>views: ${task.value.views}.</text>
                        <text>comments: ${task.value.total}.</text>
                        <b>PoI(%): ${task.value.involvedPercent}(${task.value.trueInvolvedPercent}).</b>
                        <button type="submit" form="show-deleted" name="show-deleted" value="${task.key}">Show deleted</button>
                        <button type="submit" form="stop-follow" name="stopFollow" value="${task.key}">Stop following</button>
                        <a href="${task.key}" target="_blank">Go to article.</a>
                        </span>
                        <br/>
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
                    <input type="hidden" name="command" value="track">
                    <input type="url" name="tracked"
                           title="Enter onliner's article URL and it will be added to watch list for moderated comments"/>
                    <input type="text" name="interval" title="Enter polling interval in minutes. Max 999"
                           pattern="\d{1,3}" style="width: 27" required/>
                    <button type="submit">Track article</button>
                </form>
                <form id="task" method="get" action="controller">
                    <input type="hidden" name="command" value="shutdownAll">
                    <button type="submit">Shutdown All Tasks</button>
                </form>
            </div>
        </div>
      </div>
  <div class="inner-div-3">
      <jsp:useBean id="api" scope="page" class="com.hutsko.impl.CommentParserApiImpl"/>
      <strong>
  	<jsp:text>Old value:</jsp:text>
  	</strong>${api.hello}<br>
      <jsp:setProperty name="api" property="hello" value="Hello boomer"/>
  	<strong><jsp:text>New value: </jsp:text></strong>
      ${api.hello}

        <div>
            <form id="messages" method="get" action="controller">
                <input type="hidden" name="command" value="showStats">
                <input type="url" name="articleURL" title="Enter onliter's article URL" required/>
                <button type="submit">Show statistic</button>
            </form>
        </div>

        <div></div>
        <div>
            <c:if test="${not empty comments}">
                <h2>All comments below</h2>
                <br>
                <c:forEach items="${comments}" var="comment">
                    <c:out value="${comment.text}"/>
                    <br/>
                </c:forEach>
            </c:if>
        </div>
    </div>
</div>
    <form id="show-deleted" method="get" action="controller">
    <input type="hidden" name="command" value="show-deleted">
    </form>
    <form id="stop-follow" method="get" action="controller">
    <input type="hidden" name="command" value="stopFollow">
    </form>
</body>
</html>
