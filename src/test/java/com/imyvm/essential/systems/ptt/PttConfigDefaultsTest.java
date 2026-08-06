package com.imyvm.essential.systems.ptt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PttConfigDefaultsTest {
    @Test
    void rewardTiersHaveIndependentDefaultAcquireLimits() {
        assertEquals(120_000L, PttConfigDefaults.DAY_ACQUIRE_TIME_LIMIT);
        assertEquals(300_000L, PttConfigDefaults.WEEK_ACQUIRE_TIME_LIMIT);
        assertEquals(900_000L, PttConfigDefaults.MONTH_ACQUIRE_TIME_LIMIT);
        assertEquals(1_800_000L, PttConfigDefaults.YEAR_ACQUIRE_TIME_LIMIT);
    }
}
