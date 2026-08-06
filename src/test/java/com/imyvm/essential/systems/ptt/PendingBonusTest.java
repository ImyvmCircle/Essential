package com.imyvm.essential.systems.ptt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingBonusTest {
    @Test
    void elapseClampsRemainingTimeAtZero() {
        PendingBonus bonus = new PendingBonus("token", 100, 7, 5_000L, 5_000L);

        PendingBonus.TickWindow window = bonus.elapse(6_000L);

        assertEquals(5_000L, window.previousMillis());
        assertEquals(0L, window.currentMillis());
        assertEquals(0L, bonus.getRemainingMillis());
    }

    @Test
    void elapseRejectsNegativeDuration() {
        PendingBonus bonus = new PendingBonus("token", 100, 7, 5_000L, 5_000L);

        assertThrows(IllegalArgumentException.class, () -> bonus.elapse(-1L));
    }

    @Test
    void gettersReturnCreationSnapshot() {
        PendingBonus bonus = new PendingBonus("token", 125, 9, 8_000L, 6_500L);

        assertEquals("token", bonus.getToken());
        assertEquals(125, bonus.getBonus());
        assertEquals(9, bonus.getPeriodId());
        assertEquals(8_000L, bonus.getInitialLimitMillis());
        assertEquals(6_500L, bonus.getRemainingMillis());
    }

    @Test
    void validatesStructureSeparatelyFromClaimability() {
        PendingBonus expired = new PendingBonus("token", 125, 9, 8_000L, 0L);
        PendingBonus corrupt = new PendingBonus("", 125, 9, 8_000L, 9_000L);

        assertTrue(expired.isStructurallyValid());
        assertFalse(expired.isValid());
        assertFalse(corrupt.isStructurallyValid());
    }
}
