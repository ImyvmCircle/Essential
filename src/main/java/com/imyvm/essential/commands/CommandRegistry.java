package com.imyvm.essential.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandRegistry {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
            literal("afk")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(new AfkCommand()));

        dispatcher.register(
            literal("ss")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(new ItemShowCommand()));

        dispatcher.register(
            literal("ptt")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(new PlayTimeTrackCommand()));
    }
}
