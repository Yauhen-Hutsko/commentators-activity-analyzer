package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public interface ArticleLoader {
    String getTitle();

    LocalDateTime getCreated();

    JsonNode getCommentsNode();

    JsonNode uploadCommentsFromURL(String httpsRequestUrl);
}
