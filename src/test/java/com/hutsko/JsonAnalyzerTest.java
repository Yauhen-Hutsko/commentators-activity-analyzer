package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class JsonAnalyzerTest {
    public static final String JSON_TEST_FILE = "src/test/resource/test.json";
    private JsonAnalyzer analyzer;
    ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(new File(JSON_TEST_FILE));
        analyzer = new JsonAnalyzer(jsonNode);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test(){
        analyzer.handleJson();
    }
}
