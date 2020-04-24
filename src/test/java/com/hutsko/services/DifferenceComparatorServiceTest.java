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

    private JsonNode sourceJson;
    private JsonNode cachedJson;
    private JsonNode oneComment;
    private JsonNode forStash;
    private JsonNode stashAppend;
    private ObjectMapper mapper = new ObjectMapper();
    //use any valid url just to initialize class.
    private DifferenceComparatorService service = new DifferenceComparatorService("https://tech.onliner.by/2019/02/20/muzykant-v-it");

    @Before
    public void setUp() throws Exception {
        sourceJson = mapper.readTree(new File("src/test/resource/source.json")).get("comments");
        cachedJson = mapper.readTree(new File("src/test/resource/cached.json")).get("comments");
        oneComment = mapper.readTree(new File("src/test/resource/oneComment.json")).get("comments");
        forStash = mapper.readTree(new File("src/test/resource/forStash.json"));
        stashAppend = mapper.readTree(new File("src/test/resource/stashAppend.json"));
        Whitebox.setInternalState(service, "cachedJson", cachedJson);
        Whitebox.setInternalState(service, "sourceJson", sourceJson);

    }

    @After
    public void tearDown() throws Exception {
        File f = new File("muzykant-v-it.json");
        f.delete();
    }

    @Test
    public void separateDiffComments_should_find_three_comments() {
        ObjectNode root = service.separateDiffComments(sourceJson, cachedJson);
        Assert.assertEquals(3, root.get("stashed").size());
    }

    @Test
    public void separateDiffComments_should_not_find_any_comment() throws IOException {
        ObjectNode root = service.separateDiffComments(sourceJson, sourceJson);
        Assert.assertEquals(0, root.get("stashed").size());
    }

    @Test
    public void separateDiffComments_should_add_every_comment() throws IOException {
        ObjectNode root = service.separateDiffComments(oneComment, sourceJson);
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
        File f = new File("muzykant-v-it2.json");
        service.stashComments(stashAppend);
        Assert.assertEquals(4, mapper.readTree(f).get("stashed").size());
        Assert.assertTrue(mapper.readTree(f).get("stashed").get("99999999").has("id"));
    }

}