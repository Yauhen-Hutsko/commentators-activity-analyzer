package com.hutsko.tools.articles;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * One article URL from a section sitemap.
 */
public final class SitemapEntry {
    private final String url;
    private final String section;
    private final String slug;
    private final LocalDate publishedDate;
    private final OffsetDateTime lastmod;

    public SitemapEntry(String url, String section, String slug, LocalDate publishedDate, OffsetDateTime lastmod) {
        this.url = url;
        this.section = section;
        this.slug = slug;
        this.publishedDate = publishedDate;
        this.lastmod = lastmod;
    }

    public String getUrl() {
        return url;
    }

    public String getSection() {
        return section;
    }

    public String getSlug() {
        return slug;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    /** May be null when the sitemap has no or an unparsable lastmod. */
    public OffsetDateTime getLastmod() {
        return lastmod;
    }
}
