package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.util.List;
import java.util.Map;

public interface JsonAnalyzer {
    void fillCommentators();

    void fillComments(final JsonNode node);

    List<Comment> getComments();

    Map<Integer, User> getCommentators();

    int getTotalBeforeModeration();

    int getTotalAfterModeration();
}
