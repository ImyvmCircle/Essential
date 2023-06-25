package com.imyvm.essential.systems.ptt;

import com.imyvm.essential.util.RandomUtil;
import com.imyvm.hoki.config.Option;
import com.typesafe.config.Config;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Calendar;
import java.util.function.Function;

import static com.imyvm.essential.EssentialMod.CONFIG;

public enum TimeCounter {
    CONTINUOUS("continuous", PlayerTrackData::getContinuous, TimeCounter::getContinuous,
        CONFIG.PTT_CONTINUOUS_REQUIRED, CONFIG.PTT_CONTINUOUS_BONUS, false),
    DAY("day", PlayerTrackData::getDay, TimeCounter::getDayId,
        CONFIG.PTT_DAY_REQUIRED, CONFIG.PTT_DAY_BONUS, true),
    WEEK("week", PlayerTrackData::getWeek, TimeCounter::getWeekId,
        CONFIG.PTT_WEEK_REQUIRED, CONFIG.PTT_WEEK_BONUS, true),
    MONTH("month", PlayerTrackData::getMonth, TimeCounter::getMonthId,
        CONFIG.PTT_MONTH_REQUIRED, CONFIG.PTT_MONTH_BONUS, true),
    YEAR("year", PlayerTrackData::getYear, TimeCounter::getYearId,
        CONFIG.PTT_YEAR_REQUIRED, CONFIG.PTT_YEAR_BONUS, true),
    TOTAL("total", PlayerTrackData::getTotal, (calendar) -> 1,
        new Option<>("", Long.MAX_VALUE, Config::getLong), new Option<>("", 0, Config::getInt), false);

    private final String typeId;
    private final Function<PlayerTrackData, TrackData> getDataFunction;
    private final Function<Calendar, Integer> periodIdCalculator;
    private final Option<Long> timeRequiredOption;
    private final Option<Integer> bonusOption;
    private final boolean shouldAcquire;
    private int periodId;

    TimeCounter(String typeId, Function<PlayerTrackData, TrackData> getDataFunction,
                Function<Calendar, Integer> periodIdCalculator, Option<Long> timeRequiredOption,
                Option<Integer> bonusOption, boolean shouldAcquire) {
        this.typeId = typeId;
        this.getDataFunction = getDataFunction;
        this.periodIdCalculator = periodIdCalculator;
        this.timeRequiredOption = timeRequiredOption;
        this.bonusOption = bonusOption;
        this.shouldAcquire = shouldAcquire;
    }

    void updatePeriodId(Calendar calendar) {
        this.periodId = this.periodIdCalculator.apply(calendar);
    }

    void apply(ServerPlayerEntity player, PlayerTrackData playerTrackData, long msSinceLastTick) {
        TrackData data = this.getDataFunction.apply(playerTrackData);
        data.update(msSinceLastTick, this.periodId);
        if (data.getStatus() == TrackData.Status.NOT_MEET && data.getDuration() > this.timeRequiredOption.getValue()) {
            int bonus = this.bonusOption.getValue();
            if (bonus == 0)
                return;

            if (this.shouldAcquire) {
                data.setStatus(TrackData.Status.PENDING);
                String token = RandomUtil.getRandomString(5);
                long expiredAt = System.currentTimeMillis() + CONFIG.PTT_ACQUIRE_TIME_LIMIT.getValue();
                BonusSupplier.getInstance().addTicket(
                    new BonusSupplier.Ticket(player, data, this.typeId, token, bonus, expiredAt));
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
        return this.timeRequiredOption.getValue();
    }

    public Function<PlayerTrackData, TrackData> getGetDataFunction() {
        return this.getDataFunction;
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
