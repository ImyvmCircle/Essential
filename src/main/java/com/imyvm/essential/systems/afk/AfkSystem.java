package com.imyvm.essential.systems.afk;

import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.systems.BaseSystem;
import net.fabricmc.fabric.api.event.player.*;  // CHECKSTYLE.SUPPRESS: AvoidStarImport
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;

import java.util.function.Function;

import static com.imyvm.essential.EssentialMod.LAZY_TICKER;

public class AfkSystem extends BaseSystem implements LazyTicker.LazyTickable {
    @Override
    public void register() {
        LAZY_TICKER.register(this);
        this.registerEvents();
    }

    private void registerEvents() {
        Function<Player, InteractionResult> onActivity = (player) -> {
            ((PlayerEntityMixinInterface) player).imyvm$updateActivity();
            return InteractionResult.PASS;
        };

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> onActivity.apply(sender));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> onActivity.apply(player));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> onActivity.apply(player));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onActivity.apply(player));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> onActivity.apply(player));
        UseItemCallback.EVENT.register((player, world, hand) -> {
            onActivity.apply(player);
            return InteractionResult.PASS;
        });
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        server.getPlayerList().getPlayers().forEach(
            player -> ((PlayerEntityMixinInterface) player).imyvm$lazyTick());
    }
}
