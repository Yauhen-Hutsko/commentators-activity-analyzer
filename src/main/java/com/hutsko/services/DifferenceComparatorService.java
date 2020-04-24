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
    private JsonNode c; // uses like a buffer for recursive method. Can be convert to local if replace recursion with if-statement

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
        ObjectNode stashed = mapper.createObjectNode();

        c = mapper.createObjectNode(); //bad solution
        for (JsonNode entry : includeSubComments(sourceJson)) {
            sourceCommentsId.add(entry.get("id").toString());
        }
        // check if sourceJSON and cachedJSON has the difference
        c = mapper.createObjectNode();
        for (JsonNode entry : includeSubComments(cachedJson)) {
            if (!sourceCommentsId.contains(entry.get("id").toString())) {
                stashed.set(entry.get("id").asText(), entry);
            }
        }
        rootNode.set("stashed", stashed);
        return rootNode;
    }

    void stashComments(JsonNode deletedComments) {
        // 4. open connection to DB(txt.file) and upload json for rewriting
        // Use name of the article from URL for file naming
        File f = new File(sourceURL.substring(sourceURL.lastIndexOf('/') + 1) + ".json");
        if (!f.exists()) {
            try {
                f.createNewFile();
                mapper.writeValue(f, deletedComments);
            } catch (IOException e) {
                //TODO: add logging
                e.printStackTrace();
            }
        } else {
            JsonNode storage = null;
            try {
                storage = mapper.readTree(f);
            } catch (IOException e) {
                //TODO: add logging
                e.printStackTrace();
            }
//        ObjectNode o = mapper.createObjectNode();
//        o.set("stashed",deletedComments);
            // 5. write cache to DB
            // 6. close connection
            try {
                mapper.writeValue(f, storage);
            } catch (IOException e) {
                //TODO: add logging
                e.printStackTrace();
            }
        }
    }


    /**
     * This method takes upper-lvl comment node, extract comments under 'replies' node and return new jsonNode with a full list of comments.
     *
     * @param node any node that contains json with next fields:<br/>
     *             <code>{
     *             "comments": [
     *             {
     *             "id": "96e3c5e0",
     *             "text": "comment1",
     *             "replies": []
     *             }
     *             ]
     *             }</code>
     * @return all comments that input node comprises.
     */
    private JsonNode includeSubComments(final JsonNode node) {
        JsonNode commentsNode = (node.has("comments")) ? node.get("comments") : node;

        for (JsonNode jsonNode : commentsNode) {
            if (jsonNode.has("replies") && jsonNode.get("replies").size() != 0) {
                //recursive mapping of subcomments
                includeSubComments(jsonNode.get("replies"));
            }
        }
        for (JsonNode jsonNode : commentsNode) {
            ((ObjectNode) c).set(jsonNode.get("id").toString(), jsonNode);
        }
        return c;
    }
}

