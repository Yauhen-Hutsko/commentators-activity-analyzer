package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.hutsko.entity.User;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class PageAnalyzer {
    public static final String GET_ALL_COMMENTS_REQUEST = "https://comments.api.onliner.by/news/tech.post/414510/comments?limit=99999";
    private static final String FILE_PATH = "https://tech.onliner.by/2019/02/20/muzykant-v-it";
    private static final String FILE_PATH_2 = "https://people.onliner.by/2018/12/07/dating-2";


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        OnlinerCommentsLoader parser = new OnlinerCommentsLoader(FILE_PATH_2);
        Document document = parser.readHtmlPageSourceCode();
        String commentsUrl = parser.extractUrlForGettingComments(document);
        try {
            JsonNode commentsNode = parser.uploadCommentsFromURL(commentsUrl);
            JsonAnalyzer jsonAnalyzer = new JsonAnalyzer();
            jsonAnalyzer.fillComments(commentsNode);
            System.out.println("Quantity of comments: " + jsonAnalyzer.getComments().size());

            Map<String, User> userMap = jsonAnalyzer.getAllUsers();
            System.out.println("Unique users: " + userMap.size());

            Optional<User> user = userMap.values().stream().max(Comparator.comparingInt(o -> o.getMessages().size()));
            System.out.println(user.get());
            long endTime = System.currentTimeMillis();
            System.out.println("Time of evaluating(second): " + Duration.ofMillis(endTime - startTime));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getting unique name of user from Json is more convenient
     */
    @Deprecated
    static String uniqueName(Element element) {
        Element raw = element.getElementsByAttribute("href").get(1);
        String link = raw.attr("href");
        int lastSeparatorPosition = link.lastIndexOf("/");
        return link.substring(lastSeparatorPosition + 1);
    }
}
