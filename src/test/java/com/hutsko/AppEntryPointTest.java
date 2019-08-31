package com.hutsko;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;


public class AppEntryPointTest {

    private static final String TEST_DATA_PATH = "src/test/resource/testsample.html";
    public static final String GET_ALL_COMMENTS_REQUEST_PATH = "https://comments.api.onliner.by/news/tech.post/414510/comments?last_comment_id=0324e1d0-35f1-11e9-a247-b93a98d4ed90&limit=99999&v=0.06049730447098667";

    private AppEntryPoint appEntryPoint;
    private Elements allCommentElements;

    @Before
    public void setUp() throws Exception {
        Document doc = Jsoup.parse(new File(TEST_DATA_PATH), "UTF-8");
        appEntryPoint = new AppEntryPoint();
        allCommentElements = doc.getElementsByAttributeValueStarting("id", "comment-");
        System.out.println("Elements was read: " + allCommentElements.size());
    }

    @After
    public void tearDown() throws Exception {
    }

//    @Test
    public void testUniqueName() {
        String name = AppEntryPoint.uniqueName(allCommentElements.get(0));
        System.out.println(name);
        Assert.assertNotNull(name);
        Assert.assertNotEquals(name, "");

    }
}
