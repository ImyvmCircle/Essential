package com.imyvm.essential.systems.ptt;

import com.imyvm.economy.api.DatabaseApi;
import com.imyvm.economy.util.MoneyUtil;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.util.TimeUtil;
import com.imyvm.hoki.util.CommandUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LAZY_TICKER;
import static com.imyvm.essential.EssentialMod.LOGGER;
import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;
import static com.imyvm.essential.Translator.tr;

public class BonusSupplier implements LazyTicker.LazyTickable {
    private static final String BONUS_ACQUIRE_COMMAND_FMT = "/bonus %s %s";
    private static final SimpleCommandExceptionType NO_SUCH_TICKET_EXCEPTION =
        new SimpleCommandExceptionType(tr("commands.bonus.failed.no_such_ticket"));
    private static final SimpleCommandExceptionType TOKEN_NOT_MATCHED_EXCEPTION =
        new SimpleCommandExceptionType(tr("commands.bonus.failed.token_not_matched"));
    private static final SimpleCommandExceptionType ECONOMY_FAILED_EXCEPTION =
        new SimpleCommandExceptionType(tr("commands.bonus.failed.economy"));
    private static final BonusSupplier INSTANCE = new BonusSupplier();

    private final Map<TicketKey, BonusReminderController> controllers = new HashMap<>();
    private final Map<TimeCounter, CachedPolicy> policyCache = new EnumMap<>(TimeCounter.class);

    private BonusSupplier() {
    }

    public static BonusSupplier getInstance() {
        return INSTANCE;
    }

    public void register() {
        LAZY_TICKER.register(this);
        ServerPlayConnectionEvents.JOIN.register(
            (handler, sender, server) -> this.onPlayerJoined(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> this.onPlayerDisconnected(handler.getPlayer()));
    }

    public void onBonusCreated(ServerPlayer player, TimeCounter counter, TrackData data,
                               PendingBonus replaced) {
        if (replaced != null) {
            this.removeController(new TicketKey(player.getUUID(), counter));
            player.sendSystemMessage(tr("message.ptt.bonus.replaced",
                tr("name.bonus." + counter.getTypeId())));
            LOGGER.warn("Replaced pending PTT bonus {} for player {}", counter.getTypeId(), player.getUUID());
        }

        this.rebuildPlayerControllers(player, false);
        PendingBonus pendingBonus = data.getPendingBonus();
        player.sendSystemMessage(tr("message.ptt.bonus.to_receive",
            tr("name.bonus." + counter.getTypeId()), createAcquireCommand(counter, pendingBonus)));

        BonusReminderController controller = this.controllers.get(new TicketKey(player.getUUID(), counter));
        if (controller != null && CONFIG.PTT_REMINDER_SOUND_ENABLED.getValue())
            controller.playCreatedSound();
    }

    public void onTicketAcquire(ServerPlayer player, String typeId, String token) throws CommandSyntaxException {
        TimeCounter counter = TimeCounter.findAcquirable(typeId)
            .orElseThrow(NO_SUCH_TICKET_EXCEPTION::create);
        PlayerTrackData playerTrackData = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID()).getPlayerTrackData();
        TrackData data = counter.getData(playerTrackData);
        PendingBonus pendingBonus = data.getPendingBonus();
        if (pendingBonus == null || !pendingBonus.isValid())
            throw NO_SUCH_TICKET_EXCEPTION.create();
        if (!pendingBonus.getToken().equals(token))
            throw TOKEN_NOT_MATCHED_EXCEPTION.create();

        try {
            DatabaseApi.getInstance().getPlayer(player).addMoney(pendingBonus.getBonus());
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to transfer PTT bonus {} to player {}",
                counter.getTypeId(), player.getUUID(), exception);
            throw ECONOMY_FAILED_EXCEPTION.create();
        }

