package com.hutsko.tools.services;

import com.hutsko.tools.util.HttpDebugFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Crawls the Onliner forum member list and persists unique user profiles to CSV.
 * Uses a Playwright-rendered HTML session to avoid receiving minimal/empty pages.
 * Resumes from the last saved prefix/start offset and appends after each prefix.
 */
public class UserProfilesCollection implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(UserProfilesCollection.class);
    private static final String MEMBERLIST_URL = "https://forum.onliner.by/memberlist.php";
    private static final String NAME_PATTERN = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String STORAGE_DIR = "GADB";
    private static final String USERS_FILE = "user_profiles.csv";
    private static final String PROGRESS_FILE = "user_profiles.progress";
    private final int pauseSeconds;
    private volatile boolean stopRequested = false;

    /**
     * @param pauseSeconds pause between page requests to reduce blocking risk
     */
    public UserProfilesCollection(int pauseSeconds) {
        this.pauseSeconds = Math.max(pauseSeconds, 0);
    }

    /**
     * Runs a full crawl for all prefixes (a-z, 0-9) and writes new users to CSV.
     */
    @Override
    public void run() {
        log.info("UserProfilesCollection started at {}", LocalDateTime.now());
        Map<String, UserProfile> batch = new HashMap<>();
        Set<String> seenKeys = loadExistingUserKeys(resolveStorageFile());
        Progress progress = loadProgress();
        final String cookieHeader = resolveCookieHeader();
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            log.warn("Cookie header is empty. Server may return minimal HTML without user data.");
        }

        for (int i = progress.prefixIndex; i < NAME_PATTERN.length(); i++) {
            if (shouldStop()) {
                persist(batch);
                return;
            }
            char prefix = NAME_PATTERN.charAt(i);
            int start = (i == progress.prefixIndex) ? progress.startOffset : 0;
            collectByPrefix(cookieHeader, prefix, start, batch, seenKeys);
            persist(batch);
            batch.clear();
            saveProgress(i + 1, 0);
        }
        log.info("UserProfilesCollection finished.");
    }

    /**
     * Collects users for a given prefix, traversing all pagination links.
     */
    private void collectByPrefix(String cookieHeader, char prefix, int start,
                                 Map<String, UserProfile> batch, Set<String> seenKeys) {
        int currentStart = start;
        while (true) {
            if (shouldStop()) {
                saveProgress(prefixIndex(prefix), currentStart);
                return;
            }
            String url = buildUrl(prefix, currentStart);
            Document doc = fetchWithRetry(cookieHeader, url);
            if (doc == null) {
                saveProgress(prefixIndex(prefix), currentStart);
                return;
            }
            int added = collectUsers(doc, prefix, batch, seenKeys);
            if (added < 0) {
                saveProgress(prefixIndex(prefix), currentStart);
                return;
            }
            int nextStart = findNextStart(doc, prefix, currentStart);
            if (nextStart <= currentStart) {
                return;
            }
            saveProgress(prefixIndex(prefix), nextStart);
            pauseBetweenRequests();
            currentStart = nextStart;
        }
    }

    /**
     * Extracts user rows from the member list table.
     */
    private int collectUsers(Document document, char prefix, Map<String, UserProfile> batch, Set<String> seenKeys) {
        Element table = document.selectFirst("div.b-userlist table");
        if (table == null) {
            log.warn("User list table not found for prefix {}", prefix);
            return -1;
        }
        int added = 0;
        for (Element row : table.select("tr")) {
            Element userTd = row.selectFirst("td.user");
            if (userTd == null) {
                continue;
            }
            Element profileLink = userTd.selectFirst("a[href]");
            String username = profileLink == null ? "" : profileLink.text().trim();
            if (username.isEmpty()) {
                continue;
            }
            String profileUrl = profileLink == null ? "" : profileLink.absUrl("href");
            String userId = extractUserId(row, profileUrl);
            String key = buildUserKey(userId, profileUrl, username);
            if (!seenKeys.contains(key) && !batch.containsKey(key)) {
                String avatarUrl = "";
                Element avatarImg = row.selectFirst("td.ph img");
                if (avatarImg != null) {
                    avatarUrl = avatarImg.absUrl("src");
                }
                String status = "";
                Element statusEl = row.selectFirst("td.user-status span");
                if (statusEl != null) {
                    status = statusEl.text().trim();
                }
                String messages = "";
                Element msgEl = row.selectFirst("td.u-msgs");
                if (msgEl != null) {
                    messages = msgEl.text().trim();
                }
                String rankTitle = extractRankTitle(userTd, username);
                String yearsOnSite = extractYearsOnSite(row);
                batch.put(key, new UserProfile(userId, username, profileUrl, avatarUrl, rankTitle, status, messages, yearsOnSite, String.valueOf(prefix)));
                seenKeys.add(key);
                added++;
            }
        }
        return added;
    }

    /**
     * Fetches a fully rendered HTML page via Playwright and parses it with Jsoup.
     */
    private Document fetch(String cookieHeader, String url) {
        try {
            String html = HttpDebugFetcher.fetchResponse(url, cookieHeader).body();
            return Jsoup.parse(html, url);
        } catch (Exception e) {
            log.warn("Failed to fetch {}", url, e);
            return null;
        }
    }

    private Document fetchWithRetry(String cookieHeader, String url) {
        int attempts = 0;
        while (attempts < 3 && !shouldStop()) {
            attempts++;
            Document doc = fetch(cookieHeader, url);
            if (doc != null) {
                return doc;
            }
            if (attempts < 3) {
                pauseBetweenRequests();
            }
        }
        return null;
    }

    private void pauseBetweenRequests() {
        if (pauseSeconds <= 0) {
            return;
        }
        try {
            Thread.sleep(pauseSeconds * 1000L);
        } catch (InterruptedException e) {
            stopRequested = true;
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Signals the crawler to stop as soon as possible.
     */
    public void requestStop() {
        stopRequested = true;
    }

    private boolean shouldStop() {
        return stopRequested || Thread.currentThread().isInterrupted();
    }

    /**
     * Resolves cookies in priority order:
     * 1) -Donliner.cookies
     * 2) -Donliner.cookies.file
     * 3) target/onliner_cookies.txt
     */
    private String resolveCookieHeader() {
        String cookieHeader = System.getProperty("onliner.cookies");
        if (cookieHeader != null && !cookieHeader.trim().isEmpty()) {
            return cookieHeader.trim();
        }
        String cookieFile = System.getProperty("onliner.cookies.file");
        if (cookieFile == null || cookieFile.trim().isEmpty()) {
            cookieFile = resolveDefaultCookiePath();
        }
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(cookieFile));
            return new String(bytes, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.warn("Failed to read cookies file {}", cookieFile, e);
            return null;
        }
    }

    private String resolveDefaultCookiePath() {
        String base = System.getProperty("catalina.base");
        if (base != null && !base.trim().isEmpty()) {
            String inGadb = base + File.separator + STORAGE_DIR + File.separator + "onliner_cookies.txt";
            if (new File(inGadb).exists()) {
                return inGadb;
            }
            String inTemp = base + File.separator + "temp" + File.separator + "onliner_cookies.txt";
            if (new File(inTemp).exists()) {
                return inTemp;
            }
        }
        return "target/onliner_cookies.txt";
    }

    private void persist(Map<String, UserProfile> batch) {
        if (batch.isEmpty()) {
            return;
        }
        File file = resolveStorageFile();
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (writeHeader) {
                writer.write("user_id,username,profile_url,avatar_url,rank_title,status,messages,years_on_site,source_prefix");
                writer.newLine();
            }
            int appended = 0;
            for (UserProfile profile : batch.values()) {
                writer.write(csv(profile.userId));
                writer.write(",");
                writer.write(csv(profile.username));
                writer.write(",");
                writer.write(csv(profile.profileUrl));
                writer.write(",");
                writer.write(csv(profile.avatarUrl));
                writer.write(",");
                writer.write(csv(profile.rankTitle));
                writer.write(",");
                writer.write(csv(profile.status));
                writer.write(",");
                writer.write(csv(profile.messages));
                writer.write(",");
                writer.write(csv(profile.yearsOnSite));
                writer.write(",");
                writer.write(csv(profile.sourcePrefix));
                writer.newLine();
                appended++;
            }
            log.info("Appended {} new users to {}", appended, file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write users to CSV", e);
        }
    }

    private Set<String> loadExistingUserKeys(File file) {
        Set<String> existing = new HashSet<>();
        if (!file.exists()) {
            return existing;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            int idxUserId = -1;
            int idxProfileUrl = -1;
            int idxUsername = -1;
            if (header != null) {
                String[] cols = parseCsvLine(header);
                for (int i = 0; i < cols.length; i++) {
                    String name = cols[i].trim().toLowerCase(Locale.ROOT);
                    if ("user_id".equals(name)) {
                        idxUserId = i;
                    } else if ("profile_url".equals(name)) {
                        idxProfileUrl = i;
                    } else if ("username".equals(name)) {
                        idxUsername = i;
                    }
                }
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = parseCsvLine(line);
                String userId = valueAt(row, idxUserId);
                String profileUrl = valueAt(row, idxProfileUrl);
                String username = valueAt(row, idxUsername);
                String key = buildUserKey(userId, profileUrl, username);
                if (!key.trim().isEmpty()) {
                    existing.add(key);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read existing CSV", e);
        }
        return existing;
    }

    private File resolveStorageFile() {
        String base = System.getProperty("catalina.base");
        if (base == null || base.trim().isEmpty()) {
            base = ".";
        }
        File dir = new File(base, STORAGE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("Failed to create storage directory {}", dir.getAbsolutePath());
        }
        return new File(dir, USERS_FILE);
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String sanitized = value.replace("\"", "\"\"");
        return "\"" + sanitized + "\"";
    }

    private String valueAt(String[] row, int index) {
        if (index < 0 || index >= row.length) {
            return "";
        }
        return row[index];
    }

    private String[] parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    cells.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        cells.add(sb.toString());
        return cells.toArray(new String[0]);
    }

    private static class UserProfile {
        private final String userId;
        private final String username;
        private final String profileUrl;
        private final String avatarUrl;
        private final String rankTitle;
        private final String status;
        private final String messages;
        private final String yearsOnSite;
        private final String sourcePrefix;

        private UserProfile(String userId, String username, String profileUrl, String avatarUrl,
                            String rankTitle, String status, String messages, String yearsOnSite, String sourcePrefix) {
            this.userId = userId == null ? "" : userId;
            this.username = username;
            this.profileUrl = profileUrl == null ? "" : profileUrl;
            this.avatarUrl = avatarUrl == null ? "" : avatarUrl;
            this.rankTitle = rankTitle == null ? "" : rankTitle;
            this.status = status == null ? "" : status;
            this.messages = messages == null ? "" : messages;
            this.yearsOnSite = yearsOnSite == null ? "" : yearsOnSite;
            this.sourcePrefix = sourcePrefix;
        }
    }

    private String extractUserId(Element row, String profileUrl) {
        Element statusTd = row.selectFirst("td.user-status");
        if (statusTd != null) {
            String id = statusTd.attr("data-user-id");
            if (!id.trim().isEmpty()) {
                return id.trim();
            }
        }
        Element rateStat = row.selectFirst("td.rate-stat span");
        if (rateStat != null && !rateStat.text().trim().isEmpty()) {
            return rateStat.text().trim();
        }
        if (profileUrl != null && profileUrl.matches(".*?/user/\\d+.*")) {
            return profileUrl.replaceAll(".*?/user/(\\d+).*", "$1");
        }
        return "";
    }

    private String extractRankTitle(Element userTd, String username) {
        String text = userTd.text().trim();
        if (text.startsWith(username)) {
            return text.substring(username.length()).trim();
        }
        return text;
    }

    private String extractYearsOnSite(Element row) {
        for (Element td : row.select("td")) {
            String text = td.text().trim();
            if (text.contains("лет на сайте") || text.contains("год на сайте") || text.contains("года на сайте")) {
                return text;
            }
        }
        return "";
    }

    private String buildUserKey(String userId, String profileUrl, String username) {
        if (userId != null && !userId.trim().isEmpty()) {
            return userId.trim().toLowerCase(Locale.ROOT);
        }
        if (profileUrl != null && !profileUrl.trim().isEmpty()) {
            return profileUrl.trim().toLowerCase(Locale.ROOT);
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String buildUrl(char prefix, int start) {
        if (start <= 0) {
            return MEMBERLIST_URL + "?name=" + prefix;
        }
        return MEMBERLIST_URL + "?name=" + prefix + "&start=" + start;
    }

    private int findNextStart(Document doc, char prefix, int currentStart) {
        int next = -1;
        for (Element link : doc.select("a[href*='memberlist.php'][href*='name=" + prefix + "'][href*='start=']")) {
            String href = link.absUrl("href");
            int start = extractStartParam(href);
            if (start > currentStart && (next < 0 || start < next)) {
                next = start;
            }
        }
        return next;
    }

    private int extractStartParam(String url) {
        if (url == null) {
            return -1;
        }
        int idx = url.indexOf("start=");
        if (idx < 0) {
            return -1;
        }
        String value = url.substring(idx + 6).replaceAll("[^0-9].*$", "");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int prefixIndex(char prefix) {
        return NAME_PATTERN.indexOf(prefix);
    }

    private Progress loadProgress() {
        File file = resolveProgressFile();
        if (!file.exists()) {
            return new Progress(0, 0);
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            int idx = Integer.parseInt(props.getProperty("prefixIndex", "0"));
            int start = Integer.parseInt(props.getProperty("startOffset", "0"));
            return new Progress(Math.max(0, idx), Math.max(0, start));
        } catch (Exception e) {
            log.warn("Failed to read progress file {}", file.getAbsolutePath(), e);
            return new Progress(0, 0);
        }
    }

    private void saveProgress(int prefixIndex, int startOffset) {
        File file = resolveProgressFile();
        Properties props = new Properties();
        props.setProperty("prefixIndex", String.valueOf(prefixIndex));
        props.setProperty("startOffset", String.valueOf(startOffset));
        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "User profiles crawl progress");
        } catch (IOException e) {
            log.warn("Failed to write progress file {}", file.getAbsolutePath(), e);
        }
    }

    private File resolveProgressFile() {
        String base = System.getProperty("catalina.base");
        if (base == null || base.trim().isEmpty()) {
            base = ".";
        }
        File dir = new File(base, STORAGE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("Failed to create storage directory {}", dir.getAbsolutePath());
        }
        return new File(dir, PROGRESS_FILE);
    }

    private static class Progress {
        private final int prefixIndex;
        private final int startOffset;

        private Progress(int prefixIndex, int startOffset) {
            this.prefixIndex = prefixIndex;
            this.startOffset = startOffset;
        }
    }
}
