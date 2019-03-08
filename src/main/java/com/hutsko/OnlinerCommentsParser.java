package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class OnlinerCommentsParser {

    private static final int QUANTITY_OF_LAST_COMMENTS = 99999;
    private String pageUrl;

    OnlinerCommentsParser(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    Document readHtmlPageSourceCode() {
        Document document = null;
        try {
            document = Jsoup.connect(pageUrl).get();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return document;
    }

    String extractUrlForGettingComments(Document document) {
        Element element = document.getElementById("fast-comments");
        Node appNode = element.getElementsByTag("app").first();
        return RequestUrlBuilder.buildUrl(appNode, QUANTITY_OF_LAST_COMMENTS);
    }

    JsonNode uploadCommentsFromURL(String httpsRequestUrl) throws IOException {
        URL url = new URL(httpsRequestUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine = "";
        if (connection != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((inputLine = reader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            reader.close();
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(stringBuilder.toString());
    }


    void cannotThrowOldCode(Document document) {
        Element node = document;
        Elements allCommentElements = node.getElementsByAttributeValueStarting("id", "comment-");
        for (Element element : allCommentElements) {
            Element nameNode = element.getElementsByClass("news-comment__name").first();
            Element timeNode = element.getElementsByClass("news-comment__time").first();
            Elements likeNode = element.getElementsByClass("like-control news-comment__button news-comment__button_counter news-comment__button_counter_up");
            Elements dislikeNode = element.getElementsByClass("like-control news-comment__button news-comment__button_counter news-comment__button_counter_down");
            System.out.format(nameNode.text() + " has %s likes and %s dislikes\n", likeNode.text(), dislikeNode.text());
            System.out.println(timeNode.text());
//                commentators.putIfAbsent(uniqueName(nameNode),  );
//                User user = new User(nameNode.text(), );
//                System.out.println("nodeName: " + tupple.nodeName());
//                System.out.println("size: " + tupple.childNodeSize());
            System.out.println("attribute name is: " + element.attr("id") + "\n");

        }
//            System.out.println(document.title());
//            System.out.println("elements by tag:" +allCommentElements.size() + "\n");
//            System.out.println("discussionNode: " + discussionNode);
        System.out.println("amount of comments:  " + allCommentElements.size());
    }
}
