package com.hutsko.impl;

import com.hutsko.CommentParserApi;
import com.hutsko.JsonAnalyzer;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;

public class CommentParserApiImpl implements CommentParserApi {
    JsonAnalyzer analyzer;

    public CommentParserApiImpl(JsonAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public final Instant getUserActivity(){
        return null;
    }

    @Override
    public final Comment getBestComment(){
        Optional<Comment> comment = analyzer.getComments().stream().max(Comparator.comparingInt(Comment::getLikes));
        return comment.get();
    }

    @Override
    public final Comment getWorstComment(){
        Optional<Comment> comment = analyzer.getComments().stream().max(Comparator.comparingInt(Comment::getDislikes));
        return comment.get();
    }

    @Override
    public final User getBestUser(){
        Optional<User> user = analyzer.getCommentators().values().stream().max(Comparator.comparingInt(User::getLikeCounter));
        return user.get();
    }

    @Override
    public final User getWorstUser(){
        Optional<User> user = analyzer.getCommentators().values().stream().max(Comparator.comparingInt(User::getDislikeCounter));
        return user.get();
    }
}
