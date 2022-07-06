package com.imyvm.essential.data;

import com.imyvm.essential.systems.fly.PurchaseType;
import com.imyvm.essential.systems.fly.SavedFlySession;
import net.minecraft.nbt.NbtCompound;

public class PlayerData {
    private PurchaseType flyType;
    private long flyTimeLeft;
    private boolean isFlying;

    public void setSavedFlySession(SavedFlySession savedSession) {
        if (savedSession == null) {
            this.flyType = null;
            this.flyTimeLeft = 0;
        }
        else {
            this.flyType = savedSession.type();
            this.flyTimeLeft = savedSession.timeLeft();
            this.isFlying = savedSession.isFlying();
        }
    }

    public SavedFlySession resumeFlySession() {
        if (this.flyType == null)
            return null;

        SavedFlySession savedSession = new SavedFlySession(this.flyType, this.flyTimeLeft, this.isFlying);
        this.setSavedFlySession(null);

        return savedSession;
    }

    public void loadNbt(NbtCompound nbt) {
        if (nbt.contains("flyType")) {
            this.flyType = PurchaseType.valueOf(nbt.getString("flyType"));
            this.flyTimeLeft = nbt.getLong("flyTimeLeft");
            this.isFlying = nbt.getBoolean("isFlying");
        }
    }

    public void writeNbt(NbtCompound nbt) {
        if (this.flyType != null) {
            nbt.putString("flyType", this.flyType.toString());
            nbt.putLong("flyTimeLeft", this.flyTimeLeft);
            nbt.putBoolean("isFlying", this.isFlying);
        }
    }
}
