<%@ page import="com.hutsko.tools.services.util.UserProfilesCollectionScheduler" %>
<%@ page import="com.hutsko.tools.services.util.UserProfilesEnrichmentScheduler" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.util.Properties" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>User Profile Analytics</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
</head>
<body class="app">
<header class="app-header">
    <div>
        <div class="brand">User Profile Analytics</div>
        <div class="meta">Standalone view for upcoming backend integration</div>
    </div>
    <div class="nav-actions">
        <a class="btn btn-light" href="jsp/welcome.jsp">Back to dashboard</a>
    </div>
</header>

<main class="layout">
    <section class="panel stack">
        <div>
            <h2>Profile snapshot</h2>
            <p class="muted">Placeholder data. Replace with backend response later.</p>
        </div>
        <div class="metrics">
            <div class="metric">
                <span class="muted">User</span>
                <strong>onliner_critic</strong>
            </div>
            <div class="metric">
                <span class="muted">Total comments</span>
                <strong>1,274</strong>
            </div>
            <div class="metric">
                <span class="muted">Avg. replies</span>
                <strong>12.8</strong>
            </div>
            <div class="metric">
                <span class="muted">PoI</span>
                <strong>67%</strong>
            </div>
        </div>

        <div class="divider"></div>

        <div class="bars">
            <div class="bar">
                <div class="muted">Tech posts</div>
                <div class="bar-track"><div class="bar-fill" style="width: 82%;"></div></div>
            </div>
            <div class="bar">
                <div class="muted">Economy</div>
                <div class="bar-track"><div class="bar-fill" style="width: 48%;"></div></div>
            </div>
            <div class="bar">
                <div class="muted">Gadgets</div>
                <div class="bar-track"><div class="bar-fill" style="width: 64%;"></div></div>
            </div>
            <div class="bar">
                <div class="muted">Off-topic</div>
                <div class="bar-track"><div class="bar-fill" style="width: 22%;"></div></div>
            </div>
        </div>
    </section>

    <aside class="stack">
        <section class="panel">
            <h3>Admin tools</h3>
            <%
                String crawlerStatus = (String) request.getAttribute("userProfilesStatus");
                String enrichStatus = (String) request.getAttribute("userProfilesEnrichmentStatus");
                boolean crawlerRunning = UserProfilesCollectionScheduler.isRunning();
                boolean enrichRunning = UserProfilesEnrichmentScheduler.isRunning();
                String base = System.getProperty("catalina.base");
                if (base == null || base.trim().isEmpty()) {
                    base = ".";
                }
                File progressFile = new File(new File(base, "GADB"), "user_profiles.enrich.progress");
                String lastProcessed = "n/a";
                if (progressFile.exists()) {
                    Properties props = new Properties();
                    try (FileInputStream in = new FileInputStream(progressFile)) {
                        props.load(in);
                        lastProcessed = props.getProperty("processedRows", "0");
                    } catch (Exception ignored) {
                        lastProcessed = "n/a";
                    }
                }
            %>
            <div class="task-meta">
                <span class="chip">Crawler: <%= crawlerRunning ? "running" : "stopped" %></span>
                <span class="chip">Enrichment: <%= enrichRunning ? "running" : "stopped" %></span>
                <span class="chip">Enriched rows: <%= lastProcessed %></span>
            </div>
            <% if (crawlerStatus != null && !crawlerStatus.trim().isEmpty()) { %>
                <p class="muted"><%= crawlerStatus %></p>
            <% } %>
            <% if (enrichStatus != null && !enrichStatus.trim().isEmpty()) { %>
                <p class="muted"><%= enrichStatus %></p>
            <% } %>
            <form class="form-grid" method="post" action="controller">
                <input type="hidden" name="command" value="startUserCrawl"/>
                <label>
                    Admin password
                    <input type="password" name="adminPassword" placeholder="Required for crawler" required/>
                </label>
                <button class="btn-secondary" type="submit">Start user profile crawl</button>
            </form>
            <form class="form-grid" method="post" action="controller" style="margin-top: 10px;">
                <input type="hidden" name="command" value="stopUserCrawl"/>
                <button class="btn-light" type="submit">Stop crawler</button>
            </form>
            <div class="divider"></div>
            <form class="form-grid" method="post" action="controller">
                <input type="hidden" name="command" value="startUserEnrich"/>
                <label>
                    Admin password
                    <input type="password" name="adminPassword" placeholder="Required for enrichment" required/>
                </label>
                <button class="btn-secondary" type="submit">Start profile enrichment</button>
            </form>
            <form class="form-grid" method="post" action="controller" style="margin-top: 10px;">
                <input type="hidden" name="command" value="stopUserEnrich"/>
                <button class="btn-light" type="submit">Stop enrichment</button>
            </form>
        </section>
        <section class="panel">
            <h3>Activity trend</h3>
            <p class="muted">Weekly activity, last 8 weeks.</p>
            <div class="bars">
                <div class="bar">
                    <div class="muted">W-1</div>
                    <div class="bar-track"><div class="bar-fill" style="width: 40%;"></div></div>
                </div>
                <div class="bar">
                    <div class="muted">W-2</div>
                    <div class="bar-track"><div class="bar-fill" style="width: 55%;"></div></div>
                </div>
                <div class="bar">
                    <div class="muted">W-3</div>
                    <div class="bar-track"><div class="bar-fill" style="width: 70%;"></div></div>
                </div>
                <div class="bar">
                    <div class="muted">W-4</div>
                    <div class="bar-track"><div class="bar-fill" style="width: 30%;"></div></div>
                </div>
            </div>
        </section>

        <section class="panel">
            <h3>Top signals</h3>
            <div class="task-meta">
                <span class="chip">Insightful</span>
                <span class="chip">Debate starter</span>
                <span class="chip">High reply rate</span>
            </div>
            <div class="divider"></div>
            <p class="muted">Next: include sentiment histogram and user clustering.</p>
        </section>
    </aside>
</main>
</body>
</html>
