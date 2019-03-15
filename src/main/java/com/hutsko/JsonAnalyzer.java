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

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Create map of unique users with all history of their comments
     * @return HashMap where Key = userId, Value = User
     */
    Map<String, User> makeMeHappy(){
        Map<String, User> commentators = new HashMap<>();
        for (Comment comment : comments) {
            if (commentators.containsKey(comment.getAuthorId())) {
                //add comment to existed user
                createUserFromComment(comment, commentators.get(comment.getAuthorId()));
            } else {
                commentators.put(comment.getAuthorId(), createUserFromComment(comment, null));
            }
        }
        return commentators;
    }

    /**
     * It adds Comment into User.  New user will be created if <code>user</code> param is null.
     * Otherwise comment will be added to provided user.
     * @param comment
     * @param user
     * @return
     */
    private User createUserFromComment(final Comment comment, User user) {
        if (user == null) {
            user = new User();
        }
        user.setName(comment.getAuthorName());
//        user.setCommentCounter(comment.);
        user.setDislikeCounter(comment.getDislikes());
        user.setLikeCounter(comment.getLikes());
        user.addMessage(comment.getId(), comment.getText());
        return user;
    }

    /**
     * <p>Method saved list of comment objects into class variable {@code comments}. Use method {@code getComments()} for getting results.</p>
     * <p>It's void because otherwise recursive invocation returns only result from the method on the top of the stack.</p>
     * <p>todo: Consider double if-condition instead of recursion because max nesting level for 'onliner' = 1</p>
     */
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
