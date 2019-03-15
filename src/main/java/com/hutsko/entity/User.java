package com.hutsko.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Endpoint class that represents unique user, all his comments and some related information</p>
 */
public class User {
    private String name;
//    private int commentCounter;
    private int likeCounter;
    private int dislikeCounter;
    private Map<String, String> messages;   //k - timestamp, v - message's body


    public User(){
        messages = new HashMap<>();
    };


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public int getCommentCounter() {
//        return commentCounter;
//    }

//    public void setCommentCounter(int commentCounter) {
//        this.commentCounter = commentCounter;
//    }

    public int getLikeCounter() {
        return likeCounter;
    }

    public void setLikeCounter(int likeCounter) {
        this.likeCounter = likeCounter;
    }

    public int getDislikeCounter() {
        return dislikeCounter;
    }

    public void setDislikeCounter(int dislikeCounter) {
        this.dislikeCounter = dislikeCounter;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public void addMessage(String key, String value) {
        messages.put(key, value);
    }
}
