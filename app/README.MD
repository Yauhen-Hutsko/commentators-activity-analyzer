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

Articles crawler (onliner.by news, every article listed in the section sitemaps)
1. Collects article metadata into the PostgreSQL table `articles` (created automatically,
   schema in `tools/src/main/resources/sql/articles-schema.sql`):
   - discover: reads `https://<section>.onliner.by/sitemap.xml` for people, tech, auto, realt, money (~194k articles).
     Only sitemap-listed articles are collected. tech and auto sitemaps hold max 50,000 URLs each, so their oldest
     articles (roughly before 2012-2013) are not included;
   - enrich: fetches each article page and stores post_id, title, published_at, authors, categories, tags,
     article_type, word_count, views, image_url, is_special and the raw `page_meta` JSON.
2. Run (without `-q`, otherwise Maven hides the crawler's progress logs):
   mvn -DskipTests -pl tools exec:java -Dexec.mainClass=com.hutsko.tools.ArticlesCrawlerCli -Dexec.args=""
   Options: `--from YEAR` / `--to YEAR` (default: all years), `--sections people,tech`, `--pause-ms 1000` (delay between requests), `--limit N` (trial runs),
   `--discover-only`, `--enrich-only`.
3. Resumable: progress is stored per row (`fetch_status`: pending | ok | not_found | error; errors are retried up to 3 times).
   Stop with Ctrl+C and rerun the same command to continue.
4. At the default 1 request/second, fetching all ~194k pages takes roughly 2.5-3 days (resumable).
5. Progress: `select section, fetch_status, count(*) from articles group by 1, 2 order by 1, 2;`

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
1. Two implementations of the same page, both reading `GET /greatanalyzer/api/analytics/registrations?from=YYYY-MM&to=YYYY-MM`
   (both params optional, inclusive), which returns `{"series":[{"month":"2020-08","count":358},...],"total":N}`:
   - `/greatanalyzer/react/` - React + TypeScript + Recharts app in `frontend/` (the approach to develop further).
   - `/greatanalyzer/analytics.html` - plain HTML + vanilla JS + Chart.js (reference only, not developed further).
2. Users are de-duplicated by `user_id` (earliest `created_at_iso` wins); rows without a valid ISO date are skipped.
3. `mvn package` builds the frontend too (frontend-maven-plugin downloads a pinned Node into `frontend/.node`)
   and packs `frontend/dist` into the WAR under `/react`. Skip it with `-Dskip.installnodenpm -Dskip.npm`.
4. Frontend development with hot reload: start the backend (Docker or `tomcat7:run` on 8080), then
   `cd frontend && npm install && npm run dev` and open `http://localhost:5173/greatanalyzer/react/`
   (`/greatanalyzer/api` is proxied to `http://localhost:8080`, override with `API_TARGET`).
   Frontend tests: `npm test`.
5. Local backend without Docker: `mvn -pl tools install -DskipTests && cd app && mvn tomcat7:run -Dmaven.tomcat.port=8080`.
   Note: `tomcat7:run` serves `src/main/webapp` only, so use `npm run dev` for the React app in this mode.
   DB defaults: `jdbc:postgresql://localhost:5432/postgres`, OS user, no password, table `user_profiles_enriched`.
