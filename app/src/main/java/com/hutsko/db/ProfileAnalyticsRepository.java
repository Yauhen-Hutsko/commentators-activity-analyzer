package com.hutsko.db;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated analytics over the enriched user profiles table.
 * <p>
 * The production table can contain several rows per {@code user_id}, so every query
 * de-duplicates users first (keeping the earliest registration date).
 */
public class ProfileAnalyticsRepository {
    private static final String ISO_DATE_PATTERN = "^[0-9]{4}-(0[1-9]|1[0-2])-[0-9]{2}$";

    private final PostgresConnector connector;
    private final String tableName;

    public ProfileAnalyticsRepository() {
        this(new PostgresConnector(), DatabaseConfig.load().getProfileTable());
    }

    public ProfileAnalyticsRepository(PostgresConnector connector, String tableName) {
        this.connector = connector;
        this.tableName = TableNames.validate(tableName);
    }

    /**
     * Monthly count of new user registrations, one entry per month from the first to the last
     * month with data (months without registrations are returned with zero).
     *
     * @param from inclusive lower bound, or {@code null} for no bound
     * @param to   inclusive upper bound, or {@code null} for no bound
     */
    public List<MonthlyCount> registrationsByMonth(YearMonth from, YearMonth to) {
        String sql = "select left(created_at_iso, 7) as month, count(*) as registrations"
                + " from (select distinct on (user_id) user_id, created_at_iso"
                + "       from " + tableName
                + "       where user_id is not null and created_at_iso ~ '" + ISO_DATE_PATTERN + "'"
                + "       order by user_id, created_at_iso) users"
                + " where left(created_at_iso, 7) between ? and ?"
                + " group by 1 order by 1";
        String lower = from == null ? "0000-00" : from.toString();
        String upper = to == null ? "9999-99" : to.toString();
        List<MonthlyCount> rows = connector.query(sql,
                rs -> new MonthlyCount(rs.getString("month"), rs.getLong("registrations")),
                lower, upper);
        return fillMissingMonths(rows);
    }

    /**
     * Inserts zero-count entries for months absent between the first and last entry.
     * Input must be sorted by month.
     */
    static List<MonthlyCount> fillMissingMonths(List<MonthlyCount> sorted) {
        if (sorted.isEmpty()) {
            return sorted;
        }
        Map<String, Long> byMonth = new LinkedHashMap<>();
        for (MonthlyCount row : sorted) {
            byMonth.put(row.getMonth(), row.getCount());
        }
        YearMonth current = YearMonth.parse(sorted.get(0).getMonth());
        YearMonth last = YearMonth.parse(sorted.get(sorted.size() - 1).getMonth());
        List<MonthlyCount> result = new ArrayList<>();
        while (!current.isAfter(last)) {
            String key = current.toString();
            result.add(new MonthlyCount(key, byMonth.getOrDefault(key, 0L)));
            current = current.plusMonths(1);
        }
        return result;
    }
}
