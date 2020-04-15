package com.hutsko.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hutsko.ArticleLoader;
import com.hutsko.ArticleLoaderFactory;

import java.io.File;

public class DifferenceComparatorService {

    private String sourceURL;
    private ArticleLoader article;
    private JsonNode sourceJson;
    private JsonNode cachedJson;
    private ObjectMapper mapper;

    public DifferenceComparatorService(String url) {
        sourceURL = url;
        mapper = new ObjectMapper();
        article = ArticleLoaderFactory.getInstance(url);
        sourceJson = article.getCommentsNode();
    }

    //it should be the method that invoked on schedule
    void process() {
        // 1. getting source-json on schedule
        sourceJson = article.getCommentsNode();
        // 2. create cache-json
        // separate diff
        cachedJson = (cachedJson == null) ? sourceJson : separateDiffComments();
        ObjectNode storage = mapper.createObjectNode();

        // 3. open connection to DB(txt.file) and upload json for rewriting
        // 3.1 check if sourceJSON and cachedJSON has the difference
        File db = new File(sourceURL.substring(sourceURL.lastIndexOf('/')+1));

        // 4. write cache to DB
        // 5. close connection
    }

    private JsonNode separateDiffComments() {
        ArrayNode storage = mapper.createArrayNode();

        return null;
    }
}

