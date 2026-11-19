package com.hutsko.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lightweight JDBC connector for PostgreSQL-backed reads.
 */
public class PostgresConnector {
    private final DatabaseConfig config;

    public PostgresConnector() {
        this(DatabaseConfig.load());
    }

    public PostgresConnector(DatabaseConfig config) {
        this.config = config;
        loadDriver(config.getDriverClassName());
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    public <T> List<T> query(String sql, SqlRowMapper<T> mapper, Object... params) {
        List<T> rows = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            statement.setQueryTimeout(config.getQueryTimeoutSeconds());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(mapper.map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to run PostgreSQL query", e);
        }
        return rows;
    }

    public <T> Optional<T> queryOne(String sql, SqlRowMapper<T> mapper, Object... params) {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<Map<String, Object>> queryForMapList(String sql, Object... params) {
        return query(sql, resultSet -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            return row;
        }, params);
    }

    private void bind(PreparedStatement statement, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    private void loadDriver(String driverClassName) {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load JDBC driver: " + driverClassName, e);
        }
    }
}
