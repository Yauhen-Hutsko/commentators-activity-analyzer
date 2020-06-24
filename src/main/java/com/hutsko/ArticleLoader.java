package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;

import java.time.LocalDateTime;

public interface ArticleLoader {

    String getTitle();

    String getViews();

    LocalDateTime getCreated();

    JsonNode getCommentsRootNode();

    Document readHtmlPageSourceCode();

}
