package com.hutsko.db;

/**
 * Read model for enriched user profile rows stored in PostgreSQL.
 */
public class EnrichedUserProfile {
    private final Long userId;
    private final String username;
    private final String profileUrl;
    private final String avatarUrl;
    private final String rankTitle;
    private final String status;
    private final Integer messages;
    private final String yearsOnSite;
    private final String sourcePrefix;
    private final String createdAtRaw;
    private final String createdAtIso;
    private final String profileDetailsRaw;
    private final Integer ratingPlace;
    private final Integer ratingKarma;
    private final Integer ratingCommentsTotal;

    public EnrichedUserProfile(Long userId,
                               String username,
                               String profileUrl,
                               String avatarUrl,
                               String rankTitle,
                               String status,
                               Integer messages,
                               String yearsOnSite,
                               String sourcePrefix,
                               String createdAtRaw,
                               String createdAtIso,
                               String profileDetailsRaw,
                               Integer ratingPlace,
                               Integer ratingKarma,
                               Integer ratingCommentsTotal) {
        this.userId = userId;
        this.username = username;
        this.profileUrl = profileUrl;
        this.avatarUrl = avatarUrl;
        this.rankTitle = rankTitle;
        this.status = status;
        this.messages = messages;
        this.yearsOnSite = yearsOnSite;
        this.sourcePrefix = sourcePrefix;
        this.createdAtRaw = createdAtRaw;
        this.createdAtIso = createdAtIso;
        this.profileDetailsRaw = profileDetailsRaw;
        this.ratingPlace = ratingPlace;
        this.ratingKarma = ratingKarma;
        this.ratingCommentsTotal = ratingCommentsTotal;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getRankTitle() {
        return rankTitle;
    }

    public String getStatus() {
        return status;
    }

    public Integer getMessages() {
        return messages;
    }

    public String getYearsOnSite() {
        return yearsOnSite;
    }

    public String getSourcePrefix() {
        return sourcePrefix;
    }

    public String getCreatedAtRaw() {
        return createdAtRaw;
    }

    public String getCreatedAtIso() {
        return createdAtIso;
    }

    public String getProfileDetailsRaw() {
        return profileDetailsRaw;
    }

    public Integer getRatingPlace() {
        return ratingPlace;
    }

    public Integer getRatingKarma() {
        return ratingKarma;
    }

    public Integer getRatingCommentsTotal() {
        return ratingCommentsTotal;
    }
}
