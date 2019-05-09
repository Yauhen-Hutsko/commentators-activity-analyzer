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


public class OnlinerCommentsLoader {

    private static final int QUANTITY_OF_LAST_COMMENTS = 99999;
    private String pageUrl;

    OnlinerCommentsLoader(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    //TODO: make this method public API and make methods below private. Tests?
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
}
