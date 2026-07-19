package com.imyvm.essential.systems.fly;

import com.imyvm.essential.AbilitySources;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.data.PlayerData;
import com.imyvm.essential.systems.BaseSystem;
import com.imyvm.essential.util.TimeUtil;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LAZY_TICKER;
import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;
import static com.imyvm.essential.Translator.tr;

public class FlySystem extends BaseSystem implements LazyTicker.LazyTickable {
    private static final FlySystem INSTANCE = new FlySystem();

    private final Map<UUID, FlySession> playerToSession = new ConcurrentHashMap<>();
    private final Map<UUID, Long> fallProtectionEnd = new ConcurrentHashMap<>();
    private final Set<PaidFlyGui> guiSet = ConcurrentHashMap.newKeySet();

    private FlySystem() {
    }

    @Override
    public void register() {
        LAZY_TICKER.register(this);
        this.registerEvents();
    }

    private void registerEvents() {
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            FlySession session = this.playerToSession.get(player.getUUID());
            if (session != null)
                session.onWorldChange();
        });

        ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> this.onPlayerDisconnect(handler.getPlayer()));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> this.onPlayerJoin(handler.getPlayer()));
    }

    public void onPlayerDisconnect(ServerPlayer player) {
        FlySession session = this.playerToSession.get(player.getUUID());
        if (session != null) {
            PlayerData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID());
            data.setSavedFlySession(session.toSaved());

            Pal.revokeAbility(player, VanillaAbilities.ALLOW_FLYING, AbilitySources.PAID_FLY);
            this.playerToSession.remove(player.getUUID());
        }
    }

    public void onPlayerJoin(ServerPlayer player) {
        PlayerData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID());

        SavedFlySession savedSession = data.resumeSavedFlySession();
        if (savedSession != null) {
            FlySession session = FlySession.fromSaved(player, savedSession);
            this.addSession(player, session);
            player.sendSystemMessage(tr("message.paid_fly.reminder.resumed." + session.getType().getId(),
                TimeUtil.formatDuration((int) (session.getTimeLeft() / 1000))));
        }
    }

    public void addSession(ServerPlayer player, FlySession session) {
        this.playerToSession.put(player.getUUID(), session);
    }

    public FlySession getSession(ServerPlayer player) {
        return this.playerToSession.get(player.getUUID());
    }

    public void addFallProtect(ServerPlayer player) {
        long expireAt = System.currentTimeMillis() + CONFIG.FLY_FALL_PROTECT_DURATION.getValue();
        this.fallProtectionEnd.put(player.getUUID(), expireAt);
    }

    public boolean checkAndClearFallProtect(Player player) {
        boolean result = System.currentTimeMillis() < this.fallProtectionEnd.getOrDefault(player.getUUID(), 0L);
        this.fallProtectionEnd.remove(player.getUUID());
        return result;
    }

    public void addGui(PaidFlyGui gui) {
        this.guiSet.add(gui);
    }

    public void removeGui(PaidFlyGui gui) {
        this.guiSet.remove(gui);
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        this.playerToSession.forEach((uuid, session) -> {
            session.lazyTick(server, tickCounts, msSinceLastTick);
            if (session.isEnded())
                this.playerToSession.remove(uuid);
        });

        this.guiSet.forEach((gui) -> gui.lazyTick(server, tickCounts, msSinceLastTick));
    }

    public static FlySystem getInstance() {
        return INSTANCE;
    }
}
