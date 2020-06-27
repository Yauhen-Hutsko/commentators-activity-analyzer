package com.hutsko.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class OnlinerUsers {
    private static final String RATING_ONLINER_DOMAIN = "https://rating.onliner.by";
    private static final String RATING_ALL_USERS = "https://rating.onliner.by/rating/users?type=overall";
    private static final String PROFILE_USER_API = "https://profile.onliner.by/sdapi/user.api/users/";
    private ObjectMapper mapper = new ObjectMapper();

    private ArrayNode userProfiles;
    private Document sourceHTML;
    private ObjectNode rootNode;
    private String nextPageRelativePath;

    public OnlinerUsers() {
        rootNode = mapper.createObjectNode();
        userProfiles = mapper.createArrayNode();
        sourceHTML = readHtmlPageSourceCode(RATING_ALL_USERS);
        nextPageRelativePath = sourceHTML.getElementsByClass("next-page").first().getElementsByTag("a").first().attr("href");
    }

    /**
     * Make preload of data:
     * 1. Save to collection all user profile links
     * 2. Retrive user-data in loop.
     */
    public void obtainUserProfileURLs() {
        long startTime = System.currentTimeMillis();
        do {
            String nextPageAbsolutePath = RATING_ONLINER_DOMAIN + nextPageRelativePath;
            readHtmlPage();
            sourceHTML = readHtmlPageSourceCode(nextPageAbsolutePath);
        } while (!nextPageRelativePath.isEmpty());

        long endTime = System.currentTimeMillis();
        rootNode.put("timeConsumed", Duration.ofMillis(endTime - startTime).toMinutes());
        rootNode.put("total", userProfiles.size());
        saveAsJson();
    }

    private boolean saveAsJson() {
        File file = new File("/OnlinerUsersLIST.json");
        try {
            rootNode.set("userId", userProfiles);
            mapper.writeValue(file, rootNode);
            System.out.println("The work should be done.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Nope.");
        return false;
    }

    private void readHtmlPage() {
        Element result = sourceHTML.getElementsByClass("next-page").first();
        if (result != null) {
            nextPageRelativePath = result.getElementsByTag("a").first().attr("href");
        } else {
            nextPageRelativePath = "";
        }
        Elements users = sourceHTML.getElementsByClass("user");
        for (Element user : users) {
            String profilePath = user.attr("href");
//            String userApiUrl = PROFILE_USER_API + profilePath.substring(profilePath.lastIndexOf('/'));
            String userId = profilePath.substring(profilePath.lastIndexOf('/') + 1);
            userProfiles.add(userId);
//            JsonNode userNode;
//            userNode = JsonProcessing.uploadJsonFromURL(userApiUrl);
//            rootNode.set(profilePath, userNode);
        }
    }

    public ObjectNode getRootNode() {
        return rootNode;
    }

    private Document readHtmlPageSourceCode(String pageUrl) {
        Document document = null;
        try {
            document = Jsoup.connect(pageUrl).get();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return document;
    }
}
