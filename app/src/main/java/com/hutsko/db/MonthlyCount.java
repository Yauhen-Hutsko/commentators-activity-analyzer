package com.hutsko.db;

/**
 * Number of events (e.g. registrations) in a calendar month, month formatted as {@code YYYY-MM}.
 */
public class MonthlyCount {
    private final String month;
    private final long count;

    public MonthlyCount(String month, long count) {
        this.month = month;
        this.count = count;
    }

    public String getMonth() {
        return month;
    }

    public long getCount() {
        return count;
    }
}
