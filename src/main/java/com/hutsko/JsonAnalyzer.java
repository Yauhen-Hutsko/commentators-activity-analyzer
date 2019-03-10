package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.io.IOException;
import java.util.*;

/**
 * This class uses for processing the response from Onliner.by which contains all information
 * about comments
 */
public class JsonAnalyzer {

    public static final String COMMENTS_NODE_NAME = "comments";
    public static final String SUBCOMMENT_NODE_NAME = "replies";
    private Map<String, List<User>> commentators = new HashMap<>();
    List<Comment> comments = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    List<Comment> handleJson(JsonNode inputNode) throws IOException {
        List<Comment> comments = new ArrayList<>();
        JsonNode commentsNode = extractNodeByKey(inputNode, COMMENTS_NODE_NAME);

        for (JsonNode node : commentsNode) {
            Comment comment = mapper.readValue(node.toString(), Comment.class);
            comments.add(comment);
        }
        return comments;
    }

    void commentMappingJsonToObject(JsonNode node) throws IOException {
        JsonNode commentsNode = extractNodeByKey(node, COMMENTS_NODE_NAME);

        for (JsonNode jsonNode : commentsNode) {
            if (jsonNode.has(SUBCOMMENT_NODE_NAME) && jsonNode.get(SUBCOMMENT_NODE_NAME).size() != 0) {
                //recursive mapping of subcomments
                commentMappingJsonToObject(jsonNode.get(SUBCOMMENT_NODE_NAME));
            }
        }
        comments.addAll(handleJson(node));
    }

    private JsonNode extractNodeByKey(JsonNode node, String key) {
        return (node.has(key))? node.get(key): node;
    }
}
