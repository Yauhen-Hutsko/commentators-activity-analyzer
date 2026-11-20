package com.hutsko.tools.articles;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class SitemapParserTest {

    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n"
            + " <url><loc>https://people.onliner.by/2020/08/10/gruppa-turistov</loc>"
            + "<lastmod>2026-11-19T19:34:42+03:00</lastmod></url>\n"
            + " <url><loc>https://people.onliner.by/2020/08/10/gruppa-turistov/</loc></url>\n"     // duplicate with slash
            + " <url><loc>https://people.onliner.by/2017/12/31/too-old</loc></url>\n"              // out of range
            + " <url><loc>https://people.onliner.by/2023/01/01/too-new</loc></url>\n"              // out of range
            + " <url><loc>https://tech.onliner.by/2020/08/10/other-section</loc></url>\n"         // wrong section
            + " <url><loc>https://people.onliner.by/2019/02/30/impossible-date</loc></url>\n"      // invalid date
            + " <url><loc>https://people.onliner.by/accidents</loc></url>\n"                       // not an article
            + " <url><loc>https://people.onliner.by/2018/01/01/new-year</loc><lastmod>garbage</lastmod></url>\n"
            + "</urlset>";

    @Test
    public void parse_keeps_only_articles_of_section_within_year_range() {
        List<SitemapEntry> entries = SitemapParser.parse(XML, "people", 2018, 2022);

        Assert.assertEquals(2, entries.size());

        SitemapEntry first = entries.get(0);
        Assert.assertEquals("https://people.onliner.by/2020/08/10/gruppa-turistov", first.getUrl());
        Assert.assertEquals("people", first.getSection());
        Assert.assertEquals("gruppa-turistov", first.getSlug());
        Assert.assertEquals(LocalDate.of(2020, 8, 10), first.getPublishedDate());
        Assert.assertEquals(OffsetDateTime.parse("2026-11-19T19:34:42+03:00"), first.getLastmod());

        SitemapEntry second = entries.get(1);
        Assert.assertEquals("new-year", second.getSlug());
        Assert.assertNull("unparsable lastmod becomes null", second.getLastmod());
    }

    @Test
    public void parse_without_year_bounds_keeps_all_valid_articles_including_stylised_old_dates() {
        String xml = "<urlset>"
                + "<url><loc>https://tech.onliner.by/1863/07/02/dozatory</loc></url>"
                + "<url><loc>https://tech.onliner.by/2026/11/19/fresh</loc></url>"
                + "<url><loc>https://tech.onliner.by/2019/02/30/impossible-date</loc></url>"
                + "</urlset>";

        List<SitemapEntry> entries = SitemapParser.parse(xml, "tech", null, null);

        Assert.assertEquals(2, entries.size());
        Assert.assertEquals(LocalDate.of(1863, 7, 2), entries.get(0).getPublishedDate());
        Assert.assertEquals("fresh", entries.get(1).getSlug());
    }
}
