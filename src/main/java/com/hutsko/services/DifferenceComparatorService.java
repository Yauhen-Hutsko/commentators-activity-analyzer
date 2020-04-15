package com.hutsko.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.hutsko.ArticleLoader;
import com.hutsko.ArticleLoaderFactory;

public class DifferenceComparatorService {

    private String url;
    private ArticleLoader article;
    private JsonNode sourceJson;
    private JsonNode cachedJson;

    // 1. getting source-json on schedule
    void init(){
        article = ArticleLoaderFactory.getInstance(url);
    }
    // 2. create cache-json
    // 3. open connection to DB(txt.file)
    // 4. write cache to DB
    // 5. close connection
}
