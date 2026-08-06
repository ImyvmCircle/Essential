package com.imyvm.essential.systems.ptt;

import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.systems.BaseSystem;
import com.imyvm.essential.util.TimeUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import static com.imyvm.essential.EssentialMod.LAZY_TICKER;
import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;
import static com.imyvm.essential.Translator.tr;

public class PlayTimeTrackSystem extends BaseSystem implements LazyTicker.LazyTickable {
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");
    private static final long MILLIS_PER_TICK = 50L; // 20 TPS，与 vanilla playOneMinute 换算一致
    private static final PlayTimeTrackSystem INSTANCE = new PlayTimeTrackSystem();

    private PlayTimeTrackSystem() {
    }

    // 上次计时的 tick；tick 只在服务器真正运行时递增，暂停/退出存档期间不计时
    private long lastTick = 0;

    public static PlayTimeTrackSystem getInstance() {
        return INSTANCE;
    }

    @Override
    public void register() {
        LAZY_TICKER.register(this);
        BonusSupplier.getInstance().register();
    }

    public Component getPlayTimeText(PlayerTrackData trackData) {
        MutableComponent text = (MutableComponent) tr("commands.ptt.header");
        for (TimeCounter counter : TimeCounter.values()) {
            text.append("\n");

            TrackData data = counter.getGetDataFunction().apply(trackData);
            Component name = tr("name.ptt.category." + counter.getTypeId());
            int duration = (int) (data.getDuration() / 1000);
            String statusTemplate = "name.ptt.status." + data.getStatus().toString().toLowerCase();
            Component status;
            if (data.getStatus() == TrackData.Status.NOT_MEET) {
                int remaining = (int) Math.max(0L, counter.getTimeRequired() - duration);
                status = tr(statusTemplate, TimeUtil.formatDuration(remaining));
            } else {
                status = tr(statusTemplate);
            }

            if (counter == TimeCounter.TOTAL)
                text.append(tr("commands.ptt.item.total", name, TimeUtil.formatDuration(duration)));
            else
                text.append(tr("commands.ptt.item", name, TimeUtil.formatDuration(duration), status));

            PendingBonus pendingBonus = data.getPendingBonus();
            if (pendingBonus != null) {
                int remaining = (int) Math.min(Integer.MAX_VALUE,
                    (pendingBonus.getRemainingMillis() + 999L) / 1000L);
                text.append("\n").append(tr("commands.ptt.pending_bonus", name,
                    TimeUtil.formatDuration(remaining)));
            }
        }
        return text;
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        long lastTick = this.lastTick;
        this.lastTick = tickCounts;
        long elapsed = lastTick == 0L ? 0L : (tickCounts - lastTick) * MILLIS_PER_TICK;

        Calendar calendar = Calendar.getInstance(TIME_ZONE);
        Arrays.stream(TimeCounter.values()).forEach(updater -> updater.updatePeriodId(calendar));

        server.getPlayerList().getPlayers().stream()
            .filter(player -> !((PlayerEntityMixinInterface) player).imyvm$isAwayFromKeyboard())
            .forEach(player -> {
                PlayerTrackData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID()).getPlayerTrackData();
                Arrays.stream(TimeCounter.values()).forEach(updater -> updater.apply(player, data, elapsed));
            });
    }
}
