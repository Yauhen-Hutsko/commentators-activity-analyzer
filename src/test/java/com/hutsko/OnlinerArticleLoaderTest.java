package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class OnlinerArticleLoaderTest {

    private static final String FILE_PATH = "https://tech.onliner.by/2019/02/20/muzykant-v-it";
    private static final String REQUEST_FOR_LAST_COMMENT = "https://comments.api.onliner.by/news/tech.post/414510/comments?limit=1&v=0.33252155200390354";
    private static final String TEST_DATA = "\n" +
            "<div class=\"news-grid__part news-grid__part_1\">\n" +
            "    <div id=\"fast-comments-root\">\n" +
            "        <div class=\"fast-comments\" id=\"fast-comments\">\n" +
            "            <app\n" +
            "                    project-name=\"news\"\n" +
            "                    entity-type=\"tech.post\"\n" +
            "                    entity-id=\"414510\">\n" +
            "            </app>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <script src=\"https://tech.onliner.by/assets/fast-comments.3967d3c2b80889d3.js\"></script>\n" +
            "</div>";
    private OnlinerArticleLoader parser;
    private RequestUrlBuilder builder;

    @Before
    public void setUp() throws Exception {
        parser = new OnlinerArticleLoader(FILE_PATH);
        builder = new RequestUrlBuilder();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetPage() throws IOException {
        Document document = parser.readHtmlPageSourceCode();
        String title = document.title();
        System.out.println(title);

        Assert.assertNotNull(title);
        Assert.assertTrue(title.length() > 1);
    }

    @Test
    public void testGetCommentsUrl() {
        Document node = Jsoup.parse(TEST_DATA);
        Node appNode = node.getElementsByTag("app").first();
        String actual = RequestUrlBuilder.buildUrl(appNode, 1);
        System.out.println(actual);
        Assert.assertEquals(REQUEST_FOR_LAST_COMMENT, actual);
    }

    @Test
    public void testUploadComments() throws IOException {
        JsonNode node = parser.uploadCommentsFromURL("https://comments.api.onliner.by/news/tech.post/414510/comments?limit=99999");
        Assert.assertEquals(node.size(), 6);
    }
}
