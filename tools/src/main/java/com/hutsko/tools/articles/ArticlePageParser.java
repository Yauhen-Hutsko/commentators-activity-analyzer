package com.hutsko.tools.articles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses an onliner.by article page.
 * <p>
 * Primary source is the {@code <script id="page-meta" type="application/json">} block
 * (authors, categories, tags, publication date, type, word count); the rest comes from page markup.
 */
public final class ArticlePageParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ArticlePageParser() {
    }

    public static ArticleDetails parse(String html, String url) {
        Document doc = Jsoup.parse(html, url);
        ArticleDetails details = new ArticleDetails();

        Element container = doc.selectFirst(".news-container[data-post-id]");
        if (container != null) {
            details.postId = parseLong(container.attr("data-post-id"));
            details.special = "1".equals(container.attr("data-special-article"));
        }

        JsonNode meta = readPageMeta(doc);
        if (meta != null) {
            details.pageMetaJson = meta.toString();
            details.publishedAt = parseDateTime(text(meta, "article_publication_date"));
            details.authors = stringList(meta.get("article_authors"));
            details.categories = stringList(meta.get("article_categories"));
            details.tags = stringList(meta.get("article_tags"));
            details.articleType = text(meta, "article_type");
            details.wordCount = meta.hasNonNull("article_word_count") ? meta.get("article_word_count").asInt() : null;
        }

        Element h1 = doc.selectFirst(".news-header__title h1");
        details.title = h1 != null ? h1.text().trim() : metaContent(doc, "og:title");

        if (details.publishedAt == null) {
            details.publishedAt = parseDateTime(metaContent(doc, "article:published_time"));
        }
        if (details.publishedAt == null && container != null) {
            Long epoch = parseLong(container.attr("data-post-date"));
            details.publishedAt = epoch == null ? null : OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.ofHours(3));
        }

        Element views = doc.selectFirst(".news-header__button_views");
        if (views != null) {
            String digits = views.text().replaceAll("[^0-9]", "");
            details.views = digits.isEmpty() ? null : parseInt(digits);
        }

        details.imageUrl = metaContent(doc, "og:image");
        return details;
    }

    private static JsonNode readPageMeta(Document doc) {
        Element script = doc.selectFirst("script#page-meta");
        if (script == null) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(script.data());
            return node != null && node.isObject() ? node : null;
        } catch (IOException e) {
            return null;
        }
    }

    /** Accepts a JSON array of strings or a single comma-separated string. */
    static String[] stringList(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> addIfPresent(values, item.asText()));
        } else {
            for (String part : node.asText().split(",")) {
                addIfPresent(values, part);
            }
        }
        return values.toArray(new String[0]);
    }

    private static void addIfPresent(List<String> values, String value) {
        String trimmed = value == null ? "" : value.trim();
        if (!trimmed.isEmpty()) {
            values.add(trimmed);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static String metaContent(Document doc, String property) {
        Element meta = doc.selectFirst("meta[property=" + property + "]");
        String content = meta == null ? "" : meta.attr("content").trim();
        return content.isEmpty() ? null : content;
    }

    private static OffsetDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Long parseLong(String value) {
        try {
            return value == null || value.isEmpty() ? null : Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null; // overflow on absurd values
        }
    }
}
