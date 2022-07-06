package com.imyvm.essential.systems.ptt;

import com.imyvm.essential.EssentialStatistics;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.systems.BaseSystem;
import net.minecraft.server.MinecraftServer;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LAZY_TICKER;

public class PlayTimeTrackSystem extends BaseSystem implements LazyTicker.LazyTickable {
    private long lastTickTimestamp;

    @Override
    public void register() {
        LAZY_TICKER.register(this);
    }

    @Override
    public void lazyTick(MinecraftServer server, long tick) {
        long currentTime = System.currentTimeMillis() / 1000;
        int duration = (int) (currentTime - this.lastTickTimestamp);
        this.lastTickTimestamp = currentTime;
        int requiredTime = CONFIG.USER_GROUP_REQUIRED_PTT.getValue();

        server.getPlayerManager().getPlayerList().stream()
            .filter(player -> !((PlayerEntityMixinInterface) player).imyvm$isAwayFromKeyboard())
            .forEach(player -> player.increaseStat(EssentialStatistics.PLAY_TIME_TRACK, duration));
    }
}
