package com.hutsko;

import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This interface provides prepared entities for a front-end representation
 */
public interface CommentParserApi {
    List<LocalDateTime> getUserActivity(int userId);

    Comment getBestComment();

    Comment getWorstComment();

    User getBestUser();

    User getWorstUser();
}
