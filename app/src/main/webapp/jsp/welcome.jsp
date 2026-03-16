<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<fmt:setBundle basename="project" />

<html>
<head>
    <title>Welcome page</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/main.css">
</head>

<body class="app">
<header class="app-header">
    <div>
        <div class="brand">Onliner.by Analyzer</div>
        <div class="meta">version <fmt:message key="project.ver"/></div>
    </div>
    <div class="nav-actions">
        <a class="btn btn-light" href="${pageContext.request.contextPath}/profile.jsp">User profile analytics</a>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/jsp/welcome.jsp">Refresh</a>
    </div>
</header>

<main class="layout">
    <section class="panel">
        <h2>Tracked articles</h2>
        <c:choose>
            <c:when test="${not empty tasks}">
                <ul class="task-list">
                    <c:forEach items="${tasks}" var="task">
                        <li class="task-item">
                            <c:if test="${not empty task.value.previewImageUrl}">
                                <img class="task-thumb" src="${task.value.previewImageUrl}" alt="Preview image"/>
                            </c:if>
                            <div class="task-item-content">
                                <div class="task-title">${task.value.name}</div>
                                <div class="task-meta">
                                    <span>Views: ${task.value.views}</span>
                                    <span>Comments: ${task.value.total}</span>
                                    <span class="chip">PoI: ${task.value.involvedPercent}% (${task.value.trueInvolvedPercent}%)</span>
                                </div>
                                <div class="row-actions">
                                    <button type="submit" form="show-deleted" name="show-deleted" value="${task.key}">Show deleted</button>
                                    <button type="submit" form="stop-follow" name="stopFollow" value="${task.key}">Stop following</button>
                                    <a class="btn btn-light" href="${task.key}" target="_blank">Open article</a>
                                </div>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <p>Tracked articles not found.</p>
            </c:otherwise>
        </c:choose>
    </section>

    <aside class="stack">
        <section class="panel">
            <h3>Track new article</h3>
            <form id="tracked" class="form-grid" method="get" action="controller">
                <input type="hidden" name="command" value="track">
                <label>
                    Article URL
                    <input type="url" name="tracked" required
                           title="Enter onliner's article URL and it will be added to watch list for moderated comments"/>
                </label>
                <label>
                    Polling interval (minutes)
                    <input type="number" name="interval" min="1" max="999" required
                           title="Enter polling interval in minutes. Max 999"/>
                </label>
                <button type="submit">Track article</button>
            </form>
        </section>

        <section class="panel">
            <h3>Utilities</h3>
            <div class="form-grid">
                <form id="messages" method="get" action="controller">
                    <input type="hidden" name="command" value="showStats">
                    <label>
                        Article URL
                        <input type="url" name="articleURL" required title="Enter onliner's article URL"/>
                    </label>
                    <button class="btn-secondary" type="submit">Show statistics</button>
                </form>
                <form id="task" method="get" action="controller">
                    <input type="hidden" name="command" value="shutdownAll">
                    <button class="btn-light" type="submit">Shutdown all tasks</button>
                </form>
                <c:if test="${not empty userProfilesStatus}">
                    <p class="muted">${userProfilesStatus}</p>
                </c:if>
            </div>
        </section>

        <section class="panel">
            <h3>Dev info</h3>
            <p class="muted">Visible only for local debugging.</p>
            <details>
                <summary>Request context</summary>
                <p>ParamValues: ${paramValues}</p>
                <p>Request attributes: ${request.attributeNames}</p>
                <p>Request params: ${request.parameterNames}</p>
                <p>Saved in ServletContext: ${servletContext.getAttribute('servCont')}</p>
                <p>Params:</p>
                <c:forEach items="${param}" var="item">
                    <span><c:out value="${item}"/></span><br/>
                </c:forEach>
            </details>
            <div class="divider"></div>
            <jsp:useBean id="api" scope="page" class="com.hutsko.impl.CommentParserApiImpl"/>
            <strong><jsp:text>Old value:</jsp:text></strong> ${api.hello}<br>
            <jsp:setProperty name="api" property="hello" value="Hello boomer"/>
            <strong><jsp:text>New value: </jsp:text></strong> ${api.hello}
        </section>
    </aside>
</main>

<section class="panel" style="margin-top: 20px;">
    <c:if test="${not empty comments}">
        <h2>All comments</h2>
        <div class="divider"></div>
        <c:forEach items="${comments}" var="comment">
            <c:out value="${comment.text}"/><br/>
        </c:forEach>
    </c:if>
</section>

    <form id="show-deleted" method="get" action="controller">
    <input type="hidden" name="command" value="show-deleted">
    </form>
    <form id="stop-follow" method="get" action="controller">
    <input type="hidden" name="command" value="stopFollow">
    </form>
</body>
</html>
