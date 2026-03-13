package com.hutsko.tools.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple HTML fetcher with browser-like headers to debug server responses.
 */
public class HttpDebugFetcher {
    private static final int TIMEOUT_MS = 15000;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
        System.out.println("Usage: HttpDebugFetcher <url> [cookieHeader|@cookieFile]");
            return;
        }
        String url = args[0];
        String cookieHeader = args.length > 1 ? args[1] : null;
        if (cookieHeader != null && cookieHeader.startsWith("@")) {
            Path cookieFile = Path.of(cookieHeader.substring(1));
            cookieHeader = Files.readString(cookieFile, StandardCharsets.UTF_8).trim();
        }

        Connection.Response response = fetchResponse(url, cookieHeader);
        String html = response.body();
        String ts = LocalDateTime.now().format(TS);
        Path out = Path.of("target", "fetch-" + ts + ".html");
        Files.createDirectories(out.getParent());
        Files.write(out, html.getBytes(StandardCharsets.UTF_8));

        System.out.println("Status: " + response.statusCode());
        System.out.println("Final URL: " + response.url());
        System.out.println("Content-Type: " + response.contentType());
        System.out.println("Set-Cookies: " + response.cookies());
        System.out.println("Cookie header sent: " + (cookieHeader != null && !cookieHeader.trim().isEmpty()));
        System.out.println("Saved HTML to: " + out.toAbsolutePath());
        System.out.println("Length: " + html.length());
        System.out.println(html.substring(0, Math.min(800, html.length())));
    }

    /**
     * Fetches HTML using browser-like headers and optional Cookie header.
     */
    public static Connection.Response fetchResponse(String url, String cookieHeader) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        headers.put("Accept", "text/html,application/xhtml+xml");
        headers.put("Accept-Language", "ru,en;q=0.9");
        headers.put("Referer", "https://forum.onliner.by/");

        Connection connection = Jsoup.connect(url)
                .headers(headers)
                .timeout(TIMEOUT_MS)
                .followRedirects(true);
        if (cookieHeader != null && !cookieHeader.trim().isEmpty()) {
            connection.header("Cookie", cookieHeader);
        }
        return connection.execute();
    }
}
