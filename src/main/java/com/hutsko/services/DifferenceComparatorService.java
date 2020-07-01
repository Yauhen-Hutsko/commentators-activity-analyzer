package com.hutsko.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hutsko.ArticleLoader;
import com.hutsko.ArticleLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

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
    private static Logger log = LoggerFactory.getLogger(DifferenceComparatorService.class);

    private static final String TOMCAT_STORAGE_FOLDER = "\\GADB\\"; //relative path under Tomcat folder
    private String sourceURL;
    private int totalComments;
    private int views;
    private int deletedComments;
    private JsonNode freshJson;
    private JsonNode cachedJson;
    private ArticleLoader article;
    private ObjectMapper mapper;
    private JsonNode c; // uses like a buffer for recursive method. May be converted to local if replace recursion with if-statement
    private ObjectNode rootNode;

    public DifferenceComparatorService(String url) {
        sourceURL = url;
        mapper = new ObjectMapper();
        rootNode = mapper.createObjectNode();
        article = ArticleLoaderFactory.getInstance(sourceURL);
        JsonNode fullJson = article.getFullResponseJsonNode();
        cachedJson = fullJson.get("comments");
        rootNode.put("startTrackAtQuantityOfComments", fullJson.get("total").asInt());
    }

    void process() {
        log.debug("DifferenceComparatorService.process():starts");
        article = ArticleLoaderFactory.getInstance(sourceURL);
        totalComments = article.getFullResponseJsonNode().get("total").asInt();
        views = Integer.parseInt(article.getViews().replaceAll("[^\\d]", "")); //parsed from raw html data
        freshJson = article.getFullResponseJsonNode().get("comments");
        ObjectNode difference = separateDiffComments(freshJson, cachedJson);
        if (difference.get("stashed").size() > 0) {
            log.info("Found " + difference.get("stashed").size() + " deleted comments");
            stashComments(difference);
        } else {
            log.info("Deleted comments not found");
        }
        cachedJson = freshJson;
        log.info("Cache updated. New size is: " + cachedJson.size());
        log.debug("process():ends");
    }

    ObjectNode separateDiffComments(JsonNode freshJson, JsonNode cachedJson) {
        log.debug("separateDiffComments():starts");

        if (!cachedJson.elements().hasNext()) {
            log.error("'cachedJson' has not valid structure: " + cachedJson);
            throw new RuntimeException("cached json has incorrect inner structure");
        }
        ArrayList<String> sourceCommentsId = new ArrayList<>();
        ObjectNode stashed = mapper.createObjectNode();

        c = mapper.createObjectNode(); //bad solution
        for (JsonNode entry : includeSubComments(freshJson)) {
            sourceCommentsId.add(entry.get("id").toString());
        }
        log.info(sourceCommentsId.size() + " comments read from source.");

        // check if sourceJSON and cachedJSON has the difference
        c = mapper.createObjectNode(); //bad solution
        for (JsonNode entry : includeSubComments(cachedJson)) {
            if (!sourceCommentsId.contains(entry.get("id").toString())) {
                stashed.set(entry.get("id").asText(), entry);
            }
        }
        rootNode.set("stashed", stashed);
        log.debug("separateDiffComments():ends");
        return rootNode;
    }

    void stashComments(JsonNode deletedCommentsNode) {
        log.debug("stashComments():starts");
        // 4. open connection to DB(txt.file) and upload json for rewriting
        // Use name of the article from URL for file naming
        File f = new File(buildPath());
        if (!f.exists()) { //first encounter
            try {
                f.createNewFile();
                log.info("File " + f.getName() + " was created.");
            } catch (IOException e) {
                log.error("Can't create a file " + f.getName() + " under the path " + f.getPath(), e);
            }
        }
        JsonNode forStashNode = deletedCommentsNode.get("stashed");
        try {
            if (f.length() != 0) { //main scenario
                JsonNode storage = mapper.readTree(f).get("stashed");
                log.debug("Loaded " + storage.size() + " from storage.");
                Iterator<Map.Entry<String, JsonNode>> iterator = forStashNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry i = iterator.next();
                    ((ObjectNode) storage).set(i.getKey().toString(), (JsonNode) i.getValue());
                }
                deletedComments += forStashNode.size();

                ObjectNode rootNode = mapper.createObjectNode();
                rootNode.set("stashed", storage);
                addMetaData();
                // 5. save cache to DB
                mapper.writeValue(f, rootNode);
                log.info("Deleted comments were appended to file " + f.getName() + ". New quantity is: " + storage.size());
            } else {
                mapper.writeValue(f, deletedCommentsNode);
                log.info("Deleted comments was written to empty file");
            }
        } catch (IOException e) {
            log.error("An error has occurred during json reading from or writing to file ", e);
        }
        // 6. close connection if needed
        log.debug("stashComments():ends");
    }


    int getTotalComments() {
        return totalComments;
    }

    int getViews() {
        return views;
    }

    float getInvolvedPercent() {
        if (views == 0) {
            return 0;
        }
        float involved = (float) (totalComments * 100) / views;
        String s = String.format("%.2f %n", involved);
        return Float.valueOf(s);
    }

    float getTrueInvolvedPercent() {
        if (views == 0) {
            return 0;
        }
        float involved = (float) ((totalComments + deletedComments) * 100) / views;
        String s = String.format("%.2f %n", involved);
        return Float.valueOf(s);
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

    private String buildPath() {
        String base = System.getProperty("catalina.base");
        return base + TOMCAT_STORAGE_FOLDER + sourceURL.substring(sourceURL.lastIndexOf('/') + 1) + ".json";
    }

    private void addMetaData() {
        rootNode.put("url", sourceURL);
        rootNode.put("involvement", getTrueInvolvedPercent());
        rootNode.put("deletedTotal", deletedComments);
    }
}