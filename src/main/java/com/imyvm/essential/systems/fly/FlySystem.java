package com.imyvm.essential.systems.fly;

import com.imyvm.essential.AbilitySources;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.data.PlayerData;
import com.imyvm.essential.data.PlayerDataStorage;
import com.imyvm.essential.systems.BaseSystem;
import com.imyvm.essential.util.TimeUtil;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LAZY_TICKER;
import static com.imyvm.essential.Translator.tr;

public class FlySystem extends BaseSystem implements LazyTicker.LazyTickable {
    private final Map<UUID, FlySession> playerToSession = new ConcurrentHashMap<>();
    private final Map<UUID, Long> fallProtectionEnd = new ConcurrentHashMap<>();
    private static final FlySystem instance = new FlySystem();

    private FlySystem() {
        LAZY_TICKER.register(this);
    }

    @Override
    public void register() {
        this.registerEvents();
    }

    private void registerEvents() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            FlySession session = this.playerToSession.get(player.getUuid());
            if (session != null)
                session.onWorldChange();
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            FlySession session = this.playerToSession.get(player.getUuid());
            PlayerData data = PlayerDataStorage.getOrCreate(player);
            data.setSavedFlySession(session.toSaved());

            Pal.revokeAbility(player, VanillaAbilities.ALLOW_FLYING, AbilitySources.PAID_FLY);
            this.playerToSession.remove(player.getUuid());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            PlayerData data = PlayerDataStorage.getOrCreate(player);

            SavedFlySession savedSession = data.resumeFlySession();
            if (savedSession != null) {
                FlySession session = FlySession.fromSaved(player, savedSession);
                this.addSession(player, session);
                player.sendMessage(tr("message.paid_fly.reminder.resumed." + session.getType().getId(),
                    TimeUtil.formatDuration((int) (session.getTimeLeft() / 1000))));
            }
        });
    }

    public void addSession(ServerPlayerEntity player, FlySession session) {
        this.playerToSession.put(player.getUuid(), session);
    }

    public FlySession getSession(ServerPlayerEntity player) {
        return this.playerToSession.get(player.getUuid());
    }

    public void addFallProtect(ServerPlayerEntity player) {
        this.fallProtectionEnd.put(player.getUuid(), System.currentTimeMillis() + CONFIG.FLY_FALL_PROTECT_DURATION.getValue());
    }

    public boolean checkAndClearFallProtect(PlayerEntity player) {
        boolean result = System.currentTimeMillis() < this.fallProtectionEnd.getOrDefault(player.getUuid(), 0L);
        this.fallProtectionEnd.remove(player.getUuid());
        return result;
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        this.playerToSession.forEach((uuid, session) -> {
            session.lazyTick(server, tickCounts, msSinceLastTick);
            if (session.isEnded())
                this.playerToSession.remove(uuid);
        });
    }

    public static FlySystem getInstance() {
        return instance;
    }
}
