package com.hutsko.db;

/**
 * Table names are concatenated into SQL, so they must be validated against a strict pattern.
 */
public final class TableNames {
    private TableNames() {
    }

    public static String validate(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (!trimmed.matches("[A-Za-z0-9_.]+")) {
            throw new IllegalArgumentException("Invalid PostgreSQL table name: " + value);
        }
        return trimmed;
    }
}
