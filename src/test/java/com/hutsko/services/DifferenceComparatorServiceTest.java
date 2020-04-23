package com.hutsko.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private ObjectMapper mapper = new ObjectMapper();
    //used any valid url just to initialize class.
    private DifferenceComparatorService service = new DifferenceComparatorService("https://tech.onliner.by/2019/02/20/muzykant-v-it");

    @Before
    public void setUp() throws Exception {
        sourceJson = mapper.readTree(new File("src/test/resource/source.json")).get("comments");
        cachedJson = mapper.readTree(new File("src/test/resource/cached.json")).get("comments");
        oneComment = mapper.readTree(new File("src/test/resource/oneComment.json")).get("comments");
        Whitebox.setInternalState(service, "cachedJson", cachedJson);
        Whitebox.setInternalState(service, "sourceJson", sourceJson);

    }

    @Test
    public void separateDiffComments_should_find_three_comments() {
        ObjectNode root = service.separateDiffComments(sourceJson, cachedJson);
        Assert.assertEquals(3, root.size());
    }

    @Test
    public void separateDiffComments_should_not_find_any_comment() throws IOException {
        ObjectNode root = service.separateDiffComments(sourceJson, sourceJson);
        Assert.assertEquals(0, root.size());
    }

    @Test
    public void separateDiffComments_should_add_every_comment() throws IOException {
        ObjectNode root2 = service.separateDiffComments(oneComment, sourceJson);
        Assert.assertEquals(12, root2.size());

    }

    @Test
    public void stashComments_should_ (){}
}