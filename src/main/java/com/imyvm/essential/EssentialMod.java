package com.imyvm.essential;

import com.imyvm.essential.interfaces.LazyTickable;
import com.imyvm.essential.interfaces.PlayerEntityMixinAccessor;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

public class EssentialMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("essential");
	public static ModConfig config;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
		registerEvents();
		registerLazyTick();

		initializeConfig();

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
			((PlayerEntityMixinAccessor) player).updateActivity();
			return ActionResult.PASS;
		};

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> onActivity.apply(player));
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> onActivity.apply(player));
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onActivity.apply(player));
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> onActivity.apply(player));
		UseItemCallback.EVENT.register((player, world, hand) -> {
			onActivity.apply(player);
			return TypedActionResult.pass(null);
		});
	}

	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<ServerCommandSource>literal("afk")
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayer();
					if (player == null) {
						context.getSource().sendError(ImmediatelyTranslator.translatable("commands.afk.failed.not_player"));
						return 0;
					}

					PlayerEntityMixinAccessor playerMixin = (PlayerEntityMixinAccessor) player;
					playerMixin.updateAwayFromKeyboard(!playerMixin.isAwayFromKeyboard());

					return Command.SINGLE_SUCCESS;
				})
		);
	}

	public void registerLazyTick() {
		final int PERIOD = 20 - 1;
		IntUnaryOperator periodIncrease = v -> v == PERIOD ? 0 : v + 1;

		AtomicInteger i = new AtomicInteger();
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			if (i.getAndUpdate(periodIncrease) != 0)
				return;

			server.getPlayerManager().getPlayerList().forEach(player -> ((LazyTickable) player).lazyTick());
		});
	}
}
