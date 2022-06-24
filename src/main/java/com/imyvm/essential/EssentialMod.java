package com.imyvm.essential;

import com.imyvm.essential.commands.*;
import com.imyvm.essential.control.TeleportRequest;
import com.imyvm.essential.interfaces.LazyTickable;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.util.CommandUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

public class EssentialMod implements ModInitializer {
	public static final String MOD_ID = "imyvm_essential";
	public static final Logger LOGGER = LoggerFactory.getLogger("Essential");
	public static ModConfig config;

	public static final Set<TeleportRequest> teleportRequests = ConcurrentHashMap.newKeySet();

	@Override
	public void onInitialize() {
		registerCommands();
		registerEvents();
		registerLazyTick();

		initializeConfig();
		EssentialStatistics.initialize();
		EssentialGameRules.initialize();

		LOGGER.info("Imyvm Essential initialized");
	}

	public void initializeConfig() {
		try {
			config = new ModConfig();
		} catch (Exception e) {
			LOGGER.error("Failed to initialized config: " + e);
			throw new RuntimeException("Failed to initialized config", e);
		}
	}

	public void registerEvents() {
		Function<PlayerEntity, ActionResult> onActivity = (player) -> {
			((PlayerEntityMixinInterface) player).updateActivity();
			return ActionResult.PASS;
		};

		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> onActivity.apply(sender));
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> onActivity.apply(player));
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> onActivity.apply(player));
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onActivity.apply(player));
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> onActivity.apply(player));
		UseItemCallback.EVENT.register((player, world, hand) -> {
			onActivity.apply(player);
			return TypedActionResult.pass(null);
		});
	}

	public void registerCommands() {
		new AfkCommand();
		new ItemShowCommand();
		new TeleportCommand();
	}

	public void registerLazyTick() {
		final int PERIOD = 20 - 1;
		IntUnaryOperator periodIncrease = v -> v == PERIOD ? 0 : v + 1;

		AtomicInteger i = new AtomicInteger();
		AtomicLong lastLazyTickTimeAtomic = new AtomicLong(System.currentTimeMillis());
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			int value = i.getAndUpdate(periodIncrease);

			switch (value) {
				case 0 -> server.getPlayerManager().getPlayerList().forEach(player -> ((LazyTickable) player).lazyTick());
				case 1 -> teleportRequests.forEach(TeleportRequest::lazyTick);
				case 2 -> {
					long currentTime = System.currentTimeMillis() / 1000;
					long lastLazyTickTime = lastLazyTickTimeAtomic.getAndSet(currentTime);
					int duration = (int) (currentTime - lastLazyTickTime);
					int requiredTime = EssentialMod.config.getUserGroupRequiredPTT();

					server.getPlayerManager().getPlayerList()
						.forEach(player -> {
							int previousPlayTime = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(EssentialStatistics.PLAY_TIME_TRACK));
							player.increaseStat(EssentialStatistics.PLAY_TIME_TRACK, duration);

							if (previousPlayTime < requiredTime && previousPlayTime + duration >= requiredTime) {
								LOGGER.info(String.format("user \"%s\" has been set to user group", player.getGameProfile().getName()));
								server.getCommandManager().executeWithPrefix(CommandUtil.getServerCommandSource(server), "/role assign " + player.getGameProfile().getName() + " user");
							}
						});
				}
				default -> {}
			}
		});
	}
}
