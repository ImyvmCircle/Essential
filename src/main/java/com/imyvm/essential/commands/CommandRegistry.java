package com.imyvm.essential.commands;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.systems.ptt.TimeCounter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandRegistry {
    public static final AfkCommand AFK_COMMAND = new AfkCommand();
    public static final BonusAcquireCommand BONUS_ACQUIRE_COMMAND = new BonusAcquireCommand();
    public static final DeathProtectCommand DEATH_PROTECT_COMMAND = new DeathProtectCommand();
    public static final ImyvmEssentialManageCommand IMYVM_ESSENTIAL_MANAGE_COMMAND = new ImyvmEssentialManageCommand();
    public static final ItemShowCommand ITEM_SHOW_COMMAND = new ItemShowCommand();
    public static final PlayTimeTrackCommand PLAY_TIME_TRACK_COMMAND = new PlayTimeTrackCommand();
    public static final PaidFlyCommand PAID_FLY_COMMAND = new PaidFlyCommand();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
            literal("afk")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(AFK_COMMAND));

        dispatcher.register(
            literal("death_protect")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(literal("buy")
                    .executes(DEATH_PROTECT_COMMAND))
                .then(literal("confirm")
                    .executes(DEATH_PROTECT_COMMAND::runConfirm))
                .then(literal("status")
                    .executes(DEATH_PROTECT_COMMAND::runStatus))
        );

        dispatcher.register(
            literal("ss")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(ITEM_SHOW_COMMAND));

        dispatcher.register(
            literal("ptt")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(PLAY_TIME_TRACK_COMMAND));

        dispatcher.register(
            literal("imyvm_essential")
                .requires(Permissions.require(EssentialMod.MOD_ID + ".manage", 3))
                .then(literal("reload")
                    .executes(IMYVM_ESSENTIAL_MANAGE_COMMAND::runReload)));

        registerPaidFly(dispatcher);
        registerBonusAcquire(dispatcher);
    }

    private static void registerPaidFly(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("buyfly")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(literal("gui")
                    .executes(PAID_FLY_COMMAND::runOpenGui))
                .then(literal("list")
                    .executes(PAID_FLY_COMMAND::runList))
                .then(literal("buy")
                    .then(literal("hourly")
                        .executes(PAID_FLY_COMMAND::runBuyOneHour)
                        .then(argument("hours", IntegerArgumentType.integer(1))
                            .executes(PAID_FLY_COMMAND::runBuyHours)))
                    .then(literal("intra_world")
                        .executes(PAID_FLY_COMMAND::runBuyIntraWorld))
                    .then(literal("inter_world")
                        .executes(PAID_FLY_COMMAND::runBuyInterWorld))
                    .then(literal("lifetime")
                        .executes(PAID_FLY_COMMAND::runBuyLifetime)))
                .then(literal("status")
                    .executes(PAID_FLY_COMMAND::runStatus))
                .then(literal("cancel")
                    .executes(PAID_FLY_COMMAND::runCancel))
        );
    }

    private static void registerBonusAcquire(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("bonus")
            .requires(ServerCommandSource::isExecutedByPlayer);

        for (TimeCounter value : TimeCounter.values())
            builder.then(literal(value.getTypeId())
                .then(argument("token", StringArgumentType.word())
                    .executes(context -> BONUS_ACQUIRE_COMMAND.runAcquire(context, value.getTypeId()))));

        dispatcher.register(builder);
    }
}
