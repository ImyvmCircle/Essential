package com.imyvm.essential.data;

import com.imyvm.essential.systems.fly.SavedFlySession;
import com.imyvm.essential.systems.ptt.PlayerTrackData;
import com.imyvm.hoki.nbt.NbtPersistent;
import com.imyvm.hoki.nbt.NbtPersistentValue;

public class PlayerData implements NbtPersistent {
    @NbtPersistentValue
    private SavedFlySession savedFlySession;

    @NbtPersistentValue
    private PlayerTrackData playerTrackData = new PlayerTrackData();

    @NbtPersistentValue private int deathProtectLevel = 0;
    @NbtPersistentValue private boolean keepInventoryAtRespawn = false;

    public void setSavedFlySession(SavedFlySession savedFlySession) {
        this.savedFlySession = savedFlySession;
    }

    public SavedFlySession resumeSavedFlySession() {
        SavedFlySession value = this.savedFlySession;
        this.savedFlySession = null;
        return value;
    }

    public PlayerTrackData getPlayerTrackData() {
        return this.playerTrackData;
    }

    public int getDeathProtectLevel() {
        return this.deathProtectLevel;
    }

    public void setDeathProtectLevel(int deathProtectLevel) {
        this.deathProtectLevel = deathProtectLevel;
    }

    public boolean shouldKeepInventoryAtRespawn() {
        return this.keepInventoryAtRespawn;
    }

    public void setKeepInventoryAtRespawn(boolean keepInventoryAtRespawn) {
        this.keepInventoryAtRespawn = keepInventoryAtRespawn;
    }
}
