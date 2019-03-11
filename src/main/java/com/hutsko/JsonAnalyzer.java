package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.io.IOException;
import java.util.*;

/**
 * <p>This class uses for processing the JSON response from Onliner.by which contains all information
 * about comments.</p>
 */
public class JsonAnalyzer {

    private static final String COMMENTS_NODE_NAME = "comments";
    private static final String SUBCOMMENTS_NODE_NAME = "replies";
    private List<Comment> comments = new ArrayList<>();
    private Map<String, List<User>> commentators = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    void commentMappingJsonToObject(JsonNode node) throws IOException {
        JsonNode commentsNode = extractNodeByKey(node, COMMENTS_NODE_NAME);

        for (JsonNode jsonNode : commentsNode) {
            if (jsonNode.has(SUBCOMMENTS_NODE_NAME) && jsonNode.get(SUBCOMMENTS_NODE_NAME).size() != 0) {
                //recursive mapping of subcomments
                commentMappingJsonToObject(jsonNode.get(SUBCOMMENTS_NODE_NAME));
            }
        }
        comments.addAll(handleJson(node));
    }

    List<Comment> handleJson(JsonNode inputNode) throws IOException {
        List<Comment> comments = new ArrayList<>();
        JsonNode commentsNode = extractNodeByKey(inputNode, COMMENTS_NODE_NAME);

        for (JsonNode node : commentsNode) {
            Comment comment = mapper.readValue(node.toString(), Comment.class);
            comments.add(comment);
        }
        return comments;
    }

    private JsonNode extractNodeByKey(JsonNode node, String key) {
        return (node.has(key))? node.get(key): node;
    }

    public List<Comment> getComments() {
        return comments;
    }
}
