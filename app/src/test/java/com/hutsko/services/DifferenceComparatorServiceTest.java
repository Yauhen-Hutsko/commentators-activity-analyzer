package com.hutsko.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DifferenceComparatorServiceTest {

    private Path tomcatBase;
    private File pathToFile;
    private JsonNode freshJson;
    private JsonNode cachedJson;
    private JsonNode oneComment;
    private JsonNode forStash;
    private JsonNode stashAppend;
    private String stashed;
    private ObjectMapper mapper = new ObjectMapper();
    private DifferenceComparatorService service;

    @Before
    public void setUp() throws Exception {
        tomcatBase = Files.createTempDirectory("tomcat-base");
        Files.createDirectories(tomcatBase.resolve("GADB"));
        System.setProperty("catalina.base", tomcatBase.toString());
        pathToFile = tomcatBase.resolve("GADB").resolve("muzykant-v-it.json").toFile();

        cachedJson = mapper.readTree(new File("src/test/resources/cached.json")).get("comments");
        freshJson = mapper.readTree(new File("src/test/resources/source.json")).get("comments");
        oneComment = mapper.readTree(new File("src/test/resources/oneComment.json")).get("comments");
        forStash = mapper.readTree(new File("src/test/resources/forStash.json"));
        stashAppend = mapper.readTree(new File("src/test/resources/stashAppend.json"));
        stashed = "src/test/resources/muzykant-v-it.json";
        service = new DifferenceComparatorService("https://tech.onliner.by/2019/02/20/muzykant-v-it", mapper.readTree(new File("src/test/resources/source.json")));
    }

    @After
    public void tearDown() throws Exception {
        if (pathToFile != null) {
            pathToFile.delete();
        }
        System.clearProperty("catalina.base");
    }

    @Test
    public void count_all_entries() { // Test that lambda-exp is counting correctly.
        int onlyCommentNodes = (int) cachedJson.findValues("id").stream().filter(JsonNode::isTextual).count();
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
    public void stashComments_should_create_file() throws Exception {
        File f = pathToFile;
        service.stashComments(forStash);

        Assert.assertTrue(f.exists());
        Assert.assertTrue(f.delete());
    }

    @Test
    public void stashComments_should_write_comments_to_empty_file() throws Exception {
        File f = pathToFile;
        Assert.assertFalse(f.exists());
        f.createNewFile();
        service.stashComments(forStash);
        Assert.assertEquals(3, mapper.readTree(f).get("stashed").size());
    }

    @Test
    public void stashComments_should_append_comments_to_file() throws Exception {
        File f = pathToFile;
        Assert.assertFalse("Delete an old file and repeat test", f.exists());
        f.createNewFile();
        mapper.writeValue(f, mapper.readTree(new File(stashed))); //populate temporary file with 3 entries.

        service.stashComments(stashAppend);
        Assert.assertEquals(5, mapper.readTree(f).get("stashed").size());
        Assert.assertTrue(mapper.readTree(f).get("stashed").get("99999999").has("id")); //entity from appender
        Assert.assertTrue(mapper.readTree(f).get("stashed").get("bb936090").has("id")); //entity from stash
    }

}
