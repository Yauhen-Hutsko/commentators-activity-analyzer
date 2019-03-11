package com.hutsko.entity;

import java.util.Map;

/**
 * <p>Endpoint class that represents unique user, all his comments and some related information</p>
 */
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
