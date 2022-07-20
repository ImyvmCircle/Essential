package com.imyvm.essential.systems.fly;

import com.imyvm.hoki.nbt.NbtEnumValue;
import com.imyvm.hoki.nbt.NbtPersistent;
import com.imyvm.hoki.nbt.NbtPersistentValue;

public class SavedFlySession implements NbtPersistent {
    @NbtPersistentValue
    private final NbtEnumValue<PurchaseType> type = new NbtEnumValue<>(PurchaseType.class);

    @NbtPersistentValue
    private long timeLeft;

    @NbtPersistentValue
    private boolean isFlying;

    public SavedFlySession(PurchaseType type, long timeLeft, boolean isFlying) {
        this.type.set(type);
        this.timeLeft = timeLeft;
        this.isFlying = isFlying;
    }

    @SuppressWarnings("unused")
    public static SavedFlySession emptyForDeserialize() {
        return new SavedFlySession(PurchaseType.HOURLY, 0, false);
    }

    public PurchaseType getType() {
        return this.type.get();
    }

    public long getTimeLeft() {
        return this.timeLeft;
    }

    public boolean isFlying() {
        return this.isFlying;
    }
}
