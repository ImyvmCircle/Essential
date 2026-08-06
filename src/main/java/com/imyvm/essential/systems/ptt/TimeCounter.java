package com.imyvm.essential.systems.ptt;

import com.imyvm.essential.util.RandomUtil;
import com.imyvm.hoki.config.Option;
import com.typesafe.config.Config;
import net.minecraft.server.level.ServerPlayer;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LOGGER;
import static com.imyvm.essential.systems.ptt.PttConfigDefaults.DAY_ACQUIRE_TIME_LIMIT;
import static com.imyvm.essential.systems.ptt.PttConfigDefaults.MONTH_ACQUIRE_TIME_LIMIT;
import static com.imyvm.essential.systems.ptt.PttConfigDefaults.WEEK_ACQUIRE_TIME_LIMIT;
import static com.imyvm.essential.systems.ptt.PttConfigDefaults.YEAR_ACQUIRE_TIME_LIMIT;

public enum TimeCounter {
    CONTINUOUS("continuous", PlayerTrackData::getContinuous, TimeCounter::getContinuous,
        CONFIG.PTT_CONTINUOUS_REQUIRED, CONFIG.PTT_CONTINUOUS_BONUS, false, null, null, 0L),
    DAY("day", PlayerTrackData::getDay, TimeCounter::getDayId,
        CONFIG.PTT_DAY_REQUIRED, CONFIG.PTT_DAY_BONUS, true, CONFIG.PTT_DAY_ACQUIRE_TIME_LIMIT,
        CONFIG.PTT_DAY_CHAT_COUNTDOWN, DAY_ACQUIRE_TIME_LIMIT),
    WEEK("week", PlayerTrackData::getWeek, TimeCounter::getWeekId,
        CONFIG.PTT_WEEK_REQUIRED, CONFIG.PTT_WEEK_BONUS, true, CONFIG.PTT_WEEK_ACQUIRE_TIME_LIMIT,
        CONFIG.PTT_WEEK_CHAT_COUNTDOWN, WEEK_ACQUIRE_TIME_LIMIT),
    MONTH("month", PlayerTrackData::getMonth, TimeCounter::getMonthId,
        CONFIG.PTT_MONTH_REQUIRED, CONFIG.PTT_MONTH_BONUS, true, CONFIG.PTT_MONTH_ACQUIRE_TIME_LIMIT,
        CONFIG.PTT_MONTH_CHAT_COUNTDOWN, MONTH_ACQUIRE_TIME_LIMIT),
    YEAR("year", PlayerTrackData::getYear, TimeCounter::getYearId,
        CONFIG.PTT_YEAR_REQUIRED, CONFIG.PTT_YEAR_BONUS, true, CONFIG.PTT_YEAR_ACQUIRE_TIME_LIMIT,
        CONFIG.PTT_YEAR_CHAT_COUNTDOWN, YEAR_ACQUIRE_TIME_LIMIT),
    TOTAL("total", PlayerTrackData::getTotal, (calendar) -> 1,
        new Option<>("", Long.MAX_VALUE, Config::getLong), new Option<>("", 0, Config::getInt),
        false, null, null, 0L);

    private static final List<TimeCounter> ACQUIRABLE_VALUES = List.of(YEAR, MONTH, WEEK, DAY);

    private final String typeId;
    private final Function<PlayerTrackData, TrackData> getDataFunction;
    private final Function<Calendar, Integer> periodIdCalculator;
    private final Option<Long> timeRequiredOption;
    private final Option<Integer> bonusOption;
    private final boolean shouldAcquire;
    private final Option<Long> acquireTimeLimitOption;
    private final Option<List<Integer>> chatCountdownOption;
    private final long defaultAcquireTimeLimit;
    private int periodId;

