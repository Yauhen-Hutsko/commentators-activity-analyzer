package com.hutsko.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {
    private String id;
    private String authorName;
    private String authorId;
    private int likes;
    private int dislikes;
    private String created;
    private String text;

    public Comment() {
    }

    public Comment(String id, String authorName, String authorId, int likes, int dislikes, String created, String text) {
        this.id = id;
        this.authorName = authorName;
        this.authorId = authorId;
        this.likes = likes;
        this.dislikes = dislikes;
        this.created = created;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public String getCreated() {
        return created;
    }

    public String getText() {
        return text;
    }

    @JsonSetter("id")
    public void setId(String id) {
        this.id = id;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    @JsonSetter("created_at")
    public void setCreated(String created) {
        this.created = created;
    }

    @JsonSetter
    public void setText(String text) {
        this.text = text;
    }

    @JsonSetter("marks")
    public void setMarks(Map<String, String> map) {
        setDislikes(Integer.parseInt(map.get("dislikes")));
        setLikes(Integer.parseInt(map.get("likes")));
    }

    @JsonSetter("author")
    public void setAuthorData(Map<String, String> map) {
        setAuthorId(map.get("id"));
        setAuthorName(map.get("name"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return likes == comment.likes &&
                dislikes == comment.dislikes &&
                Objects.equals(id, comment.id) &&
                Objects.equals(authorName, comment.authorName) &&
                Objects.equals(authorId, comment.authorId) &&
                Objects.equals(created, comment.created) &&
                Objects.equals(text, comment.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, authorName, authorId, likes, dislikes, created, text);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorId='" + authorId + '\'' +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", created='" + created + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
