package com.imyvm.essential.systems.ptt;

import com.imyvm.essential.EssentialStatistics;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.systems.BaseSystem;
import net.minecraft.server.MinecraftServer;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LAZY_TICKER;

public class PlayTimeTrackSystem extends BaseSystem implements LazyTicker.LazyTickable {
    private long reminder;

    @Override
    public void register() {
        LAZY_TICKER.register(this);
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        this.reminder += msSinceLastTick;
        int duration = (int) (this.reminder / 1000);
        this.reminder -= duration * 1000L;
        int requiredTime = CONFIG.USER_GROUP_REQUIRED_PTT.getValue();

        server.getPlayerManager().getPlayerList().stream()
            .filter(player -> !((PlayerEntityMixinInterface) player).imyvm$isAwayFromKeyboard())
            .forEach(player -> player.increaseStat(EssentialStatistics.PLAY_TIME_TRACK, duration));
    }
}
