package com.hutsko.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lightweight JDBC connector for PostgreSQL (one connection per call, no pooling).
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
            bind(connection, statement, params);
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

    /**
     * Executes a parameterized INSERT/UPDATE/DELETE and returns the number of affected rows.
     */
    public int update(String sql, Object... params) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(connection, statement, params);
            statement.setQueryTimeout(config.getQueryTimeoutSeconds());
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to run PostgreSQL update", e);
        }
    }

    /**
     * Executes the same statement for every parameter row in a single transaction.
     */
    public void batchUpdate(String sql, List<Object[]> rows) {
        if (rows.isEmpty()) {
            return;
        }
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Object[] row : rows) {
                    bind(connection, statement, row);
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to run PostgreSQL batch update", e);
        }
    }

    /**
     * Executes a script of one or more parameterless statements (e.g. DDL).
     */
    public void execute(String sql) {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute PostgreSQL statement", e);
        }
    }

    private void bind(Connection connection, PreparedStatement statement, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param instanceof String[]) {
                statement.setArray(i + 1, connection.createArrayOf("text", (String[]) param));
            } else {
                statement.setObject(i + 1, param);
            }
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
