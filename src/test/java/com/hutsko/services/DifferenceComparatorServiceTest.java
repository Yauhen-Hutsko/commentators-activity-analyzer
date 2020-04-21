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

@RunWith(MockitoJUnitRunner.class)
public class DifferenceComparatorServiceTest {

    private JsonNode sourceJson;
    private JsonNode cachedJson;
    private ObjectMapper mapper = new ObjectMapper();
    //used any valid url just to initialize class.
    private DifferenceComparatorService service = new DifferenceComparatorService("https://tech.onliner.by/2019/02/20/muzykant-v-it");

    @Before
    public void setUp() throws Exception {
        sourceJson = mapper.readTree(new File("src/test/resource/source.json")).get("comments");
        cachedJson = mapper.readTree(new File("src/test/resource/cached.json")).get("comments");
        Whitebox.setInternalState(service, "cachedJson", cachedJson);
        Whitebox.setInternalState(service, "sourceJson", sourceJson);

    }

    @Test
    public void separateDiffCommentsTest() {
//        when(service.separateDiffComments(sourceJson, cachedJson)).then()
        ObjectNode root = service.separateDiffComments(sourceJson, cachedJson);
        Assert.assertEquals(root.size(), 2);
    }
}