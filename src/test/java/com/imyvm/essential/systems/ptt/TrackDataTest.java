package com.imyvm.essential.systems.ptt;

import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackDataTest {
    @Test
    void periodRolloverKeepsPendingBonus() {
        TrackData data = new TrackData(100, 1, TrackData.Status.NOT_MEET);
        PendingBonus pending = bonus(1);
        data.replacePendingBonus(pending);

        data.update(50L, 2);

        assertSame(pending, data.getPendingBonus());
        assertEquals(50L, data.getDuration());
        assertEquals(2, data.getPeriodId());
        assertEquals(TrackData.Status.NOT_MEET, data.getStatus());
    }

    @Test
    void finishingOldPeriodBonusDoesNotOverwriteCurrentStatus() {
        TrackData data = new TrackData(100, 1, TrackData.Status.NOT_MEET);
        PendingBonus pending = bonus(1);
        data.replacePendingBonus(pending);
        data.update(50L, 2);

        assertSame(pending, data.finishPendingBonus(TrackData.Status.OBTAINED));
        assertNull(data.getPendingBonus());
        assertEquals(TrackData.Status.NOT_MEET, data.getStatus());
    }

    @Test
    void finishingCurrentPeriodBonusUpdatesStatus() {
        TrackData data = new TrackData(100, 1, TrackData.Status.NOT_MEET);
        PendingBonus pending = bonus(1);
        data.replacePendingBonus(pending);

        data.finishPendingBonus(TrackData.Status.EXPIRED);

        assertEquals(TrackData.Status.EXPIRED, data.getStatus());
    }

    @Test
    void replacingPendingBonusReturnsOldTicket() {
        TrackData data = new TrackData();
        PendingBonus oldBonus = bonus(0);
        PendingBonus newBonus = new PendingBonus("new", 200, 0, 10_000L, 10_000L);
        data.replacePendingBonus(oldBonus);

        assertSame(oldBonus, data.replacePendingBonus(newBonus));
        assertSame(newBonus, data.getPendingBonus());
    }

    @Test
    void repairsLegacyPendingStatusWithoutTicket() {
        TrackData data = new TrackData(100, 1, TrackData.Status.PENDING);

        assertTrue(data.repairLegacyPendingStatus());
        assertEquals(TrackData.Status.NOT_MEET, data.getStatus());
        assertFalse(data.repairLegacyPendingStatus());
    }

    @Test
    void pendingBonusSurvivesSerializationRoundTrip() {
        TrackData source = new TrackData(100, 3, TrackData.Status.NOT_MEET);
        source.replacePendingBonus(new PendingBonus("roundtrip", 450, 3, 20_000L, 12_345L));
        Tag serialized = source.serialize();

        TrackData restored = new TrackData();
        restored.deserialize(serialized);

        PendingBonus pending = restored.getPendingBonus();
        assertNotNull(pending);
        assertEquals("roundtrip", pending.getToken());
        assertEquals(450, pending.getBonus());
        assertEquals(3, pending.getPeriodId());
        assertEquals(20_000L, pending.getInitialLimitMillis());
        assertEquals(12_345L, pending.getRemainingMillis());
    }

    private static PendingBonus bonus(int periodId) {
        return new PendingBonus("old", 100, periodId, 10_000L, 10_000L);
    }
}
