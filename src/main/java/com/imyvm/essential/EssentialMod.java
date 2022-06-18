package com.imyvm.essential;

import com.imyvm.essential.interfaces.PlayerEntityMixinAccessor;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EssentialMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("essential");
	public static ModConfig config;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));

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

	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
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
}
