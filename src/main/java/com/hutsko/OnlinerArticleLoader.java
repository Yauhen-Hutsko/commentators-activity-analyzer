package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static Logger log = LoggerFactory.getLogger(OnlinerArticleLoader.class);

    private static final String DOM_CLASS_OF_VIEWS = "news-header__button_views";
    private static final int QUANTITY_OF_LAST_COMMENTS = 99999;
    private String sourceURL;
    private String title;
    private String views;
    private LocalDateTime created;
    private JsonNode fullResponseJsonNode;


    public OnlinerArticleLoader(String sourceURL) {
        this.sourceURL = sourceURL;
        Document HTMLDoc = readHtmlPageSourceCode();
        setMetaData(HTMLDoc);
        fullResponseJsonNode = uploadFullJsonFromURL(extractUrlForGettingComments(HTMLDoc));
    }

    @Override
    public Document readHtmlPageSourceCode() {
        Document document = null;
        try {
            document = Jsoup.connect(sourceURL).get();
        } catch (IOException e) {
            log.error("Couldn't connect to " + sourceURL);
            System.err.println(e.getMessage());
        }
        return document;
    }

    //TODO: NPE hazard
    @Override
    public JsonNode uploadFullJsonFromURL(String httpsRequestUrl) {
        StringBuilder stringBuilder = new StringBuilder();
        JsonNode commentsNode = null;
        try {
            URL url = new URL(httpsRequestUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            String inputLine = "";
            if (connection != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                reader.close();
            }
            ObjectMapper mapper = new ObjectMapper();
            commentsNode = mapper.readTree(stringBuilder.toString());
        } catch (MalformedURLException e) {
            log.error("Can't parse this URL \"" + httpsRequestUrl + "\"", e);
        } catch (IOException e) {
            log.error("Error occurred around BufferedReader. ", e);
        }
        return commentsNode;
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
    public JsonNode getFullResponseJsonNode() {
        return fullResponseJsonNode;
    }

    private String extractUrlForGettingComments(Document document) {
        Element comments = document.getElementById("fast-comments");
        if (comments == null) {
            Element newsWrapperSection = document.getElementsByClass("news-wrapper").first();
            return RequestUrlBuilder.buildUrlToClosedComments(newsWrapperSection, QUANTITY_OF_LAST_COMMENTS);
        }else {
            Node appNode = comments.getElementsByTag("app").first();
            return RequestUrlBuilder.buildUrl(appNode, QUANTITY_OF_LAST_COMMENTS);
        }
    }

    private void setMetaData(Document document) {
        title = !document.title().isEmpty() ? document.title() : "not found";
        views = document.getElementsByClass(DOM_CLASS_OF_VIEWS).first().childNode(0).toString();
        String creationDate = document.getElementsByAttributeValue("property", "article:published_time").first().attr("content");
        if (!creationDate.isEmpty()) {
            this.created = LocalDateTime.parse(creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("Europe/Minsk")));
        }
    }
}
