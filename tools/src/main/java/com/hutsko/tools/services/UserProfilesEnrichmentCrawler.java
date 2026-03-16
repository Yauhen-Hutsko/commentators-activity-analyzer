package com.hutsko.tools.services;

import com.hutsko.tools.util.HttpDebugFetcher;
import com.hutsko.tools.util.OnlinerNetworkSniffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enriches an existing user CSV by visiting profile pages and appending creation date
 * plus selected raw profile details for further parsing. Supports stop/resume with
 * a progress file to continue mid-file.
 */
public class UserProfilesEnrichmentCrawler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(UserProfilesEnrichmentCrawler.class);
    private static final String STORAGE_DIR = "GADB";
    private static final String USERS_FILE = "user_profiles.csv";
    private static final String USERS_FILE_V1 = "user_profiles(v1).csv";
    private static final String ENRICHED_FILE = "user_profiles.enriched.csv";
    private static final String PROGRESS_FILE = "user_profiles.enrich.progress";
    private static final int DEFAULT_PAUSE_SECONDS = 2;
    private static final int MAX_RETRIES = 3;

    private final int pauseSeconds;
    private volatile boolean stopRequested = false;

    public UserProfilesEnrichmentCrawler() {
        this(DEFAULT_PAUSE_SECONDS);
    }

    public UserProfilesEnrichmentCrawler(int pauseSeconds) {
        this.pauseSeconds = Math.max(pauseSeconds, 0);
    }

    @Override
    public void run() {
        String cookieHeader = resolveCookieHeader();
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            log.warn("Cookie header is empty. Profile pages may return minimal HTML.");
        }
        File input = resolveInputCsv();
        if (input == null) {
            log.warn("No input CSV found to enrich.");
            return;
        }
        File output = new File(input.getParentFile(), ENRICHED_FILE);
        Progress progress = loadProgress(input.getParentFile());
        long processed = Math.max(progress.processedRows, 0);
        boolean resume = processed > 0 && output.exists() && output.length() > 0;
        if (!resume && processed > 0) {
            log.warn("Progress file found but enriched output is missing. Restarting from scratch.");
            processed = 0;
            clearProgress(input.getParentFile());
        }
        OnlinerNetworkSniffer.RenderSession session = null;
        try {
            session = OnlinerNetworkSniffer.RenderSession.open(cookieHeader);
        } catch (Exception e) {
            log.warn("Failed to open Playwright session, falling back to non-rendered fetches.", e);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output, resume), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null) {
                log.warn("Input CSV is empty: {}", input.getAbsolutePath());
                return;
            }
            CsvSchema schema = CsvSchema.parse(header);
            if (!schema.hasColumn("created_at_raw")) {
                schema.appendColumn("created_at_raw");
            }
            if (!schema.hasColumn("created_at_iso")) {
                schema.appendColumn("created_at_iso");
            }
            if (!schema.hasColumn("profile_details_raw")) {
                schema.appendColumn("profile_details_raw");
            }
            schema.removeColumn("profile_details_raw");
            schema.removeColumn("profile_onliner_id");
            schema.removeColumn("profile_fields_raw");
            if (!resume) {
                writer.write(schema.toHeader());
                writer.newLine();
            }

            long toSkip = processed;
            while (toSkip > 0) {
                if (reader.readLine() == null) {
                    log.warn("Progress offset {} exceeds input rows. Restarting from beginning.", processed);
                    processed = 0;
                    toSkip = 0;
                    if (!resume) {
                        writer.write(schema.toHeader());
                        writer.newLine();
                    }
                    break;
                }
                toSkip--;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (shouldStop()) {
                    saveProgress(input.getParentFile(), processed);
                    return;
                }
                String[] cells = schema.parseRow(line);
                String profileUrl = schema.get(cells, "profile_url");
                String createdIso = schema.get(cells, "created_at_iso");
                String createdRaw = schema.get(cells, "created_at_raw");

                if (isBlank(createdIso) || isBlank(createdRaw)) {
                    ProfileInfo info = fetchProfileInfoWithRetry(session, profileUrl, cookieHeader);
                    schema.set(cells, "created_at_raw", info.createdAtRaw);
                    schema.set(cells, "created_at_iso", info.createdAtIso);
                    pauseBetweenRequests();
                }

                writer.write(schema.joinRow(cells));
                writer.newLine();
                processed++;
                if (processed % 200 == 0) {
                    writer.flush();
                    saveProgress(input.getParentFile(), processed);
                    log.info("Enriched {} profiles", processed);
                }
            }
        } catch (IOException e) {
            log.error("Failed to enrich profiles", e);
            return;
        } finally {
            if (session != null) {
                session.close();
            }
        }

        if (shouldStop()) {
            saveProgress(input.getParentFile(), processed);
            return;
        }

        if (!input.delete() || !output.renameTo(input)) {
            log.warn("Enrichment finished but could not replace original file. Output: {}", output.getAbsolutePath());
        } else {
            clearProgress(input.getParentFile());
            log.info("Enrichment finished. Updated file: {}", input.getAbsolutePath());
        }
    }

    private ProfileInfo fetchProfileInfoWithRetry(OnlinerNetworkSniffer.RenderSession session,
                                                  String profileUrl,
                                                  String cookieHeader) {
        int attempts = 0;
        while (attempts < MAX_RETRIES && !shouldStop()) {
            attempts++;
            ProfileInfo info = fetchProfileInfoOnce(session, profileUrl, cookieHeader);
            if (info.fetchOk || attempts >= MAX_RETRIES) {
                return info;
            }
            pauseBetweenRequests();
        }
        return ProfileInfo.empty();
    }

    private ProfileInfo fetchProfileInfoOnce(OnlinerNetworkSniffer.RenderSession session,
                                             String profileUrl,
                                             String cookieHeader) {
        if (profileUrl == null || profileUrl.trim().isEmpty()) {
            return ProfileInfo.empty();
        }
        try {
            String html;
            if (session != null) {
                html = session.fetchRendered(profileUrl, 1500);
            } else {
                html = HttpDebugFetcher.fetchResponse(profileUrl, cookieHeader).body();
            }
            Document doc = Jsoup.parse(html, profileUrl);
            String createdRaw = extractCreatedRaw(extractDetails(doc));
            String createdIso = toIsoDate(createdRaw);
            return new ProfileInfo(createdRaw, createdIso, true);
        } catch (Exception e) {
            log.warn("Failed to fetch profile {}", profileUrl, e);
            return ProfileInfo.failed();
        }
    }

    private String extractDetails(Document doc) {
        List<String> details = doc.select(".profile-header__details-item").eachText();
        if (details.isEmpty()) {
            return "";
        }
        return String.join(" | ", details).trim();
    }

    private String extractCreatedRaw(String detailsRaw) {
        if (detailsRaw == null) {
            return "";
        }
        Pattern pattern = Pattern.compile("с\\s+(\\d{1,2}\\s+\\p{L}+\\s+\\d{4})");
        Matcher matcher = pattern.matcher(detailsRaw);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private String toIsoDate(String createdRaw) {
        if (createdRaw == null || createdRaw.trim().isEmpty()) {
            return "";
        }
        String[] parts = createdRaw.trim().split("\\s+");
        if (parts.length < 3) {
            return "";
        }
        Integer month = monthNumber(parts[1]);
        if (month == null) {
            return "";
        }
        try {
            int day = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[2]);
            LocalDate date = LocalDate.of(year, month, day);
            return date.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private Integer monthNumber(String ruMonth) {
        String m = ruMonth.toLowerCase(Locale.ROOT);
        switch (m) {
            case "января":
                return 1;
            case "февраля":
                return 2;
            case "марта":
                return 3;
            case "апреля":
                return 4;
            case "мая":
                return 5;
            case "июня":
                return 6;
            case "июля":
                return 7;
            case "августа":
                return 8;
            case "сентября":
                return 9;
            case "октября":
                return 10;
            case "ноября":
                return 11;
            case "декабря":
                return 12;
            default:
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
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Signals the enrichment crawler to stop as soon as possible.
     */
    public void requestStop() {
        stopRequested = true;
    }

    private boolean shouldStop() {
        return stopRequested || Thread.currentThread().isInterrupted();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private File resolveInputCsv() {
        String base = System.getProperty("catalina.base");
        if (base == null || base.trim().isEmpty()) {
            base = ".";
        }
        File dir = new File(base, STORAGE_DIR);
        File current = new File(dir, USERS_FILE);
        if (current.exists()) {
            return current;
        }
        File legacy = new File(dir, USERS_FILE_V1);
        return legacy.exists() ? legacy : null;
    }

    private Progress loadProgress(File baseDir) {
        File file = new File(baseDir, PROGRESS_FILE);
        if (!file.exists()) {
            return new Progress(0);
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            long processedRows = Long.parseLong(props.getProperty("processedRows", "0"));
            return new Progress(Math.max(0, processedRows));
        } catch (Exception e) {
            log.warn("Failed to read progress file {}", file.getAbsolutePath(), e);
            return new Progress(0);
        }
    }

    private void saveProgress(File baseDir, long processedRows) {
        File file = new File(baseDir, PROGRESS_FILE);
        Properties props = new Properties();
        props.setProperty("processedRows", String.valueOf(processedRows));
        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "User profiles enrichment progress");
        } catch (IOException e) {
            log.warn("Failed to write progress file {}", file.getAbsolutePath(), e);
        }
    }

    private void clearProgress(File baseDir) {
        File file = new File(baseDir, PROGRESS_FILE);
        if (file.exists() && !file.delete()) {
            log.warn("Failed to delete progress file {}", file.getAbsolutePath());
        }
    }

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

    private static class ProfileInfo {
        private final String createdAtRaw;
        private final String createdAtIso;
        private final boolean fetchOk;

        private ProfileInfo(String createdAtRaw, String createdAtIso, boolean fetchOk) {
            this.createdAtRaw = createdAtRaw == null ? "" : createdAtRaw;
            this.createdAtIso = createdAtIso == null ? "" : createdAtIso;
            this.fetchOk = fetchOk;
        }

        private static ProfileInfo empty() {
            return new ProfileInfo("", "", true);
        }

        private static ProfileInfo failed() {
            return new ProfileInfo("", "", false);
        }
    }

    private static class Progress {
        private final long processedRows;

        private Progress(long processedRows) {
            this.processedRows = processedRows;
        }
    }

    private static class CsvSchema {
        private final List<String> columns;
        private final List<Integer> outputIndices;
        private final java.util.Map<String, Integer> indexByName;

        private static CsvSchema parse(String headerLine) {
            List<String> cols = parseCsvLine(headerLine);
            List<Integer> output = new ArrayList<>();
            java.util.Map<String, Integer> idx = new java.util.HashMap<>();
            for (int i = 0; i < cols.size(); i++) {
                idx.put(cols.get(i).trim(), i);
                output.add(i);
            }
            CsvSchema schema = new CsvSchema(cols, output, idx);
            return schema;
        }

        private CsvSchema(List<String> columns,
                          List<Integer> outputIndices,
                          java.util.Map<String, Integer> indexByName) {
            this.columns = columns;
            this.outputIndices = outputIndices;
            this.indexByName = indexByName;
        }

        private boolean hasColumn(String name) {
            return indexByName.containsKey(name);
        }

        private void appendColumn(String name) {
            if (indexByName.containsKey(name)) {
                return;
            }
            columns.add(name);
            indexByName.put(name, columns.size() - 1);
            outputIndices.add(-1);
        }

        private void removeColumn(String name) {
            Integer idx = indexByName.get(name);
            if (idx == null) {
                return;
            }
            int index = idx;
            columns.remove(index);
            outputIndices.remove(index);
            indexByName.clear();
            for (int i = 0; i < columns.size(); i++) {
                indexByName.put(columns.get(i).trim(), i);
            }
        }

        private String toHeader() {
            return String.join(",", columns);
        }

        private String[] parseRow(String line) {
            List<String> raw = parseCsvLine(line);
            String[] row = new String[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                int rawIndex = outputIndices.get(i);
                row[i] = rawIndex >= 0 && rawIndex < raw.size() ? raw.get(rawIndex) : "";
            }
            return row;
        }

        private String get(String[] row, String name) {
            Integer idx = indexByName.get(name);
            if (idx == null || idx >= row.length) {
                return "";
            }
            return row[idx];
        }

        private void set(String[] row, String name, String value) {
            Integer idx = indexByName.get(name);
            if (idx == null) {
                return;
            }
            row[idx] = value == null ? "" : value;
        }

        private String joinRow(String[] row) {
            if (row.length < columns.size()) {
                String[] padded = new String[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    padded[i] = i < row.length ? row[i] : "";
                }
                return joinQuoted(padded);
            }
            return joinQuoted(row);
        }

        private String joinQuoted(String[] values) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(quote(values[i]));
            }
            return sb.toString();
        }

        private String quote(String value) {
            if (value == null) {
                return "";
            }
            String sanitized = value.replace("\"", "\"\"");
            return "\"" + sanitized + "\"";
        }

        private static List<String> parseCsvLine(String line) {
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
            return cells;
        }
    }
}
