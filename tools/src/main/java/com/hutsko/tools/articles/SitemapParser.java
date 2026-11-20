package com.hutsko.tools.articles;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts article entries from an onliner.by section sitemap ({@code <urlset>} with {@code <url><loc>/<lastmod>}).
 * Only canonical article URLs ({@code https://<section>.onliner.by/YYYY/MM/DD/slug}) are kept, optionally
 * limited to an inclusive year range (null bound = unbounded). Some real articles use stylised past dates
 * in the URL (e.g. {@code /1863/07/02/...}); they are kept as-is.
 */
public final class SitemapParser {
    private static final Pattern URL_BLOCK = Pattern.compile("<url>(.*?)</url>", Pattern.DOTALL);
    private static final Pattern LOC = Pattern.compile("<loc>\\s*(.*?)\\s*</loc>", Pattern.DOTALL);
    private static final Pattern LASTMOD = Pattern.compile("<lastmod>\\s*(.*?)\\s*</lastmod>", Pattern.DOTALL);
    private static final Pattern ARTICLE_URL =
            Pattern.compile("^https://([a-z]+)\\.onliner\\.by/(\\d{4})/(\\d{2})/(\\d{2})/([^/?#\\s]+)/?$");

    private SitemapParser() {
    }

    public static List<SitemapEntry> parse(String xml, String expectedSection, Integer fromYear, Integer toYear) {
        Map<String, SitemapEntry> byUrl = new LinkedHashMap<>();
        Matcher block = URL_BLOCK.matcher(xml);
        while (block.find()) {
            Matcher loc = LOC.matcher(block.group(1));
            if (!loc.find()) {
                continue;
            }
            SitemapEntry entry = toEntry(loc.group(1), lastmodOf(block.group(1)), expectedSection, fromYear, toYear);
            if (entry != null) {
                byUrl.putIfAbsent(entry.getUrl(), entry);
            }
        }
        return new ArrayList<>(byUrl.values());
    }

    static SitemapEntry toEntry(String rawUrl, OffsetDateTime lastmod, String expectedSection, Integer fromYear, Integer toYear) {
        String url = rawUrl.endsWith("/") ? rawUrl.substring(0, rawUrl.length() - 1) : rawUrl;
        Matcher m = ARTICLE_URL.matcher(url);
        if (!m.matches() || !m.group(1).equals(expectedSection)) {
            return null;
        }
        int year = Integer.parseInt(m.group(2));
        if ((fromYear != null && year < fromYear) || (toYear != null && year > toYear)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.of(year, Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
            return new SitemapEntry(url, m.group(1), m.group(5), date, lastmod);
        } catch (DateTimeException e) {
            return null; // e.g. /2019/02/30/ - not a real article date
        }
    }

    private static OffsetDateTime lastmodOf(String urlBlock) {
        Matcher m = LASTMOD.matcher(urlBlock);
        if (!m.find()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(m.group(1));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
