package com.imyvm.essential.commands;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.systems.ptt.TimeCounter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class CommandRegistry {
    public static final AfkCommand AFK_COMMAND = new AfkCommand();
    public static final BonusAcquireCommand BONUS_ACQUIRE_COMMAND = new BonusAcquireCommand();
    public static final DeathProtectCommand DEATH_PROTECT_COMMAND = new DeathProtectCommand();
    public static final ImyvmEssentialManageCommand IMYVM_ESSENTIAL_MANAGE_COMMAND = new ImyvmEssentialManageCommand();
    public static final ItemShowCommand ITEM_SHOW_COMMAND = new ItemShowCommand();
    public static final PlayTimeTrackCommand PLAY_TIME_TRACK_COMMAND = new PlayTimeTrackCommand();
    public static final PaidFlyCommand PAID_FLY_COMMAND = new PaidFlyCommand();

    private CommandRegistry() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(
            literal("afk")
                .requires(CommandSourceStack::isPlayer)
                .executes(AFK_COMMAND));

        dispatcher.register(
            literal("death_protect")
                .requires(CommandSourceStack::isPlayer)
                .then(literal("buy")
                    .executes(DEATH_PROTECT_COMMAND))
                .then(literal("confirm")
                    .executes(DEATH_PROTECT_COMMAND::runConfirm))
                .then(literal("status")
                    .executes(DEATH_PROTECT_COMMAND::runStatus))
        );

        dispatcher.register(
            literal("ss")
                .requires(CommandSourceStack::isPlayer)
                .executes(ITEM_SHOW_COMMAND));

        dispatcher.register(
            literal("ptt")
                .requires(CommandSourceStack::isPlayer)
                .executes(PLAY_TIME_TRACK_COMMAND));

        dispatcher.register(
            literal("imyvm_essential")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(literal("reload")
                    .executes(IMYVM_ESSENTIAL_MANAGE_COMMAND::runReload)));

        registerPaidFly(dispatcher);
        registerBonusAcquire(dispatcher);
    }

    private static void registerPaidFly(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("buyfly")
                .requires(CommandSourceStack::isPlayer)
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

    private static void registerBonusAcquire(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = literal("bonus")
            .requires(CommandSourceStack::isPlayer);

        for (TimeCounter value : TimeCounter.values())
            builder.then(literal(value.getTypeId())
                .then(argument("token", StringArgumentType.word())
                    .executes(context -> BONUS_ACQUIRE_COMMAND.runAcquire(context, value.getTypeId()))));

        dispatcher.register(builder);
    }
}
