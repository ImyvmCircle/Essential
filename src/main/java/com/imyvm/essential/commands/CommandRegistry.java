package com.imyvm.essential.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
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

        registerPaidFly(dispatcher);
    }

    private static void registerPaidFly(CommandDispatcher<ServerCommandSource> dispatcher) {
        PaidFlyCommand command = new PaidFlyCommand();

        dispatcher.register(
            literal("buyfly")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(literal("gui")
                    .executes(command::runOpenGui))
                .then(literal("list")
                    .executes(command::runList))
                .then(literal("buy")
                    .then(literal("hourly")
                        .executes(command::runBuyOneHour)
                        .then(argument("hours", IntegerArgumentType.integer(1))
                            .executes(command::runBuyHours)))
                    .then(literal("intra_world")
                        .executes(command::runBuyIntraWorld))
                    .then(literal("inter_world")
                        .executes(command::runBuyInterWorld))
                    .then(literal("lifetime")
                        .executes(command::runBuyLifetime)))
                .then(literal("status")
                    .executes(command::runStatus))
                .then(literal("cancel")
                    .executes(command::runCancel))
        );
    }
}
