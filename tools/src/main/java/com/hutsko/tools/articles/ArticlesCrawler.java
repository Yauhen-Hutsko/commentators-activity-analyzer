package com.hutsko.tools.articles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Collects onliner.by news articles into the {@code articles} table in two phases:
 * <ol>
 *     <li><b>discover</b>: download each section's sitemap.xml and insert article URLs within the year range;</li>
 *     <li><b>enrich</b>: fetch every pending article page and store its metadata.</li>
 * </ol>
 * All state lives in the database, so the crawler can be interrupted and restarted at any point.
 */
public class ArticlesCrawler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ArticlesCrawler.class);
    private static final int FETCH_BATCH = 200;
    private static final int PROGRESS_EVERY = 100;

    public static final List<String> DEFAULT_SECTIONS =
            Collections.unmodifiableList(Arrays.asList("people", "tech", "auto", "realt", "money"));

    /** Crawl options; defaults cover every sitemap-listed article of all news sections at one request per second. */
    public static final class Options {
        /** Inclusive year bounds; null = unbounded. */
        public Integer fromYear;
        public Integer toYear;
        public List<String> sections = DEFAULT_SECTIONS;
        public long pauseMillis = 1_000;
        public int maxAttempts = 3;
        public boolean discover = true;
        public boolean enrich = true;
        /** Stop after this many page fetches (0 = no limit); handy for trial runs. */
        public int limit = 0;
    }

    private final Options options;
    private final ArticleRepository repository;
    private final PageFetcher fetcher;
    private volatile boolean stopRequested;

    public ArticlesCrawler(Options options, ArticleRepository repository, PageFetcher fetcher) {
        this.options = options;
        this.repository = repository;
        this.fetcher = fetcher;
    }

    public void requestStop() {
        stopRequested = true;
    }

    @Override
    public void run() {
        try {
            repository.ensureSchema();
            if (options.discover) {
                discover();
            }
            if (options.enrich) {
                enrich();
            }
            log.info("Done. Article status counts: {}", repository.statusCounts());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Interrupted. Article status counts: {}", repository.statusCounts());
        }
    }

    private void discover() throws InterruptedException {
        for (String section : options.sections) {
            if (shouldStop()) {
                return;
            }
            String url = "https://" + section + ".onliner.by/sitemap.xml";
            try {
                PageFetcher.Result result = fetcher.get(url);
                if (result.getStatus() != 200) {
                    log.warn("Sitemap {} returned HTTP {}, skipping section", url, result.getStatus());
                    continue;
                }
                List<SitemapEntry> entries = SitemapParser.parse(result.getBody(), section, options.fromYear, options.toYear);
                repository.upsertDiscovered(entries);
                log.info("Sitemap {}: {} articles (years {}-{})", section, entries.size(),
                        options.fromYear == null ? "any" : options.fromYear, options.toYear == null ? "any" : options.toYear);
            } catch (java.io.IOException e) {
                log.warn("Failed to download sitemap {}: {}", url, e.getMessage());
            }
            Thread.sleep(options.pauseMillis);
        }
    }

    private void enrich() throws InterruptedException {
        int fetched = 0;
        long startedAt = System.currentTimeMillis();
        while (!shouldStop()) {
            List<String> urls = repository.nextToFetch(FETCH_BATCH, options.maxAttempts, options.fromYear, options.toYear);
            if (urls.isEmpty()) {
                log.info("Nothing left to fetch");
                return;
            }
            for (String url : urls) {
                if (shouldStop() || (options.limit > 0 && fetched >= options.limit)) {
                    if (options.limit > 0 && fetched >= options.limit) {
                        log.info("Reached fetch limit of {}", options.limit);
                    }
                    return;
                }
                fetchOne(url);
                fetched++;
                if (fetched % PROGRESS_EVERY == 0) {
                    double perMinute = fetched * 60_000.0 / Math.max(1, System.currentTimeMillis() - startedAt);
                    log.info("Fetched {} pages ({} / min). Status counts: {}",
                            fetched, String.format("%.0f", perMinute), repository.statusCounts());
                }
                Thread.sleep(options.pauseMillis);
            }
        }
    }

    private void fetchOne(String url) throws InterruptedException {
        try {
            PageFetcher.Result result = fetcher.get(url);
            int status = result.getStatus();
            if (status == 200) {
                repository.saveFetched(url, status, ArticlePageParser.parse(result.getBody(), url));
            } else if (status == 404 || status == 410) {
                repository.markFailed(url, ArticleRepository.STATUS_NOT_FOUND, status, null);
            } else {
                repository.markFailed(url, ArticleRepository.STATUS_ERROR, status, "HTTP " + status);
            }
        } catch (java.io.IOException | RuntimeException e) {
            log.warn("Failed to process {}: {}", url, e.toString());
            repository.markFailed(url, ArticleRepository.STATUS_ERROR, null, e.toString());
        }
    }

    private boolean shouldStop() {
        return stopRequested || Thread.currentThread().isInterrupted();
    }
}
