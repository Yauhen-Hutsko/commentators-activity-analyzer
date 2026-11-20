package com.hutsko.tools.articles;

import java.time.OffsetDateTime;

/**
 * Data extracted from a single article page. Any field may be null when the page does not provide it.
 */
public final class ArticleDetails {
    Long postId;
    String title;
    OffsetDateTime publishedAt;
    String[] authors;
    String[] categories;
    String[] tags;
    String articleType;
    Integer wordCount;
    Integer views;
    String imageUrl;
    Boolean special;
    String pageMetaJson;

    public Long getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String[] getCategories() {
        return categories;
    }

    public String[] getTags() {
        return tags;
    }

    public String getArticleType() {
        return articleType;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public Integer getViews() {
        return views;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Boolean getSpecial() {
        return special;
    }

    public String getPageMetaJson() {
        return pageMetaJson;
    }
}
