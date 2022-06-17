package com.imyvm.essential;

import com.imyvm.essential.interfaces.ServerPlayerEntityMixinAccessor;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EssentialMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("essential");

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));

		LOGGER.info("Imyvm Essential initialized");
	}

	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<ServerCommandSource>literal("afk")
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayer();
					if (player == null) {
						context.getSource().sendError(Text.of("You must be a player to execute this command"));
						return 0;
					}

					ServerPlayerEntityMixinAccessor playerMixin = (ServerPlayerEntityMixinAccessor) player;
					playerMixin.updateAwayFromKeyboard(!playerMixin.isAwayFromKeyboard());

					return Command.SINGLE_SUCCESS;
				})
		);
	}
}
