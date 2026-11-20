package com.hutsko.tools.articles;

import org.junit.Assert;
import org.junit.Test;

import java.time.OffsetDateTime;

public class ArticlePageParserTest {

    private static final String URL = "https://people.onliner.by/2020/08/10/sample";

    private static final String PAGE = "<html><head>"
            + "<meta property=\"og:title\" content=\"Sample title - Люди Onlíner\">"
            + "<meta property=\"og:image\" content=\"https://content.onliner.by/news/default/abc.jpeg\">"
            + "<meta property=\"article:published_time\" content=\"2020-08-10T10:30:01+03:00\">"
            + "</head><body>"
            + "<div class=\"news-posts\"><div class=\"news-container\" data-post-id=\"775346\""
            + " data-post-date=\"1597044601\" data-special-article=\"0\">"
            + "<script id=\"page-meta\" type=\"application/json\">{\"page_type\":\"article\","
            + "\"article_authors\":\"Андрей Рудь, Ольга Иванова\",\"article_categories\":[\"Происшествия\"],"
            + "\"article_publication_date\":\"2020-08-10T10:30:01+03:00\",\"article_tags\":[\"Туризм\",\"Франция\"],"
            + "\"article_type\":\"news\",\"article_word_count\":90}</script>"
            + "<div class=\"news-header__title\"><h1> Sample title </h1></div>"
            + "<div class=\"news-header__button news-header__button_views\"> 32&nbsp;617 </div>"
            + "</div></div></body></html>";

    @Test
    public void parse_extracts_page_meta_and_markup_fields() {
        ArticleDetails d = ArticlePageParser.parse(PAGE, URL);

        Assert.assertEquals(Long.valueOf(775346), d.getPostId());
        Assert.assertEquals("Sample title", d.getTitle());
        Assert.assertEquals(OffsetDateTime.parse("2020-08-10T10:30:01+03:00"), d.getPublishedAt());
        Assert.assertArrayEquals(new String[]{"Андрей Рудь", "Ольга Иванова"}, d.getAuthors());
        Assert.assertArrayEquals(new String[]{"Происшествия"}, d.getCategories());
        Assert.assertArrayEquals(new String[]{"Туризм", "Франция"}, d.getTags());
        Assert.assertEquals("news", d.getArticleType());
        Assert.assertEquals(Integer.valueOf(90), d.getWordCount());
        Assert.assertEquals(Integer.valueOf(32617), d.getViews());
        Assert.assertEquals("https://content.onliner.by/news/default/abc.jpeg", d.getImageUrl());
        Assert.assertEquals(Boolean.FALSE, d.getSpecial());
        Assert.assertTrue(d.getPageMetaJson().contains("\"article_word_count\":90"));
    }

    @Test
    public void parse_falls_back_to_meta_tags_when_page_meta_missing() {
        String page = "<html><head>"
                + "<meta property=\"og:title\" content=\"Fallback title\">"
                + "<meta property=\"article:published_time\" content=\"2019-05-11T08:00:00+03:00\">"
                + "</head><body><p>special layout</p></body></html>";

        ArticleDetails d = ArticlePageParser.parse(page, URL);

        Assert.assertNull(d.getPostId());
        Assert.assertEquals("Fallback title", d.getTitle());
        Assert.assertEquals(OffsetDateTime.parse("2019-05-11T08:00:00+03:00"), d.getPublishedAt());
        Assert.assertNull(d.getTags());
        Assert.assertNull(d.getViews());
        Assert.assertNull(d.getPageMetaJson());
    }
}
