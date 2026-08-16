package com.imyvm.essential.systems.fly;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlySessionTest {
    @Test
    void firstTickDoesNotConsumeTimeFromBeforeSessionCreation() {
        long timeLeft = FlySession.elapsedTimeLeft(5_000L, 60_000L, true);

        assertEquals(5_000L, timeLeft);
    }

    @Test
    void laterTicksConsumeElapsedTime() {
        long timeLeft = FlySession.elapsedTimeLeft(5_000L, 1_000L, false);

        assertEquals(4_000L, timeLeft);
    }
}
