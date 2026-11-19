package com.hutsko.db;

import java.util.Locale;

/**
 * Database connection settings resolved from system properties first and then environment variables.
 */
public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/commentators";
    private static final String DEFAULT_USER = "commentators";
    private static final String DEFAULT_PASSWORD = "commentators";
    private static final String DEFAULT_PROFILE_TABLE = "user_profiles_enriched";
    private static final String DEFAULT_DRIVER = "org.postgresql.Driver";

    private final String url;
    private final String user;
    private final String password;
    private final String driverClassName;
    private final String profileTable;
    private final int queryTimeoutSeconds;

    private DatabaseConfig(String url,
                           String user,
                           String password,
                           String driverClassName,
                           String profileTable,
                           int queryTimeoutSeconds) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.driverClassName = driverClassName;
        this.profileTable = profileTable;
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    public static DatabaseConfig load() {
        String url = firstNonBlank(systemProperty("db.url"), env("DB_URL"), DEFAULT_URL);
        String user = firstNonBlank(systemProperty("db.user"), env("DB_USER"), DEFAULT_USER);
        String password = firstNonBlank(systemProperty("db.password"), env("DB_PASSWORD"), DEFAULT_PASSWORD);
        String driver = firstNonBlank(systemProperty("db.driver"), env("DB_DRIVER"), DEFAULT_DRIVER);
        String profileTable = firstNonBlank(systemProperty("db.profile.table"), env("DB_PROFILE_TABLE"), DEFAULT_PROFILE_TABLE);
        String timeoutValue = firstNonBlank(systemProperty("db.query.timeout.seconds"), env("DB_QUERY_TIMEOUT_SECONDS"), "30");
        int timeout = parsePositiveInt(timeoutValue, 30);
        return new DatabaseConfig(url, user, password, driver, profileTable, timeout);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getProfileTable() {
        return profileTable;
    }

    public int getQueryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    private static String systemProperty(String name) {
        return System.getProperty(name);
    }

    private static String env(String name) {
        return System.getenv(name);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private static int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
