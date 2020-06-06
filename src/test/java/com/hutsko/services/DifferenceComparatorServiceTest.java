package com.hutsko.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class DifferenceComparatorServiceTest {

    private JsonNode freshJson;
    private JsonNode cachedJson;
    private JsonNode oneComment;
    private JsonNode forStash;
    private JsonNode stashAppend;
    private String stashed;
    private ObjectMapper mapper = new ObjectMapper();
    //use any valid url just to initialize class.
    private DifferenceComparatorService service = new DifferenceComparatorService("https://tech.onliner.by/2019/02/20/muzykant-v-it");

    @Before
    public void setUp() throws Exception {
        freshJson = mapper.readTree(new File("src/test/resources/source.json")).get("comments");
        cachedJson = mapper.readTree(new File("src/test/resources/cached.json")).get("comments");
        oneComment = mapper.readTree(new File("src/test/resources/oneComment.json")).get("comments");
        forStash = mapper.readTree(new File("src/test/resources/forStash.json"));
        stashAppend = mapper.readTree(new File("src/test/resources/stashAppend.json"));
        stashed = "src/test/resources/muzykant-v-it.json";
        Whitebox.setInternalState(service, "cachedJson", cachedJson);
        Whitebox.setInternalState(service, "freshJson", freshJson);

    }

    @After
    public void tearDown() throws Exception {
        File f = new File("muzykant-v-it.json");
        f.delete();
    }

    @Test
    public void count_all_entries() { // Test that lambda-exp is counting correctly.
        int onlyCommentNodes = (int)cachedJson.findValues("id").stream().filter(JsonNode::isTextual).count();
        Assert.assertEquals(29, cachedJson.findValues("id").size());
        Assert.assertEquals(13, onlyCommentNodes);
    }

    @Test
    public void separateDiffComments_should_find_three_comments() {
        ObjectNode root = service.separateDiffComments(freshJson, cachedJson);
        Assert.assertEquals(3, root.get("stashed").size());
    }

    @Test
    public void separateDiffComments_should_not_find_any_comment() throws IOException {
        ObjectNode root = service.separateDiffComments(freshJson, freshJson);
        Assert.assertEquals(0, root.get("stashed").size());
    }

    @Test
    public void separateDiffComments_should_add_every_comment() throws IOException {
        ObjectNode root = service.separateDiffComments(oneComment, freshJson);
        Assert.assertEquals(12, root.get("stashed").size());

    }

    @Test
    public void stashComments_should_create_file() {
        File f = new File("muzykant-v-it.json");

        service.stashComments(forStash);
        Assert.assertTrue(f.exists());
        Assert.assertTrue(f.delete());
    }

    @Test
    public void stashComments_should_write_comments_to_empty_file() throws IOException {
        File f = new File("muzykant-v-it.json");
        Assert.assertFalse(f.exists());
        f.createNewFile();
        service.stashComments(forStash);
        Assert.assertEquals(3, mapper.readTree(f).get("stashed").size());
    }

    @Test
    public void stashComments_should_append_comments_to_file() throws IOException {
        File f = new File("muzykant-v-it.json");
        Assert.assertFalse("Delete an old file and repeat test", f.exists());
        f.createNewFile();
        mapper.writeValue(f, mapper.readTree(new File(stashed))); //populate temporary file with 3 entries.

        service.stashComments(stashAppend);
        Assert.assertEquals(5, mapper.readTree(f).get("stashed").size());
        Assert.assertTrue(mapper.readTree(f).get("stashed").get("99999999").has("id")); //entity from appender
        Assert.assertTrue(mapper.readTree(f).get("stashed").get("bb936090").has("id")); //entity from stash
    }

}