    TimeCounter(String typeId, Function<PlayerTrackData, TrackData> getDataFunction,
                Function<Calendar, Integer> periodIdCalculator, Option<Long> timeRequiredOption,
                Option<Integer> bonusOption, boolean shouldAcquire, Option<Long> acquireTimeLimitOption,
                Option<List<Integer>> chatCountdownOption, long defaultAcquireTimeLimit) {
        this.typeId = typeId;
        this.getDataFunction = getDataFunction;
        this.periodIdCalculator = periodIdCalculator;
        this.timeRequiredOption = timeRequiredOption;
        this.bonusOption = bonusOption;
        this.shouldAcquire = shouldAcquire;
        this.acquireTimeLimitOption = acquireTimeLimitOption;
        this.chatCountdownOption = chatCountdownOption;
        this.defaultAcquireTimeLimit = defaultAcquireTimeLimit;
    }

    void updatePeriodId(Calendar calendar) {
        this.periodId = this.periodIdCalculator.apply(calendar);
    }

    void apply(ServerPlayer player, PlayerTrackData playerTrackData, long msSinceLastTick) {
        TrackData data = this.getDataFunction.apply(playerTrackData);
        if (data.repairLegacyPendingStatus())
            LOGGER.warn("Repaired legacy PENDING state without ticket for player {} and bonus {}",
                player.getUUID(), this.typeId);
        data.update(msSinceLastTick, this.periodId);
        if (data.getStatus() == TrackData.Status.NOT_MEET && data.getDuration() > this.timeRequiredOption.getValue()) {
            int bonus = this.bonusOption.getValue();
            if (bonus <= 0) {
                if (bonus < 0)
                    LOGGER.warn("Ignoring invalid negative PTT bonus {} for {}", bonus, this.typeId);
                return;
            }

            if (this.shouldAcquire) {
                String token = RandomUtil.getRandomString(5);
                long acquireTimeLimit = this.getAcquireTimeLimit();
                PendingBonus pendingBonus = new PendingBonus(token, bonus, data.getPeriodId(),
                    acquireTimeLimit, acquireTimeLimit);
                PendingBonus replaced = data.replacePendingBonus(pendingBonus);
                BonusSupplier.getInstance().onBonusCreated(player, this, data, replaced);
            } else {
                data.setStatus(TrackData.Status.OBTAINED);
                BonusSupplier.getInstance().quickTransfer(player, bonus, this.typeId);
            }
        }
    }

    public String getTypeId() {
        return this.typeId;
    }

    public Long getTimeRequired() {
        return this.timeRequiredOption.getValue() / 1000L;
    }

    public Function<PlayerTrackData, TrackData> getGetDataFunction() {
        return this.getDataFunction;
    }

    public TrackData getData(PlayerTrackData playerTrackData) {
        return this.getDataFunction.apply(playerTrackData);
    }

    public static List<TimeCounter> getAcquirableValues() {
        return ACQUIRABLE_VALUES;
    }

    public static Optional<TimeCounter> findAcquirable(String typeId) {
        return ACQUIRABLE_VALUES.stream()
            .filter(counter -> counter.typeId.equals(typeId))
            .findFirst();
    }

    public long getAcquireTimeLimit() {
        if (this.acquireTimeLimitOption == null)
            return 0L;

        long configured = this.acquireTimeLimitOption.getValue();
        if (configured > 0L)
            return configured;

        LOGGER.warn("Invalid acquire time limit {} for PTT bonus {}; using default {}",
            configured, this.typeId, this.defaultAcquireTimeLimit);
        return this.defaultAcquireTimeLimit;
    }

    public List<Integer> getChatCountdown() {
        if (this.chatCountdownOption == null)
            return List.of();
        return this.chatCountdownOption.getValue();
    }

    long getDefaultAcquireTimeLimit() {
        return this.defaultAcquireTimeLimit;
    }

    private static int getContinuous(Calendar calendar) {
        return calendar.get(Calendar.MINUTE) / 10;
    }

    private static int getDayId(Calendar calendar) {
        return calendar.get(Calendar.YEAR) * 366 + calendar.get(Calendar.DAY_OF_YEAR);
    }

    private static int getWeekId(Calendar calendar) {
        Calendar calendar1 = (Calendar) calendar.clone();
        calendar1.set(Calendar.DAY_OF_WEEK, calendar1.getFirstDayOfWeek());
        return getDayId(calendar1);
    }

    private static int getMonthId(Calendar calendar) {
        return calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
    }

    private static int getYearId(Calendar calendar) {
        return calendar.get(Calendar.YEAR);
    }
}
