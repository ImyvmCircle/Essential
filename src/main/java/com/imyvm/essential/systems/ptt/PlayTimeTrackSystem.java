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
    private static final PlayTimeTrackSystem INSTANCE = new PlayTimeTrackSystem();

    private PlayTimeTrackSystem() {
    }

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
                status = tr(statusTemplate, TimeUtil.formatDuration((int) (counter.getTimeRequired() - duration)));
            } else {
                status = tr(statusTemplate);
            }

            if (counter == TimeCounter.TOTAL)
                text.append(tr("commands.ptt.item.total", name, TimeUtil.formatDuration(duration)));
            else
                text.append(tr("commands.ptt.item", name, TimeUtil.formatDuration(duration), status));
        }
        return text;
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        Calendar calendar = Calendar.getInstance(TIME_ZONE);
        Arrays.stream(TimeCounter.values()).forEach(updater -> updater.updatePeriodId(calendar));

        server.getPlayerList().getPlayers().stream()
            .filter(player -> !((PlayerEntityMixinInterface) player).imyvm$isAwayFromKeyboard())
            .forEach(player -> {
                PlayerTrackData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID()).getPlayerTrackData();
                Arrays.stream(TimeCounter.values()).forEach(updater -> updater.apply(player, data, msSinceLastTick));
            });
    }
}