package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import com.hutsko.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class JsonAnalyzer {
    private final JsonNode fullJson;
    private Pattern pattern = Pattern.compile("\\d{1,4}");
    private Map<String, List<User>> commentators = new HashMap<>();

    public JsonAnalyzer(JsonNode fullJson) {
        this.fullJson = fullJson;
    }

    void handleJson(){
        JsonNode commentsNode = fullJson.get("comments");
        for (JsonNode node : commentsNode) {

//            System.out.println(node.findValue("id"));
            System.out.println(node.findValues("id"));
        }
    }

    void userJsonToObjectMapping(JsonNode node){
        JsonNode replies;
        if ( !(replies = node.get("replies")).isNull()) {
            userJsonToObjectMapping(replies);
        }

    }
}
