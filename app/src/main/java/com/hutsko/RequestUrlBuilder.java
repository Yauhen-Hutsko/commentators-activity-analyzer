package com.hutsko;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * <p>As long as onliner's comments don't loaded on the page by default and fetched by script,
 * it necessary to get this script from source page.</p>
 * <p>{@code buildUrl(Node node, int quantity)} gets already extracted 'app' node, parses it and combine correct
 * request for getting comments </p>
 */
class RequestUrlBuilder {
    private static final String SLASH = "/";
    private static final String PREFIX_NEWS = "news";
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

    static String buildUrlToClosedComments(Element htmlPageTag, int quantity) {
        String name_A = SLASH +  PREFIX_NEWS;
        //todo: entity_B should be extracted requested URL
        String entity_B = htmlPageTag.getElementsByAttribute("data-io-article-url").attr("data-io-article-url");
        String id_C = SLASH + htmlPageTag.getElementsByAttribute("news_id").attr("news_id");
        StringBuilder path = new StringBuilder(COMMENT_API_PATH_START)
                .append(name_A)
                .append(SLASH)
                .append(entity_B.substring(8, entity_B.indexOf('.')))
                .append(".post")
                .append(id_C)
                .append(String.format(COMMENT_API_PATH_END, quantity));
        return path.toString();
    }

}
