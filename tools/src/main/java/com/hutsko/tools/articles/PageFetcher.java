package com.hutsko.tools.articles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Plain HTTP GET with retries: transient failures (I/O errors, 429, 5xx) are retried with exponential backoff.
 * Permanent HTTP statuses (e.g. 404) are returned to the caller as-is.
 */
public class PageFetcher {
    private static final Logger log = LoggerFactory.getLogger(PageFetcher.class);
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; greatanalyzer-crawler/1.0)";
    private static final int MAX_ATTEMPTS = 4;
    private static final long INITIAL_BACKOFF_MS = 5_000;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static final class Result {
        private final int status;
        private final String body;

        Result(int status, String body) {
            this.status = status;
            this.body = body;
        }

        public int getStatus() {
            return status;
        }

        public String getBody() {
            return body;
        }
    }

    public Result get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xml;q=0.9,*/*;q=0.8")
                .GET()
                .build();
        long backoff = INITIAL_BACKOFF_MS;
        for (int attempt = 1; ; attempt++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int status = response.statusCode();
                boolean transientStatus = status == 429 || status >= 500;
                if (!transientStatus || attempt >= MAX_ATTEMPTS) {
                    return new Result(status, response.body());
                }
                log.warn("HTTP {} for {} (attempt {}/{}), retrying in {} ms", status, url, attempt, MAX_ATTEMPTS, backoff);
            } catch (IOException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
                log.warn("I/O error for {} (attempt {}/{}): {}, retrying in {} ms", url, attempt, MAX_ATTEMPTS, e.getMessage(), backoff);
            }
            Thread.sleep(backoff);
            backoff *= 2;
        }
    }
}
