package com.imyvm.essential.systems.ptt;

public final class PttConfigDefaults {
    public static final long DAY_ACQUIRE_TIME_LIMIT = 2 * 60 * 1000L;
    public static final long WEEK_ACQUIRE_TIME_LIMIT = 5 * 60 * 1000L;
    public static final long MONTH_ACQUIRE_TIME_LIMIT = 15 * 60 * 1000L;
    public static final long YEAR_ACQUIRE_TIME_LIMIT = 30 * 60 * 1000L;
    public static final double WARNING_RATIO = 0.25D;
    public static final double CRITICAL_RATIO = 0.10D;
    public static final int SOUND_COUNTDOWN_SECONDS = 5;

    private PttConfigDefaults() {
    }
}
