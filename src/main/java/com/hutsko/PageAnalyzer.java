
package com.hutsko;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PageAnalyzer {
    private static final String FILE_PATH = "src/main/resource/fullPage.html";

    private Map<String, String> commentators = new HashMap<>();

    public static void main(String[] args) {
        try {
            File sourceFile = new File(FILE_PATH);
            Document document = Jsoup.parse(sourceFile, "UTF-8");
            Element node = document.getElementById("comments-list");
            Elements allCommentElements = node.getElementsByAttributeValueStarting("id", "comment-");
//            Element discussionNode = document.getElementById("fast-comments");

            for (Element element : allCommentElements) {
                Elements nameNode = element.getElementsByClass("news-comment__name");
                Elements timeNode = element.getElementsByClass("news-comment__time");
                System.out.println(nameNode.text());
                System.out.println(timeNode.text());
//                System.out.println("nodeName: " + tupple.nodeName());
//                System.out.println("size: " + tupple.childNodeSize());
//                System.out.println("attribute name is: " + tupple.attr( "id") + "\n");

            }
//            System.out.println(document.title());
//            System.out.println("elements by tag:" +allCommentElements.size() + "\n");
//            System.out.println("discussionNode: " + discussionNode);
            System.out.println("amount of comments:  " + allCommentElements.size());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