        data.finishPendingBonus(TrackData.Status.OBTAINED);
        this.removeController(new TicketKey(player.getUUID(), counter));
        player.sendSystemMessage(tr("commands.bonus.success",
            MoneyUtil.format(pendingBonus.getBonus()), tr("name.bonus." + typeId)));
    }

    public void quickTransfer(ServerPlayer player, int bonus, String typeId) {
        DatabaseApi.getInstance().getPlayer(player).addMoney(bonus);
        Component name = tr("name.bonus." + typeId);
        player.sendSystemMessage(tr("commands.bonus.success", MoneyUtil.format(bonus), name));
    }

    public void onPlayerJoined(ServerPlayer player) {
        this.rebuildPlayerControllers(player, true);
    }

    public void onPlayerDisconnected(ServerPlayer player) {
        UUID playerId = player.getUUID();
        List<TicketKey> keys = this.controllers.keySet().stream()
            .filter(key -> key.playerId().equals(playerId))
            .toList();
        keys.forEach(this::removeController);
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        long elapsedMillis = Math.max(0L, msSinceLastTick);
        if (msSinceLastTick < 0L)
            LOGGER.warn("Ignoring negative PTT bonus tick duration {}", msSinceLastTick);

        for (BonusReminderController controller : new ArrayList<>(this.controllers.values())) {
            TicketKey key = new TicketKey(controller.getPlayer().getUUID(), controller.getCounter());
            try {
                ReminderPolicy policy = this.getPolicy(controller.getCounter());
                BonusReminderController.TickResult result = controller.tick(elapsedMillis, policy,
                    CONFIG.PTT_REMINDER_SOUND_ENABLED.getValue());
                if (result == BonusReminderController.TickResult.ACTIVE)
                    continue;

                if (result == BonusReminderController.TickResult.EXPIRED) {
                    controller.getData().finishPendingBonus(TrackData.Status.EXPIRED);
                    controller.getPlayer().sendSystemMessage(tr("message.ptt.bonus.expired",
                        tr("name.bonus." + controller.getCounter().getTypeId())));
                    if (CONFIG.PTT_REMINDER_SOUND_ENABLED.getValue())
                        controller.playExpiredSound();
                }
                this.removeController(key);
            } catch (RuntimeException exception) {
                LOGGER.error("Failed to update PTT bonus {} for player {}",
                    controller.getCounter().getTypeId(), controller.getPlayer().getUUID(), exception);
            }
        }
    }

    static Component createAcquireCommand(TimeCounter counter, PendingBonus pendingBonus) {
        return CommandUtil.getSuggestCommandText(String.format(BONUS_ACQUIRE_COMMAND_FMT,
            counter.getTypeId(), pendingBonus.getToken()));
    }

    private void rebuildPlayerControllers(ServerPlayer player, boolean sendSummary) {
        this.onPlayerDisconnected(player);
        PlayerTrackData playerTrackData = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID()).getPlayerTrackData();
        List<BonusReminderController> restored = new ArrayList<>();

        for (TimeCounter counter : TimeCounter.getAcquirableValues()) {
            TrackData data = counter.getData(playerTrackData);
            if (data.repairLegacyPendingStatus())
                LOGGER.warn("Repaired legacy PENDING state without ticket for player {} and bonus {}",
                    player.getUUID(), counter.getTypeId());

            PendingBonus pendingBonus = data.getPendingBonus();
            if (pendingBonus == null)
                continue;
            if (!pendingBonus.isStructurallyValid()) {
                data.clearInvalidPendingBonus();
                LOGGER.warn("Cleared invalid pending PTT bonus {} for player {}",
                    counter.getTypeId(), player.getUUID());
                continue;
            }
            if (pendingBonus.getRemainingMillis() <= 0L) {
                data.finishPendingBonus(TrackData.Status.EXPIRED);
                player.sendSystemMessage(tr("message.ptt.bonus.expired",
                    tr("name.bonus." + counter.getTypeId())));
                continue;
            }

            BonusReminderController controller = new BonusReminderController(player, counter, data,
                pendingBonus, this.getPolicy(counter));
            this.controllers.put(new TicketKey(player.getUUID(), counter), controller);
            restored.add(controller);
        }

        if (sendSummary && !restored.isEmpty())
            this.sendRestoreSummary(player, restored);
    }

    private void sendRestoreSummary(ServerPlayer player, List<BonusReminderController> restored) {
        MutableComponent summary = tr("message.ptt.bonus.resume.header").copy();
        boolean hasCritical = false;
        for (BonusReminderController controller : restored) {
            PendingBonus pendingBonus = controller.getPendingBonus();
            summary.append("\n").append(tr("message.ptt.bonus.resume.item",
                tr("name.bonus." + controller.getCounter().getTypeId()),
                TimeUtil.formatDuration((int) Math.min(Integer.MAX_VALUE,
                    (pendingBonus.getRemainingMillis() + 999L) / 1000L)),
                createAcquireCommand(controller.getCounter(), pendingBonus)));
            hasCritical |= controller.isCritical(this.getPolicy(controller.getCounter()));
        }
        player.sendSystemMessage(summary);

        if (hasCritical && CONFIG.PTT_REMINDER_SOUND_ENABLED.getValue())
            restored.getFirst().playCriticalResumeSound();
    }

    private ReminderPolicy getPolicy(TimeCounter counter) {
        List<Integer> configuredMilestones = counter.getChatCountdown();
        PolicyConfig config = new PolicyConfig(
            CONFIG.PTT_REMINDER_WARNING_RATIO.getValue(),
            CONFIG.PTT_REMINDER_CRITICAL_RATIO.getValue(),
            CONFIG.PTT_REMINDER_SOUND_COUNTDOWN_SECONDS.getValue(),
            configuredMilestones == null ? List.of() : List.copyOf(configuredMilestones));
        CachedPolicy cached = this.policyCache.get(counter);
        if (cached != null && cached.config().equals(config))
            return cached.policy();

        ReminderPolicy policy = ReminderPolicy.withFallback(config.warningRatio(), config.criticalRatio(),
            config.soundCountdownSeconds(), config.chatMilestones(),
            warning -> LOGGER.warn("{} for PTT bonus {}", warning, counter.getTypeId()));
        this.policyCache.put(counter, new CachedPolicy(config, policy));
        return policy;
    }

    private void removeController(TicketKey key) {
        BonusReminderController controller = this.controllers.remove(key);
        if (controller != null)
            controller.close();
    }

    private record TicketKey(UUID playerId, TimeCounter counter) {
    }

    private record PolicyConfig(
        double warningRatio,
        double criticalRatio,
        int soundCountdownSeconds,
        List<Integer> chatMilestones
    ) {
    }

    private record CachedPolicy(PolicyConfig config, ReminderPolicy policy) {
    }
}
