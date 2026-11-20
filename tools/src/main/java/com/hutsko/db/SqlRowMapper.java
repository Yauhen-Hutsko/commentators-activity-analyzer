package com.hutsko.db;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlRowMapper<T> {
    T map(ResultSet resultSet) throws SQLException;
}
