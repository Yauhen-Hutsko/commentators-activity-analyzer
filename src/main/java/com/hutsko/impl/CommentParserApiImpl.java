package com.hutsko.impl;

import com.hutsko.CommentParserApi;
import com.hutsko.JsonAnalyzer;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public class CommentParserApiImpl implements CommentParserApi {
    JsonAnalyzer analyzer;

    public CommentParserApiImpl(JsonAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Provides the list of timestamps for creating diagram of user activity
     * @return list of {@link java.time.Instant}
     * @param userId
     */
    @Override
    public final List<Instant> getUserActivity(int userId){
        User user = analyzer.getCommentators().get(userId);
        List<Instant> timestamps = new ArrayList<>();
        return null;
    }

    /**
     * Provides a most liked {@code Comment}
     * @return {@link com.hutsko.entity.Comment}
     */
    @Override
    public final Comment getBestComment(){
        Optional<Comment> comment = analyzer.getComments().stream().max(Comparator.comparingInt(Comment::getLikes));
        return comment.get();
    }

    /**
     * Provides a most unliked {@code Comment}
     * @return {@link com.hutsko.entity.Comment}
     */
    @Override
    public final Comment getWorstComment(){
        Optional<Comment> comment = analyzer.getComments().stream().max(Comparator.comparingInt(Comment::getDislikes));
        return comment.get();
    }

    /**
     * Provides a user who got the least likes for all messages
     * @return {@link com.hutsko.entity.User}
     */
    @Override
    public final User getBestUser(){
        Optional<User> user = analyzer.getCommentators().values().stream().max(Comparator.comparingInt(User::getLikeCounter));
        return user.get();
    }

    /**
     * Provides a user who got the most likes for all messages
     * @return {@link com.hutsko.entity.User}
     */
    @Override
    public final User getWorstUser(){
        Optional<User> user = analyzer.getCommentators().values().stream().max(Comparator.comparingInt(User::getDislikeCounter));
        return user.get();
    }
}
