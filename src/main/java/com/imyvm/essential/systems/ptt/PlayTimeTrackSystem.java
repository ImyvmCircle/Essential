package com.imyvm.essential.systems.ptt;

import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.systems.BaseSystem;
import com.imyvm.essential.util.TimeUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import static com.imyvm.essential.EssentialMod.*;
import static com.imyvm.essential.Translator.tr;

public class PlayTimeTrackSystem extends BaseSystem implements LazyTicker.LazyTickable {
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");
    private static final PlayTimeTrackSystem INSTANCE = new PlayTimeTrackSystem();

    private PlayTimeTrackSystem() {
    }
    public static final class DurationConstants {
        public static final int DAY = (int) (CONFIG.PTT_DAY_REQUIRED.getValue() / 1000);
        public static final int WEEK = (int) (CONFIG.PTT_WEEK_REQUIRED.getValue() / 1000);
        public static final int MONTH = (int) (CONFIG.PTT_MONTH_REQUIRED.getValue() / 1000);
        public static final int YEAR = (int) (CONFIG.PTT_YEAR_REQUIRED.getValue() / 1000);
    }

    public static PlayTimeTrackSystem getInstance() {
        return INSTANCE;
    }

    @Override
    public void register() {
        LAZY_TICKER.register(this);
        BonusSupplier.getInstance().register();
    }

    public Text getPlayTimeText(PlayerTrackData trackData) {
        MutableText text = (MutableText) tr("commands.ptt.header");
        for (TimeCounter counter : TimeCounter.values()) {
            text.append("\n");

            TrackData data = counter.getGetDataFunction().apply(trackData);
            Text name = tr("name.ptt.category." + counter.getTypeId());
            int duration = (int) (data.getDuration() / 1000);
            String statusStr = "name.ptt.status." + data.getStatus().toString().toLowerCase();
            Text status;
            if (statusStr.equals("name.ptt.status.not_meet")){
                switch (counter.getTypeId()) {
                    case "day" -> status = tr(statusStr, TimeUtil.formatDuration(DurationConstants.DAY - duration));
                    case "week" -> status = tr(statusStr, TimeUtil.formatDuration(DurationConstants.WEEK - duration));
                    case "month" -> status = tr(statusStr, TimeUtil.formatDuration(DurationConstants.MONTH - duration));
                    case "year" -> status = tr(statusStr, TimeUtil.formatDuration(DurationConstants.YEAR - duration));
                    default -> status = tr("name.ptt.status.continuous");
                }
            } else {
                status = tr(statusStr);
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

        server.getPlayerManager().getPlayerList().stream()
            .filter(player -> !((PlayerEntityMixinInterface) player).imyvm$isAwayFromKeyboard())
            .forEach(player -> {
                PlayerTrackData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUuid()).getPlayerTrackData();
                Arrays.stream(TimeCounter.values()).forEach(updater -> updater.apply(player, data, msSinceLastTick));
            });
    }
}
