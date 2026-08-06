package com.imyvm.essential.systems.ptt;

import com.imyvm.hoki.nbt.NbtPersistent;
import com.imyvm.hoki.nbt.NbtPersistentValue;

public class PendingBonus implements NbtPersistent {
    @NbtPersistentValue private String token;
    @NbtPersistentValue private int bonus;
    @NbtPersistentValue private int periodId;
    @NbtPersistentValue private long initialLimitMillis;
    @NbtPersistentValue private long remainingMillis;

    public PendingBonus() {
        this("", 0, 0, 0L, 0L);
    }

    public PendingBonus(String token, int bonus, int periodId, long initialLimitMillis, long remainingMillis) {
        this.token = token;
        this.bonus = bonus;
        this.periodId = periodId;
        this.initialLimitMillis = initialLimitMillis;
        this.remainingMillis = remainingMillis;
    }

    @SuppressWarnings("unused")
    public static PendingBonus emptyForDeserialize() {
        return new PendingBonus();
    }

    public TickWindow elapse(long elapsedMillis) {
        if (elapsedMillis < 0L)
            throw new IllegalArgumentException("elapsedMillis cannot be negative");

        long previousMillis = this.remainingMillis;
        this.remainingMillis = Math.max(0L, previousMillis - elapsedMillis);
        return new TickWindow(previousMillis, this.remainingMillis);
    }

    public String getToken() {
        return this.token;
    }

    public int getBonus() {
        return this.bonus;
    }

    public int getPeriodId() {
        return this.periodId;
    }

    public long getInitialLimitMillis() {
        return this.initialLimitMillis;
    }

    public long getRemainingMillis() {
        return this.remainingMillis;
    }

    public boolean isValid() {
        return this.isStructurallyValid() && this.remainingMillis > 0L;
    }

    public boolean isStructurallyValid() {
        return this.token != null && !this.token.isBlank()
            && this.bonus > 0
            && this.initialLimitMillis > 0L
            && this.remainingMillis >= 0L
            && this.remainingMillis <= this.initialLimitMillis;
    }

    public record TickWindow(long previousMillis, long currentMillis) {
    }
}
