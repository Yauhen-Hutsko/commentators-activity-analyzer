package com.hutsko.entity;

import java.util.Map;

public class User {
    private String name;
    private Map<String, String> postCounter;

    public User(String name, Map<String, String> postCounter){
        this.name = name;
        this.postCounter = postCounter;
    };


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getPostCounter() {
        return postCounter;
    }

    public void setPostCounter(Map<String, String> postCounter) {
        this.postCounter = postCounter;
    }

}
