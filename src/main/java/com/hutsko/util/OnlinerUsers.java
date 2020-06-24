package com.hutsko.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class OnlinerUsers {
    private static final String URL_ALL_USERS = "https://rating.onliner.by/rating/users?type=overall";
    private static final String URL_USER_API = "https://profile.onliner.by/sdapi/user.api/users";
    public static final String ONLINER_DOMAIN = "https://rating.onliner.by";

    private ObjectMapper mapper = new ObjectMapper();

    private Document sourceHTML;
    private ObjectNode rootNode;

    public OnlinerUsers() {
        sourceHTML = readHtmlPageSourceCode(URL_ALL_USERS);
        rootNode = mapper.createObjectNode();
    }

    /**
     * Make preload of data:
     * 1. Save to collection all links to user profiles
     * 2. Retrive user-data in loop.
     */
    public void generateUserList() {
        JsonNode userNode;
        String nextPageRelativePath;

        Elements users = sourceHTML.getElementsByClass("user");
        for (Element user : users) {
            String s = user.attr("href");
            String b = URL_USER_API + s.substring(s.lastIndexOf('/'));
            userNode = JsonProcessing.uploadJsonFromURL(b);
            rootNode.set(s, userNode);
        }
        nextPageRelativePath = sourceHTML.getElementsByClass("next-page").first().getElementsByTag("a").first().attr("href");
        if (!nextPageRelativePath.isEmpty()) {
            String nextPage = ONLINER_DOMAIN + nextPageRelativePath;
            sourceHTML = readHtmlPageSourceCode(nextPage);
            generateUserList();
        } else {
            System.out.println("next page not found. Current page: " + nextPageRelativePath);
        }
//        return rootNode;
    }

    public ObjectNode getRootNode() {
        return rootNode;
    }
//    public void readHtmlPage(String url) {
//        Elements users = sourceHTML.getElementsByClass("user");
//        for (Element user : users) {
//            String s = user.attr("href");
//            String b = URL_USER_API + s.substring(s.lastIndexOf('/'));
//            userNode = JsonProcessing.uploadJsonFromURL(b);
//            rootNode.set(s, userNode);
//        }
//    }

    public Document readHtmlPageSourceCode(String pageUrl) {
        Document document = null;
        try {
            document = Jsoup.connect(pageUrl).get();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return document;
    }
}
