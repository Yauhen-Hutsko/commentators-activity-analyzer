package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutsko.util.JsonProcessing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OnlinerArticleLoader implements ArticleLoader {
    private static final String DOM_CLASS_OF_VIEWS = "news-header__button_views";
    private static final int QUANTITY_OF_LAST_COMMENTS = 99999;
    private static Logger log = LoggerFactory.getLogger(OnlinerArticleLoader.class);
    private String sourceURL;
    private String title;
    private String views;
    private LocalDateTime created;
    private JsonNode commentsRootNode;


    public OnlinerArticleLoader(String sourceURL) {
        this.sourceURL = sourceURL;
        Document HTMLDoc = readHtmlPageSourceCode();
        setMetaData(HTMLDoc);
        commentsRootNode = JsonProcessing.uploadJsonFromURL(extractUrlForGettingComments(HTMLDoc));
    }

    @Override
    public Document readHtmlPageSourceCode() {
        Document document = null;
        try {
            document = Jsoup.connect(sourceURL).get();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return document;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getViews() {
        return views;
    }

    @Override
    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public JsonNode getCommentsRootNode() {
        return commentsRootNode;
    }

    private String extractUrlForGettingComments(Document document) {
        Element comments = document.getElementById("fast-comments");
        Node appNode = comments.getElementsByTag("app").first();
        return RequestUrlBuilder.buildUrl(appNode, QUANTITY_OF_LAST_COMMENTS);
    }

    private void setMetaData(Document document) {
        title = !document.title().isEmpty() ? document.title() : "not found";
        views = document.getElementsByClass(DOM_CLASS_OF_VIEWS).first().childNode(0).toString();
        String creationDate = document.getElementsByAttributeValue("name", "article:published_time").first().attr("content");
        if (!creationDate.isEmpty()) {
            this.created = LocalDateTime.parse(creationDate, DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy H:mm", Locale.US).withZone(ZoneId.of("Europe/Minsk")));
        }
    }
}
