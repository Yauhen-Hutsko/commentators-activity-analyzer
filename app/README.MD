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
2. User profile analytics page: use the Admin tools section to start/stop the user profile crawler.

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