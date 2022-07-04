package com.imyvm.essential.tasks;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.EssentialStatistics;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import net.minecraft.server.MinecraftServer;

public class PlayTimeTrackTask implements LazyTicker.LazyTickable {
    private long lastTickTimestamp;

    @Override
    public void lazyTick(MinecraftServer server, long tick) {
        long currentTime = System.currentTimeMillis() / 1000;
        int duration = (int) (currentTime - this.lastTickTimestamp);
        this.lastTickTimestamp = currentTime;
        int requiredTime = EssentialMod.CONFIG.USER_GROUP_REQUIRED_PTT.getValue();

        server.getPlayerManager().getPlayerList().stream()
            .filter(player -> !((PlayerEntityMixinInterface) player).imyvm$isAwayFromKeyboard())
            .forEach(player -> player.increaseStat(EssentialStatistics.PLAY_TIME_TRACK, duration));
    }
}
