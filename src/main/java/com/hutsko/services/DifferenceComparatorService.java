package com.hutsko.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hutsko.ArticleLoader;
import com.hutsko.ArticleLoaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Service description:
 * 1. At initialization its fetch comments from an article and cache them.
 * 2. After that on schedule, comments fetched again and comparing with cached ones.
 * If cached comment wasn't found in source then it saved in a list.
 * Once, all source comments processed, the list stored in DB(currently it's a file on hard drive).
 * 3. Source json becomes new cache.
 * 4. Step 2 is repeated.
 *
 * @version 0.9
 * note: TODO:#Mustithread - every article has its own thread
 */
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
        cachedJson = article.getCommentsRootNode().get("comments");
    }

    // separate diff
    void process() {
        sourceJson = article.getCommentsRootNode().get("comments");
        ObjectNode difference = separateDiffComments(sourceJson, cachedJson);
        if (difference.size() > 0) {
            stashComments(difference);
        }
    }

    ObjectNode separateDiffComments(JsonNode sourceJson, JsonNode cachedJson) {
        if (!cachedJson.elements().hasNext()) {
            throw new RuntimeException("cached json has incorrect inner structure");
        }
        ArrayList<String> sourceCommentsId = new ArrayList<>();
        ObjectNode rootNode = mapper.createObjectNode();

        for (JsonNode entry : sourceJson) {
            sourceCommentsId.add(entry.get("id").toString());
        }
        // 5.1 check if sourceJSON and cachedJSON has the difference
        for (JsonNode entry : cachedJson) {
            if (!sourceCommentsId.contains(entry.get("id").toString())) {
                rootNode.set(entry.get("id").toString(), entry);
            }
        }
        return rootNode;
    }

    void stashComments(ObjectNode deletedComments) {
        // 4. open connection to DB(txt.file) and upload json for rewriting
        // Use name of the article from URL for file naming
        File f = new File(sourceURL.substring(sourceURL.lastIndexOf('/') + 1));
        if (!f.exists()) {
            try {
                f.createNewFile();
                mapper.writeValue(f, deletedComments);
            } catch (IOException e) {
                //TODO: add logging
                e.printStackTrace();
            }
        }
        ObjectNode storage = null;
        try {
            storage = (ObjectNode) mapper.readTree(f);
        } catch (IOException e) {
            //TODO: add logging
            e.printStackTrace();
        }
        storage.setAll(deletedComments);
        // 5. write cache to DB
        // 6. close connection
        try {
            mapper.writeValue(f, storage);
        } catch (IOException e) {
            //TODO: add logging
            e.printStackTrace();
        }
    }


    public void fillComments(final JsonNode node, ObjectNode collection) {
        JsonNode commentsNode = extractNodeByKey(node, COMMENTS_NODE_NAME);

        for (JsonNode jsonNode : commentsNode) {
            if (jsonNode.has(SUBCOMMENTS_NODE_NAME) && jsonNode.get(SUBCOMMENTS_NODE_NAME).size() != 0) {
                //recursive mapping of subcomments
                fillComments(jsonNode.get(SUBCOMMENTS_NODE_NAME));
            }
        }
        ObjectNode c = mapper.createObjectNode();
        collection.setAll(c);
    }

}

