package com.hutsko;

import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.time.Instant;
import java.util.List;

/**
 * This interface provides prepared entities for a front-end representation
 */
public interface CommentParserApi {
    List<Instant> getUserActivity(int userId);

    Comment getBestComment();

    Comment getWorstComment();

    User getBestUser();

    User getWorstUser();
}
