Project layout
1. Parent (root) Maven project: builds all modules.
2. app: web application (WAR).
3. tools: crawler + debug utilities.

Build and deploy (multi-module)
1. From the repo root:
   mvn -U -DskipTests -pl app -am clean install org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:deploy
2. Add -X to enable Maven debug logging.

Tomcat initial setup
1. Download and extract Tomcat 9 (apache-tomcat-9.0.67-windows-x64.zip).
2. Update the HTTP port in conf/server.xml.
3. Configure users/roles in conf/tomcat-users.xml.
4. Create folder GADB under the Tomcat base directory.

Web UI features
1. Track article: open the main dashboard and submit an article URL with an interval. (Deprecated: comment API does not return data anymore)
2. User profile analytics page: use the Admin tools section to start/stop the user profile crawler and the enrichment crawler.

Crawler cookies
1. Default cookie file (used automatically): target/onliner_cookies.txt (repo root).
2. Override with JVM flags:
   -Donliner.cookies=<COOKIE_HEADER>
   -Donliner.cookies.file=/absolute/path/to/cookies.txt

Tools module usage
1. Install Playwright browsers once:
   mvn -q -DskipTests -pl tools exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
2. Run network sniffer:
   mvn -q -DskipTests -pl tools exec:java -Dexec.mainClass=com.hutsko.tools.util.OnlinerNetworkSniffer -Dexec.args="https://forum.onliner.by/memberlist.php?name=f @/absolute/path/to/cookies.txt"
3. Run HTML debug fetcher:
   mvn -q -DskipTests -pl tools exec:java -Dexec.mainClass=com.hutsko.tools.util.HttpDebugFetcher -Dexec.args="https://forum.onliner.by/memberlist.php?name=f @/absolute/path/to/cookies.txt"
4. Run crawler CLI: 
   mvn -q -DskipTests -pl tools exec:java -Dexec.mainClass=com.hutsko.tools.UserProfilesCollectorCli -Dexec.args="5"
5. Run profile enrichment CLI:
   mvn -q -DskipTests -pl tools exec:java -Dexec.mainClass=com.hutsko.tools.UserProfilesEnrichmentCli -Dexec.args="2"
6. CSV output location for Tomcat runs:
   ${catalina.base}/GADB/user_profiles.csv

PostgreSQL connector
1. Default connection settings are resolved from system properties first, then environment variables.
2. Supported keys:
   - `db.url` / `DB_URL`
   - `db.user` / `DB_USER`
   - `db.password` / `DB_PASSWORD`
   - `db.profile.table` / `DB_PROFILE_TABLE`
   - `db.query.timeout.seconds` / `DB_QUERY_TIMEOUT_SECONDS`
3. The backend repository can read enriched profiles or execute raw SQL for chart data preparation.

Docker
1. Copy `.env.example` to `.env` (git-ignored) and set `DB_USER` / `DB_PASSWORD` for your PostgreSQL.
   Never commit `.env`; only `.env.example` with placeholders is tracked.
2. `docker compose up --build` starts the app only; it reads the PostgreSQL running on your machine
   via `host.docker.internal:5432`.
3. The app is exposed on `http://localhost:8080/greatanalyzer`.
4. Optional throwaway DB with sample rows: set `SANDBOX_DB_PASSWORD` and
   `DB_URL=jdbc:postgresql://db:5432/commentators` in `.env`, then `docker compose --profile sandbox up --build`
   (exposed on `localhost:5433`).

Registration analytics
1. Page: `/greatanalyzer/analytics.jsp` (linked from the dashboard header).
2. API: `GET /greatanalyzer/api/analytics/registrations?from=YYYY-MM&to=YYYY-MM` (both optional, inclusive)
   returns `{"series":[{"month":"2020-08","count":358},...],"total":N}`.
3. Users are de-duplicated by `user_id` (earliest `created_at_iso` wins); rows without a valid ISO date are skipped.
4. Local run without Docker: `mvn -pl tools install -DskipTests && cd app && mvn tomcat7:run -Dmaven.tomcat.port=8090`.
   DB defaults: `jdbc:postgresql://localhost:5432/postgres`, OS user, no password, table `user_profiles_enriched`.
