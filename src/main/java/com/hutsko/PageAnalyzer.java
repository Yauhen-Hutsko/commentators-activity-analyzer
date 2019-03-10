
package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Duration;

public class PageAnalyzer {
    private static final String FILE_PATH = "https://tech.onliner.by/2019/02/20/muzykant-v-it";
    public static final String GET_ALL_COMMENTS_REQUEST_PATH = "https://comments.api.onliner.by/news/tech.post/414510/comments?limit=99999";


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        OnlinerCommentsParser parser = new OnlinerCommentsParser(GET_ALL_COMMENTS_REQUEST_PATH);
        try {
            JsonNode commentsNode = parser.uploadCommentsFromURL(GET_ALL_COMMENTS_REQUEST_PATH);
            JsonAnalyzer analyzer = new JsonAnalyzer();
            analyzer.commentMappingJsonToObject(commentsNode);

            System.out.println(analyzer.comments.size());
            long endTime = System.currentTimeMillis();
            System.out.println("Time of evaluating(second): " + Duration.ofMillis(endTime-startTime));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String uniqueName(Element element) {
        Element raw = element.getElementsByAttribute("href").get(1);
        String link = raw.attr("href");
        int lastSeparatorPosition = link.lastIndexOf("/");
        return link.substring(lastSeparatorPosition + 1);
    }
}
