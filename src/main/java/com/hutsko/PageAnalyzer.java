
package com.hutsko;

import com.hutsko.entity.User;
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

    private static Map<String, String> commentators = new HashMap<>();

    public static void main(String[] args) {
        try {
            File sourceFile = new File(FILE_PATH);
            Document document = Jsoup.parse(sourceFile, "UTF-8");
            Element node = document.getElementById("comments-list");
            Elements allCommentElements = node.getElementsByAttributeValueStarting("id", "comment-");
//            Element discussionNode = document.getElementById("fast-comments");

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
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    static String uniqueName(Element element) {
        Element raw = element.getElementsByAttribute("href").get(1);
        String link = raw.attr("href");
        int lastSeparatorPosition = link.lastIndexOf("/");
        return link.substring(lastSeparatorPosition + 1);
    }
}
