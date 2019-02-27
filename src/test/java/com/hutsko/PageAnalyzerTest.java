package com.hutsko;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;


public class PageAnalyzerTest {

    private String TEST_DATA_PATH = "src/test/resource/testsample.html";

    private PageAnalyzer pageAnalyzer;
    private Elements allCommentElements;

    @Before
    public void setUp() throws Exception {
        Document doc = Jsoup.parse(new File(TEST_DATA_PATH), "UTF-8");
        pageAnalyzer = new PageAnalyzer();
        allCommentElements = doc.getElementsByAttributeValueStarting("id", "comment-");
        System.out.println("Elements was read: " + allCommentElements.size());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUniqueName() {
        String name = PageAnalyzer.uniqueName(allCommentElements.get(0));
        System.out.println(name);
        Assert.assertNotNull(name);
        Assert.assertNotEquals(name, "");

    }
}
