package com.imyvm.essential.systems.fly;

public class SavedFlySession {
    private final PurchaseType type;
    private final long timeLeft;
    private final boolean isFlying;

    public SavedFlySession(PurchaseType type, long timeLeft, boolean isFlying) {
        this.type = type;
        this.timeLeft = timeLeft;
        this.isFlying = isFlying;
    }

    public PurchaseType getType() {
        return this.type;
    }

    public long getTimeLeft() {
        return this.timeLeft;
    }

    public boolean isFlying() {
        return this.isFlying;
    }
}
