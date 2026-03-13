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
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Crawls the Onliner forum member list and persists unique user profiles to CSV.
 * Uses a Playwright-rendered HTML session to avoid receiving minimal/empty pages.
 */
public class UserProfilesCollection implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(UserProfilesCollection.class);
    private static final String MEMBERLIST_URL = "https://forum.onliner.by/memberlist.php";
    private static final String NAME_PATTERN = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String STORAGE_DIR = "GADB";
    private static final String USERS_FILE = "user_profiles.csv";
    private final int pauseSeconds;
    private volatile boolean stopRequested = false;

    /**
     * @param pauseSeconds pause between page requests to reduce blocking risk
     */
    public UserProfilesCollection(int pauseSeconds) {
        this.pauseSeconds = Math.max(pauseSeconds, 0);
    }

    @Override
    /**
     * Runs a full crawl for all prefixes (a-z, 0-9) and writes new users to CSV.
     */
    public void run() {
        log.info("UserProfilesCollection started at {}", LocalDateTime.now());
        Map<String, UserProfile> collected = new HashMap<>();
        final String cookieHeader = resolveCookieHeader();
        for (int i = 0; i < NAME_PATTERN.length(); i++) {
            if (shouldStop()) {
                break;
            }
            char prefix = NAME_PATTERN.charAt(i);
            collectByPrefix(cookieHeader, prefix, collected);
        }
        if (!collected.isEmpty()) {
            persist(collected);
            log.info("UserProfilesCollection finished. Collected {} users.", collected.size());
        } else {
            log.warn("No users collected.");
        }
    }

    /**
     * Collects users for a given prefix, traversing all pagination links.
     */
    private void collectByPrefix(String cookieHeader, char prefix, Map<String, UserProfile> collected) {
        String startUrl = MEMBERLIST_URL + "?name=" + prefix;
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(startUrl);

        while (!queue.isEmpty()) {
            if (shouldStop()) {
                break;
            }
            String url = queue.removeFirst();
            if (!visited.add(url)) {
                continue;
            }
            Document doc = fetch(cookieHeader, url);
            if (doc == null) {
                continue;
            }
            collectUsers(doc, prefix, collected);
            pauseBetweenRequests();
            for (Element link : doc.select("a[href*='memberlist.php'][href*='name=" + prefix + "']")) {
                String href = link.absUrl("href");
                if (href != null && !href.isEmpty() && !visited.contains(href)) {
                    queue.addLast(href);
                }
            }
        }
    }

    /**
     * Extracts profile links from a member list page.
     */
    private void collectUsers(Document document, char prefix, Map<String, UserProfile> collected) {
        for (Element link : document.select("a[href*='memberlist.php?mode=viewprofile']")) {
            String name = link.text().trim();
            if (name.isEmpty()) {
                continue;
            }
            String key = name.toLowerCase(Locale.ROOT);
            if (!collected.containsKey(key)) {
                collected.put(key, new UserProfile(name, link.absUrl("href"), String.valueOf(prefix)));
            }
        }
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

    private void persist(Map<String, UserProfile> collected) {
        File file = resolveStorageFile();
        Set<String> existing = loadExistingUserKeys(file);
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (writeHeader) {
                writer.write("username,profile_url,source_prefix");
                writer.newLine();
            }
            int appended = 0;
            for (UserProfile profile : collected.values()) {
                String key = profile.username.toLowerCase(Locale.ROOT);
                if (existing.contains(key)) {
                    continue;
                }
                writer.write(csv(profile.username));
                writer.write(",");
                writer.write(csv(profile.profileUrl));
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
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                    existing.add(parts[0].trim().toLowerCase(Locale.ROOT));
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

    private static class UserProfile {
        private final String username;
        private final String profileUrl;
        private final String sourcePrefix;

        private UserProfile(String username, String profileUrl, String sourcePrefix) {
            this.username = username;
            this.profileUrl = profileUrl == null ? "" : profileUrl;
            this.sourcePrefix = sourcePrefix;
        }
    }
}
