package com.imyvm.essential.systems.ptt;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

public final class ReminderPolicy {
    private static final long MILLIS_PER_SECOND = 1000L;
    private static final int MAX_SOUND_COUNTDOWN_SECONDS = 60;

    private final double warningRatio;
    private final double criticalRatio;
    private final int soundCountdownSeconds;
    private final List<Integer> chatMilestones;

    public ReminderPolicy(double warningRatio, double criticalRatio, int soundCountdownSeconds,
                          List<Integer> chatMilestones) {
        if (!(criticalRatio > 0.0D && criticalRatio < warningRatio && warningRatio < 1.0D))
            throw new IllegalArgumentException("Ratios must satisfy 0 < critical < warning < 1");
        if (soundCountdownSeconds < 0 || soundCountdownSeconds > MAX_SOUND_COUNTDOWN_SECONDS)
            throw new IllegalArgumentException("soundCountdownSeconds must be between 0 and 60");

        this.warningRatio = warningRatio;
        this.criticalRatio = criticalRatio;
        this.soundCountdownSeconds = soundCountdownSeconds;
        this.chatMilestones = chatMilestones.stream()
            .filter(value -> value != null && value > 0)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .toList();
    }

    public static ReminderPolicy withFallback(double warningRatio, double criticalRatio,
                                              int soundCountdownSeconds, List<Integer> chatMilestones,
                                              Consumer<String> warningLogger) {
        double safeWarningRatio = warningRatio;
        double safeCriticalRatio = criticalRatio;
        if (!(criticalRatio > 0.0D && criticalRatio < warningRatio && warningRatio < 1.0D)) {
            warningLogger.accept("Invalid reminder ratios; using defaults");
            safeWarningRatio = PttConfigDefaults.WARNING_RATIO;
            safeCriticalRatio = PttConfigDefaults.CRITICAL_RATIO;
        }

        int safeCountdownSeconds = soundCountdownSeconds;
        if (soundCountdownSeconds < 0 || soundCountdownSeconds > MAX_SOUND_COUNTDOWN_SECONDS) {
            warningLogger.accept("Invalid sound countdown; using default");
            safeCountdownSeconds = PttConfigDefaults.SOUND_COUNTDOWN_SECONDS;
        }

        List<Integer> safeMilestones = chatMilestones == null ? List.of() : chatMilestones;
        return new ReminderPolicy(safeWarningRatio, safeCriticalRatio, safeCountdownSeconds, safeMilestones);
    }

    public ReminderStage stage(long remainingMillis, long initialLimitMillis) {
        if (initialLimitMillis <= 0L)
            throw new IllegalArgumentException("initialLimitMillis must be positive");
        if (remainingMillis <= 0L)
            return ReminderStage.EXPIRED;

        double ratio = (double) remainingMillis / initialLimitMillis;
        if (ratio <= this.criticalRatio)
            return ReminderStage.CRITICAL;
        if (ratio <= this.warningRatio)
            return ReminderStage.WARNING;
        return ReminderStage.NORMAL;
    }

    public Optional<ReminderStage> crossedStage(long previousMillis, long currentMillis, long initialLimitMillis) {
        ReminderStage previousStage = this.stage(previousMillis, initialLimitMillis);
        ReminderStage currentStage = this.stage(currentMillis, initialLimitMillis);
        if (previousStage == currentStage)
            return Optional.empty();
        return Optional.of(currentStage);
    }

    public OptionalInt crossedChatMilestone(long previousMillis, long currentMillis) {
        return this.chatMilestones.stream()
            .mapToInt(Integer::intValue)
            .filter(seconds -> crossed(previousMillis, currentMillis, seconds))
            .min();
    }

    public OptionalInt crossedSoundSecond(long previousMillis, long currentMillis, boolean soundEnabled) {
        if (!soundEnabled || this.soundCountdownSeconds == 0)
            return OptionalInt.empty();

        for (int seconds = 1; seconds <= this.soundCountdownSeconds; seconds++) {
            if (crossed(previousMillis, currentMillis, seconds))
                return OptionalInt.of(seconds);
        }
        return OptionalInt.empty();
    }

    public List<Integer> chatMilestones() {
        return this.chatMilestones;
    }

    private static boolean crossed(long previousMillis, long currentMillis, int seconds) {
        long thresholdMillis = seconds * MILLIS_PER_SECOND;
        return previousMillis > thresholdMillis && currentMillis <= thresholdMillis;
    }
}
