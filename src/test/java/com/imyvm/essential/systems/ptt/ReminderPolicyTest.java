package com.imyvm.essential.systems.ptt;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReminderPolicyTest {
    private static final ReminderPolicy POLICY = new ReminderPolicy(0.25D, 0.10D, 5,
        List.of(10, 5, 3, 2, 1));

    @Test
    void resolvesStageAtBoundaries() {
        assertEquals(ReminderStage.NORMAL, POLICY.stage(2_501L, 10_000L));
        assertEquals(ReminderStage.WARNING, POLICY.stage(2_500L, 10_000L));
        assertEquals(ReminderStage.WARNING, POLICY.stage(1_001L, 10_000L));
        assertEquals(ReminderStage.CRITICAL, POLICY.stage(1_000L, 10_000L));
        assertEquals(ReminderStage.EXPIRED, POLICY.stage(0L, 10_000L));
    }

    @Test
    void stageRejectsInvalidInitialLimit() {
        assertThrows(IllegalArgumentException.class, () -> POLICY.stage(1L, 0L));
    }

    @Test
    void reportsOnlyMostUrgentStageWhenTickCrossesSeveral() {
        assertEquals(ReminderStage.CRITICAL,
            POLICY.crossedStage(3_000L, 900L, 10_000L).orElseThrow());
        assertTrue(POLICY.crossedStage(900L, 800L, 10_000L).isEmpty());
    }

    @Test
    void normalizesChatMilestones() {
        ReminderPolicy policy = new ReminderPolicy(0.25D, 0.10D, 5,
            List.of(1, 5, -1, 3, 5, 0));

        assertEquals(List.of(5, 3, 1), policy.chatMilestones());
    }

    @Test
    void reportsOnlyMostUrgentCrossedChatMilestone() {
        assertEquals(3, POLICY.crossedChatMilestone(11_000L, 2_500L).orElseThrow());
        assertTrue(POLICY.crossedChatMilestone(2_500L, 2_100L).isEmpty());
    }

    @Test
    void reportsCountdownSoundOncePerCrossedSecond() {
        assertEquals(5, POLICY.crossedSoundSecond(5_200L, 4_900L, true).orElseThrow());
        assertEquals(4, POLICY.crossedSoundSecond(4_900L, 3_900L, true).orElseThrow());
        assertEquals(1, POLICY.crossedSoundSecond(3_900L, 900L, true).orElseThrow());
        assertTrue(POLICY.crossedSoundSecond(900L, 800L, true).isEmpty());
    }

    @Test
    void soundCanBeDisabled() {
        assertTrue(POLICY.crossedSoundSecond(5_200L, 4_900L, false).isEmpty());
    }

    @Test
    void rejectsInvalidRatiosAndCountdown() {
        assertThrows(IllegalArgumentException.class,
            () -> new ReminderPolicy(0.10D, 0.25D, 5, List.of()));
        assertThrows(IllegalArgumentException.class,
            () -> new ReminderPolicy(0.25D, 0.10D, -1, List.of()));
        assertThrows(IllegalArgumentException.class,
            () -> new ReminderPolicy(0.25D, 0.10D, 61, List.of()));
    }

    @Test
    void invalidConfigurationFallsBackAndLogsWarnings() {
        List<String> warnings = new ArrayList<>();

        ReminderPolicy policy = ReminderPolicy.withFallback(0.10D, 0.25D, -1, null, warnings::add);

        assertEquals(ReminderStage.WARNING, policy.stage(2_500L, 10_000L));
        assertEquals(5, policy.crossedSoundSecond(5_200L, 4_900L, true).orElseThrow());
        assertTrue(policy.chatMilestones().isEmpty());
        assertEquals(2, warnings.size());
    }
}
