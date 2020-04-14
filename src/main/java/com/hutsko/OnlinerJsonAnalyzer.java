package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This class uses for processing the JSON response from Onliner.by which contains all information
 * about comments.</p>
 */
public class OnlinerJsonAnalyzer implements JsonAnalyzer {
    private static final String COMMENTS_NODE_NAME = "comments";
    private static final String SUBCOMMENTS_NODE_NAME = "replies";
    private final List<Comment> comments = new ArrayList<>();
    private final Map<Integer, User> users = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    public OnlinerJsonAnalyzer(JsonNode jsonNode) {
        fillComments(jsonNode);
        fillCommentators();
    }

    /**
     * @return all comments left by all users.
     */
    public List<Comment> getComments() {
        return comments;
    }

    public Map<Integer, User> getCommentators() {
        return users;
    }

    /**
     * <p>Create map of unique users with all history of their comments and store it in a hashMap {@code users}
     * where Key = userId, Value = User</p>
     * <p>Use the method {@code getCommentators()} for getting results.</p>
     */
    public void fillCommentators() {
        Map<Integer, User> commentators = new HashMap<>();
        for (Comment comment : comments) {
            if (commentators.containsKey(comment.getAuthorId())) {
                //add comment to existed user
                createUserFromComment(comment, commentators.get(comment.getAuthorId()));
            } else {
                commentators.put(comment.getAuthorId(), createUserFromComment(comment, null));
            }
        }
        users.putAll(commentators);
    }

    /**
     * <p>Method saves the list of comment objects into the class variable {@code comments}. Use the method
     * {@code getComments()} for getting all comments left by all users.</p>
     * <p>It's void because otherwise recursive invocation returns only result from the method on the top of the stack.</p>
     */
    //todo: Consider double if-condition instead of recursion because max nesting level for 'onliner' = 1
    public void fillComments(final JsonNode node) {
        JsonNode commentsNode = extractNodeByKey(node, COMMENTS_NODE_NAME);

        for (JsonNode jsonNode : commentsNode) {
            if (jsonNode.has(SUBCOMMENTS_NODE_NAME) && jsonNode.get(SUBCOMMENTS_NODE_NAME).size() != 0) {
                //recursive mapping of subcomments
                fillComments(jsonNode.get(SUBCOMMENTS_NODE_NAME));
            }
        }
        comments.addAll(extractCommentsFromJsonToObject(commentsNode));
    }

    List<Comment> extractCommentsFromJsonToObject(JsonNode inputNode) {
        List<Comment> comments = new ArrayList<>();
        JsonNode commentsNode = extractNodeByKey(inputNode, COMMENTS_NODE_NAME);

        for (JsonNode node : commentsNode) {
            Comment comment = null;
            try {
                comment = mapper.readValue(node.toString(), Comment.class);
            } catch (IOException e) {
                //TODO: add logging
                e.printStackTrace();
            }
            comments.add(comment);
        }
        return comments;
    }

    /**
     * It adds Comment into User.  New user will be created if <code>user</code> param is null.
     * Otherwise comment will be added to provided user.
     *
     * @param comment stores data obtained from json comment-node
     * @param user    is an author of the comment
     * @return User object that contains name, id, reputation and all his messages.
     */
    private User createUserFromComment(final Comment comment, User user) {
        if (user == null) {
            user = new User();
        }
        user.setId(comment.getAuthorId());
        user.setName(comment.getAuthorName());
        user.setDislikeCounter(user.getDislikeCounter() + comment.getDislikes());
        user.setLikeCounter(user.getLikeCounter() + comment.getLikes());
        user.addMessage(comment.getCreated(), comment.getText());
        return user;
    }

    private JsonNode extractNodeByKey(JsonNode node, String key) {
        return (node.has(key)) ? node.get(key) : node;
    }
}
