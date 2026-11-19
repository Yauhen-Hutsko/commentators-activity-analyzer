package com.hutsko.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserProfileRepository {
    private final PostgresConnector connector;
    private final String tableName;

    public UserProfileRepository() {
        this(new PostgresConnector(), DatabaseConfig.load().getProfileTable());
    }

    public UserProfileRepository(PostgresConnector connector, String tableName) {
        this.connector = connector;
        this.tableName = normalizeTableName(tableName);
    }

    public List<EnrichedUserProfile> findRecentProfiles(int limit) {
        int safeLimit = Math.max(limit, 1);
        String sql = "select * from " + tableName + " order by user_id desc limit ?";
        return connector.query(sql, this::mapProfile, safeLimit);
    }

    public Optional<EnrichedUserProfile> findByUserId(long userId) {
        String sql = "select * from " + tableName + " where user_id = ? limit 1";
        return connector.queryOne(sql, this::mapProfile, userId);
    }

    public List<java.util.Map<String, Object>> queryRaw(String sql, Object... params) {
        return connector.queryForMapList(sql, params);
    }

    private EnrichedUserProfile mapProfile(ResultSet resultSet) throws SQLException {
        return new EnrichedUserProfile(
                parseLong(resultSet.getString("user_id")),
                trim(resultSet.getString("username")),
                trim(resultSet.getString("profile_url")),
                trim(resultSet.getString("avatar_url")),
                trim(resultSet.getString("rank_title")),
                trim(resultSet.getString("status")),
                parseInteger(resultSet.getString("messages")),
                trim(resultSet.getString("years_on_site")),
                trim(resultSet.getString("source_prefix")),
                trim(resultSet.getString("created_at_raw")),
                trim(resultSet.getString("created_at_iso")),
                trim(resultSet.getString("profile_details_raw")),
                parseInteger(resultSet.getString("rating_place")),
                parseInteger(resultSet.getString("rating_karma")),
                parseInteger(resultSet.getString("rating_comments_total"))
        );
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.trim().isEmpty() ? null : Long.parseLong(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.trim().isEmpty() ? null : Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeTableName(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (!trimmed.matches("[A-Za-z0-9_.]+")) {
            throw new IllegalArgumentException("Invalid PostgreSQL table name: " + value);
        }
        return trimmed;
    }
}
