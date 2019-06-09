package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class OnlinerCommentsLoader {

    private static final int QUANTITY_OF_LAST_COMMENTS = 99999;
    private String pageUrl;
    private String title;
    private LocalDateTime created;

    OnlinerCommentsLoader(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    //TODO: make this method public API and make methods below private. Tests?
    Document readHtmlPageSourceCode() {
        Document document = null;
        try {
            document = Jsoup.connect(pageUrl).get();
            setMetaData(document);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return document;
    }

    String extractUrlForGettingComments(Document document) {
        Element comments = document.getElementById("fast-comments");
        Node appNode = comments.getElementsByTag("app").first();
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

    private void setMetaData(Document document) {
        this.title = document.title();
        String creationDate = document.getElementsByAttributeValue("name", "article:published_time").first().attr("content");
        this.created = LocalDateTime.parse(creationDate, DateTimeFormatter.ofPattern(
                "dd MMMM yyyy H:mm", Locale.US).withZone(ZoneId.of("Europe/Minsk")));

    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreated() {
        return created;
    }
}
