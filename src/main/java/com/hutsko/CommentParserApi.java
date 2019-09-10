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

    User getMostActiveUser();

    Comment getBestComment();

    Comment getWorstComment();

    User getBestUser();

    User getWorstUser();

    User getUserByName(String name);

    User getUserById(int id);

    List<Comment> findCommentsByText(String phrase);
}
