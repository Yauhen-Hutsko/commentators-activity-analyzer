package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.hutsko.entity.Comment;
import com.hutsko.entity.User;
import com.hutsko.impl.CommentParserApiImpl;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AppEntryPoint {
    public static final String GET_ALL_COMMENTS_REQUEST = "https://comments.api.onliner.by/news/tech.post/414510/comments?limit=99999";
    private static final String FILE_PATH = "https://people.onliner.by/2018/12/07/dating-2";
    private static final String FILE_PATH_2 = "https://tech.onliner.by/2019/02/20/muzykant-v-it";
    private static final String FILE_PATH_3 = "https://people.onliner.by/opinions/2019/01/16/mnenie-1045";
    private static final String FILE_PATH_4 = "https://tech.onliner.by/2019/01/23/ushel-v-it";
    private static final String FILE_PATH_5 = "https://tech.onliner.by/2019/05/20/it-62";
    private static final String FILE_PATH_6 = "https://tech.onliner.by/2019/03/25/it-56";
    private static final String FILE_PATH_7 = "https://tech.onliner.by/2019/08/27/v-it";
    private static final String FILE_PATH_8 = "https://tech.onliner.by/2018/12/14/medit";
    private static final String FILE_PATH_9 = "https://tech.onliner.by/2019/06/18/zhenshhiny-v-it";
    private static final String FILE_PATH_1 = "https://tech.onliner.by/2019/07/18/it-68";
    private static final String FILE_PATH_0 = "https://tech.onliner.by/2020/03/06/money-37";
    private static final String FILE_PATH_10 = "https://people.onliner.by/2020/02/28/koronavirus-7";


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        OnlinerCommentsLoader parser = new OnlinerCommentsLoader(FILE_PATH_0);
        Document document = parser.readHtmlPageSourceCode();
        String commentsUrl = parser.extractUrlForGettingComments(document);
        try {
            JsonNode commentsNode = parser.uploadCommentsFromURL(commentsUrl);
            JsonAnalyzer jsonAnalyzer = new JsonAnalyzer();
            jsonAnalyzer.fillComments(commentsNode);
            jsonAnalyzer.fillCommentators();
            CommentParserApi cpa = new CommentParserApiImpl(jsonAnalyzer);
            System.out.format("The article \"%s\" was created at %s%n", parser.getTitle(), parser.getCreated());
            System.out.println("Quantity of comments: " + jsonAnalyzer.getComments().size());

            Map<Integer, User> userMap = jsonAnalyzer.getCommentators();
            System.out.println("Unique users: " + userMap.size());

            Optional<User> bestUser = Optional.of(cpa.getBestUser());
            Optional<User> worstUser = Optional.of(cpa.getWorstUser());
            Optional<Comment> bestComment = Optional.of(cpa.getBestComment());
            Optional<Comment> worstComment = Optional.of(cpa.getWorstComment());
            List<Comment> commentList = cpa.findCommentsByText("кто будет");
//            List<Comment> commentList = cpa.findCommentsByText("курсы");
//            List<Comment> commentList = cpa.findCommentsByText("врач");
            User user = cpa.getUserByName("Miss_3003");
//            List<LocalDateTime> userActivity = cpa.getUserActivity(484273);
//            String userName = userMap.get(484273).getName();

            for (Comment comment : commentList) {
                System.out.println(comment.getText());
                System.out.println("------------------");
            }

//            System.out.println(user);
            System.out.println(cpa.getMostActiveUser());
//            System.out.println("Comments are found: " + commentList.size());
//            System.out.format("User %1s : %2s", userName, userActivity);
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
