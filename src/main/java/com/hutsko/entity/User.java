package com.hutsko.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Entity class that represents unique user, all his comments and some related information</p>
 */
public class User {
    private final Map<String, String> messages = new HashMap<>();   //k - timestamp, v - message's body
    private int id;
    private String name;
    private int likeCounter;
    private int dislikeCounter;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public void addMessage(String key, String value) {
        messages.put(key, value);
    }

    @Override
    public String toString() {
        return "User{"
                + "id=" + id + "\n"
                + "name='" + name + '\'' + "\n"
                + ", likeCounter=" + likeCounter + "\n"
                + ", dislikeCounter=" + dislikeCounter + "\n"
                + ", messages(total: " + messages.size() + ")=" + printMessagesToConsole()
                + '}';
    }

    private String printMessagesToConsole() {
        StringBuilder sb = new StringBuilder("\n");
        for (String value : messages.values()) {
            sb.append("-> ").append(value).append("\n");
        }
        return sb.toString();
    }
}
