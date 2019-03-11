package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutsko.entity.Comment;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonAnalyzerTest {
    private static final String JSON_TEST_FILE = "src/test/resource/test.json";
    private static final String JSON_COMMENT_FILE = "src/test/resource/oneComment.json";
    private static final String COMMENT_TEXT = "\u0418 \u043d\u0430\u043a\u0430\u043a\u043e\u0435 \u0430\u0439\u0442\u044b \u043d\u0435 \u043d\u0443\u0436\u043d\u043e...";
    private JsonAnalyzer analyzer;
    private JsonNode inputJson;
    private JsonNode inputJsonWithReply;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        inputJson = mapper.readTree(new File(JSON_COMMENT_FILE));
        inputJsonWithReply = mapper.readTree(new File(JSON_TEST_FILE));
        analyzer = new JsonAnalyzer();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCommentMappingJsonToObject() throws IOException {
        analyzer.commentMappingJsonToObject(inputJsonWithReply);
        Assert.assertEquals(3, analyzer.getComments().size());

    }

    @Test
    public void testCommentMapper() throws IOException {
        Comment expected = new Comment("f0fb8cb0-378b-11e9-94b6-4b8dce91a276", "novikov433", "1322951",
                10, 1, "2019-02-23T19:56:19.451000+03:00", COMMENT_TEXT);
        List<Comment> actual = analyzer.handleJson(inputJson);

        Assert.assertEquals(expected, actual.get(0));

    }

    @Test
    public void test(){
//        analyzer.getUsers();

    }

}
