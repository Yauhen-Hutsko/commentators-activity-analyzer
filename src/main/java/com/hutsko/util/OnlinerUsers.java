package com.hutsko.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OnlinerUsers {
    private static final String ONLINER_DOMAIN = "https://rating.onliner.by";
    private static final String URL_ALL_USERS = "https://rating.onliner.by/rating/users?type=overall";
    private static final String URL_USER_API = "https://profile.onliner.by/sdapi/user.api/users";
    private ObjectMapper mapper = new ObjectMapper();

    private Set<String> userProfilesSet = new HashSet<>();
    private Document sourceHTML;
    private ObjectNode rootNode;
    private String nextPageRelativePath;

    public OnlinerUsers() {
        sourceHTML = readHtmlPageSourceCode(URL_ALL_USERS);
        rootNode = mapper.createObjectNode();
        nextPageRelativePath = sourceHTML.getElementsByClass("next-page").first().getElementsByTag("a").first().attr("href");
    }

    /**
     * Make preload of data:
     * 1. Save to collection all user profile links
     * 2. Retrive user-data in loop.
     */
    public void obtainUserProfileURLs() {
        do {
            String nextPageAbsolutePath = ONLINER_DOMAIN + nextPageRelativePath;
            readHtmlPage();
            sourceHTML = readHtmlPageSourceCode(nextPageAbsolutePath);
        } while (!nextPageRelativePath.isEmpty());
        save();
    }

    private boolean save() {
        Path file = Paths.get("/OnlinerUsersLIST.txt");
        try {
            Files.write(file, userProfilesSet, UTF_8);
            System.out.println("The work should be done.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Nope.");
        return false;
    }

    private void readHtmlPage() {
        Elements users = sourceHTML.getElementsByClass("user");
        Element result = sourceHTML.getElementsByClass("next-page").first();
        if (result != null) {
            nextPageRelativePath = result.getElementsByTag("a").first().attr("href");
        } else {
            nextPageRelativePath = "";
        }
        for (Element user : users) {
            String profilePath = user.attr("href");
            String userApiUrl = URL_USER_API + profilePath.substring(profilePath.lastIndexOf('/'));
            userProfilesSet.add(userApiUrl);
//            JsonNode userNode;
//            userNode = JsonProcessing.uploadJsonFromURL(userApiUrl);
//            rootNode.set(profilePath, userNode);
        }
    }

    public ObjectNode getRootNode() {
        return rootNode;
    }

    public Set<String> getUserProfilesSet() {
        return userProfilesSet;
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
