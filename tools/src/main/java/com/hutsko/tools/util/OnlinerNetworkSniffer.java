package com.hutsko.tools.util;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Playwright-based network sniffer for Onliner pages.
 * Saves network logs and response bodies for debugging and API discovery.
 */
public class OnlinerNetworkSniffer {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: OnlinerNetworkSniffer <url> [@cookieFile]");
            return;
        }
        String url = args[0];
        String cookieHeader = args.length > 1 ? readCookieHeader(args[1]) : null;

        Path outDir = Paths.get("target", "sniff-" + LocalDateTime.now().format(TS));
        Files.createDirectories(outDir);
        Path logFile = outDir.resolve("network.log");

        try (RenderSession session = RenderSession.open(cookieHeader)) {
            session.enableSniffing(logFile, outDir);
            session.fetch(url);
            session.waitFor(5000);
        }

        System.out.println("Saved sniff log: " + logFile.toAbsolutePath());
        System.out.println("Saved bodies under: " + outDir.toAbsolutePath());
    }

    private static void appendLine(Path logFile, String line) {
        try {
            writeString(logFile, line + System.lineSeparator(),
                    Files.exists(logFile) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException ignored) {
        }
    }

    private static String readCookieHeader(String arg) throws IOException {
        if (arg == null) {
            return null;
        }
        if (arg.startsWith("@")) {
            Path cookieFile = Paths.get(arg.substring(1));
            return readString(cookieFile).trim();
        }
        return arg;
    }

    private static String safeFileName(String url) {
        return url.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Reusable Playwright session for rendering HTML pages.
     */
    public static class RenderSession implements AutoCloseable {
        private final Playwright playwright;
        private final Browser browser;
        private final com.microsoft.playwright.BrowserContext context;
        private final Page page;

        private RenderSession(Playwright playwright, Browser browser, com.microsoft.playwright.BrowserContext context, Page page) {
            this.playwright = playwright;
            this.browser = browser;
            this.context = context;
            this.page = page;
        }

        /**
         * Opens a headless browser session with optional Cookie header.
         */
        public static RenderSession open(String cookieHeader) {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Map<String, String> extraHeaders = new HashMap<>();
            extraHeaders.put("Accept-Language", "ru,en;q=0.9");
            extraHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            if (cookieHeader != null && !cookieHeader.trim().isEmpty()) {
                extraHeaders.put("Cookie", cookieHeader);
            }
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setExtraHTTPHeaders(extraHeaders);
            com.microsoft.playwright.BrowserContext context = browser.newContext(contextOptions);
            Page page = context.newPage();
            return new RenderSession(playwright, browser, context, page);
        }

        /**
         * Enables request/response logging and saves JSON/memberlist responses.
         */
        public void enableSniffing(Path logFile, Path outDir) {
            page.onRequest(request -> {
                String line = "REQ " + request.method() + " " + request.resourceType() + " " + request.url();
                appendLine(logFile, line);
            });
            page.onResponse(response -> {
                String ct = response.headers().getOrDefault("content-type", "");
                String line = "RES " + response.status() + " " + ct + " " + response.url();
                appendLine(logFile, line);
                if (ct.contains("application/json") || response.url().contains("memberlist")) {
                    try {
                        String body = response.text();
                        Path out = outDir.resolve(safeFileName(response.url()) + ".txt");
                        writeString(out, body);
                    } catch (Exception ignored) {
                    }
                }
            });
        }

        /**
         * Navigates to a URL and returns the rendered HTML content.
         */
        public String fetch(String url) {
            page.navigate(url);
            return page.content();
        }

        /**
         * Navigates to a URL, waits for the given duration, and returns the rendered HTML content.
         */
        public String fetchRendered(String url, int waitMillis) {
            page.navigate(url);
            if (waitMillis > 0) {
                page.waitForTimeout(waitMillis);
            }
            return page.content();
        }

        /**
         * Waits for a fixed duration to allow async requests to complete.
         */
        public void waitFor(int millis) {
            page.waitForTimeout(millis);
        }

        @Override
        public void close() {
            page.close();
            context.close();
            browser.close();
            playwright.close();
        }
    }

    private static void writeString(Path path, String data, StandardOpenOption... options) throws IOException {
        Files.write(path, data.getBytes(StandardCharsets.UTF_8), options);
    }

    private static String readString(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
