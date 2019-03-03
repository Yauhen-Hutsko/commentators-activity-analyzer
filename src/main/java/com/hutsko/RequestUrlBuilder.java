package com.hutsko;

import org.jsoup.nodes.Node;

public class RequestUrlBuilder {
    private static final String SLASH = "/";
    private static final String COMMENT_API_PATH_START = "https://comments.api.onliner.by";
    private static final String COMMENT_API_PATH_END = SLASH + "comments?limit=%s&v=0.33252155200390354"; // seems like 'v' is unnecessary parameter and used by onliner like hash for comments.

    static String buildUrl(Node appNode, int quantity) {
        String name_A = SLASH + appNode.attr("project-name");
        String entity_B = SLASH + appNode.attr("entity-type");
        String id_C = SLASH + appNode.attr("entity-id");
        StringBuilder path = new StringBuilder(COMMENT_API_PATH_START).
                append(name_A).
                append(entity_B).
                append(id_C).
                append(String.format(COMMENT_API_PATH_END, quantity));
        return path.toString();
    }


}
