package com.hutsko.tools.articles;

import com.hutsko.db.PostgresConnector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistence for the {@code articles} table. Crawl progress is stored in the rows themselves
 * ({@code fetch_status}, {@code fetch_attempts}), so the crawler can be stopped and resumed at any time.
 */
public class ArticleRepository {
    static final String STATUS_PENDING = "pending";
    static final String STATUS_OK = "ok";
    static final String STATUS_NOT_FOUND = "not_found";
    static final String STATUS_ERROR = "error";

    private static final String SCHEMA_RESOURCE = "/sql/articles-schema.sql";
    private static final int INSERT_BATCH_SIZE = 1_000;

    private final PostgresConnector connector;

    public ArticleRepository(PostgresConnector connector) {
        this.connector = connector;
    }

    public void ensureSchema() {
        try (InputStream in = ArticleRepository.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource " + SCHEMA_RESOURCE);
            }
            connector.execute(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + SCHEMA_RESOURCE, e);
        }
    }

    /**
     * Inserts new sitemap entries as pending; for known URLs only refreshes {@code sitemap_lastmod}.
     */
    public void upsertDiscovered(List<SitemapEntry> entries) {
        String sql = "insert into articles (url, section, slug, published_date, sitemap_lastmod)"
                + " values (?, ?, ?, ?, ?)"
                + " on conflict (url) do update set sitemap_lastmod = excluded.sitemap_lastmod";
        List<Object[]> batch = new ArrayList<>(INSERT_BATCH_SIZE);
        for (SitemapEntry e : entries) {
            batch.add(new Object[]{e.getUrl(), e.getSection(), e.getSlug(), e.getPublishedDate(), e.getLastmod()});
            if (batch.size() == INSERT_BATCH_SIZE) {
                connector.batchUpdate(sql, batch);
                batch.clear();
            }
        }
        connector.batchUpdate(sql, batch);
    }

    /**
     * Next URLs to fetch: pending rows plus failed rows that still have attempts left, oldest first.
     */
    public List<String> nextToFetch(int limit, int maxAttempts, Integer fromYear, Integer toYear) {
        String sql = "select url from articles"
                + " where (fetch_status = ? or (fetch_status = ? and fetch_attempts < ?))"
                + "   and (?::int is null or extract(year from published_date) >= ?::int)"
                + "   and (?::int is null or extract(year from published_date) <= ?::int)"
                + " order by published_date, url limit ?";
        return connector.query(sql, rs -> rs.getString(1),
                STATUS_PENDING, STATUS_ERROR, maxAttempts, fromYear, fromYear, toYear, toYear, limit);
    }

    public void saveFetched(String url, int httpStatus, ArticleDetails d) {
        String sql = "update articles set"
                + " post_id = ?, title = ?, published_at = ?, authors = ?, categories = ?, tags = ?,"
                + " article_type = ?, word_count = ?, views = ?, image_url = ?, is_special = ?, page_meta = ?::jsonb,"
                + " fetch_status = ?, http_status = ?, fetch_error = null,"
                + " fetch_attempts = fetch_attempts + 1, fetched_at = now()"
                + " where url = ?";
        connector.update(sql, d.getPostId(), d.getTitle(), d.getPublishedAt(), d.getAuthors(), d.getCategories(),
                d.getTags(), d.getArticleType(), d.getWordCount(), d.getViews(), d.getImageUrl(), d.getSpecial(),
                d.getPageMetaJson(), STATUS_OK, httpStatus, url);
    }

    public void markFailed(String url, String status, Integer httpStatus, String error) {
        String sql = "update articles set fetch_status = ?, http_status = ?, fetch_error = ?,"
                + " fetch_attempts = fetch_attempts + 1, fetched_at = now() where url = ?";
        String truncated = error == null ? null : error.substring(0, Math.min(error.length(), 1_000));
        connector.update(sql, status, httpStatus, truncated, url);
    }

    /** Row count per fetch_status, for progress logging. */
    public Map<String, Long> statusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        connector.query("select fetch_status, count(*) from articles group by 1 order by 1",
                rs -> counts.put(rs.getString(1), rs.getLong(2)));
        return counts;
    }
}
