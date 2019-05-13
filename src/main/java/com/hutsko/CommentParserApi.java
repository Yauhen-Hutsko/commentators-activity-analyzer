package com.hutsko;

import com.hutsko.entity.Comment;
import com.hutsko.entity.User;

import java.time.Instant;

public interface CommentParserApi {
    Instant getUserActivity();

    Comment getBestComment();

    Comment getWorstComment();

    User getBestUser();

    User getWorstUser();
}
