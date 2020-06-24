package com.hutsko.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutsko.OnlinerArticleLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class JsonProcessing {
    private static Logger log = LoggerFactory.getLogger(JsonProcessing.class);

    //TODO: NPE hazard. Double from OnlinerArticleLoader class
    public static JsonNode uploadJsonFromURL(String httpsRequestUrl) {
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
}
