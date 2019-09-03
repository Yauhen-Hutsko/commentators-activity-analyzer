package com.hutsko.impl;

import com.hutsko.CommentParserApi;
import com.hutsko.JsonAnalyzer;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.time.LocalDateTime;
import java.util.*;

public class CommentParserApiImpl implements CommentParserApi {
    private JsonAnalyzer analyzer;

    public CommentParserApiImpl(JsonAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Provides the list of timestamps for creating diagram of user activity
     *
     * @param userId - unique user ID from onliner.by
     * @return list of {@link java.time.Instant}
     */
    @Override
    public final List<LocalDateTime> getUserActivity(int userId) {
        User user = analyzer.getCommentators().get(userId);
        List<LocalDateTime> timestamps = new ArrayList<>();
        for (String stamp : user.getMessages().keySet()) {
            LocalDateTime dateTime = LocalDateTime.parse(stamp.replaceAll("(\\+.*)", "")); //delete timeZone suffix before parsing
            timestamps.add(dateTime);
        }
        return timestamps;
    }

    /**
     * Provides a most liked {@code Comment}
     *
     * @return {@link com.hutsko.entity.Comment}
     */
    @Override
    public final Comment getBestComment() {
        Optional<Comment> comment = analyzer.getComments().stream().max(Comparator.comparingInt(Comment::getLikes));
        return comment.get();
    }

    /**
     * Provides a most unliked {@code Comment}
     *
     * @return {@link com.hutsko.entity.Comment}
     */
    @Override
    public final Comment getWorstComment() {
        Optional<Comment> comment = analyzer.getComments().stream().max(Comparator.comparingInt(Comment::getDislikes));
        return comment.get();
    }

    /**
     * Provides a user who got the least likes for all messages
     *
     * @return {@link com.hutsko.entity.User}
     */
    @Override
    public final User getBestUser() {
        Optional<User> user = analyzer.getCommentators().values().stream().max(Comparator.comparingInt(User::getLikeCounter));
        return user.get();
    }

    /**
     * Provides a user who got the most likes for all messages
     *
     * @return {@link com.hutsko.entity.User}
     */
    @Override
    public final User getWorstUser() {
        Optional<User> user = analyzer.getCommentators().values().stream().max(Comparator.comparingInt(User::getDislikeCounter));
        return user.get();
    }

    /**
     * @param name
     * @return a user with nickname(not an ID) visible above commentary on the site.
     */
    public User getUserByName(String name) {
        return analyzer.getCommentators().values().stream().filter(u -> u.getName().equals(name)).findFirst().get();
    }

    /**
     * @param id
     * @return a user with an ID visible in user profile on the site.
     */
    public User getUserById(int id) {
        return analyzer.getCommentators().values().stream().filter(u -> (u.getId() == id) ).findFirst().get();
    }

    /**
     * @param phrase word or sentence.
     * @return list of comments which contain desired phrase. If phrase is empty, then return no comments.
     */
    @Override
    public List<Comment> findCommentsByText(String phrase) {
        List<Comment> comments = analyzer.getComments();
        List<Comment> found = new ArrayList<>();

        // if phrase contains only whitespaces, then return empty list
        if (phrase.replaceAll(" ", "").length() == 0) {
            return found;
        }
        for (Comment comment : comments) {
            if (comment.getText().contains(phrase)) {
                found.add(comment);
            }
        }
        return found;
    }

}
