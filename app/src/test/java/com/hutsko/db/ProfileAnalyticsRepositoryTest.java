package com.hutsko.db;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProfileAnalyticsRepositoryTest {

    @Test
    public void fillMissingMonths_inserts_zero_entries_across_year_boundary() {
        List<MonthlyCount> input = Arrays.asList(
                new MonthlyCount("2020-11", 5),
                new MonthlyCount("2021-02", 7));

        List<MonthlyCount> result = ProfileAnalyticsRepository.fillMissingMonths(input);

        Assert.assertEquals(4, result.size());
        Assert.assertEquals("2020-11", result.get(0).getMonth());
        Assert.assertEquals(5, result.get(0).getCount());
        Assert.assertEquals("2020-12", result.get(1).getMonth());
        Assert.assertEquals(0, result.get(1).getCount());
        Assert.assertEquals("2021-01", result.get(2).getMonth());
        Assert.assertEquals(0, result.get(2).getCount());
        Assert.assertEquals("2021-02", result.get(3).getMonth());
        Assert.assertEquals(7, result.get(3).getCount());
    }

    @Test
    public void fillMissingMonths_returns_empty_for_empty_input() {
        Assert.assertTrue(ProfileAnalyticsRepository.fillMissingMonths(Collections.<MonthlyCount>emptyList()).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejects_unsafe_table_name() {
        new ProfileAnalyticsRepository(null, "users; drop table x");
    }
}
