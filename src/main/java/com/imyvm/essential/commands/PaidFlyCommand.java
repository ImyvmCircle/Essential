package com.imyvm.essential.commands;

import com.imyvm.economy.api.DatabaseApi;
import com.imyvm.economy.api.PlayerWallet;
import com.imyvm.economy.util.MoneyUtil;
import com.imyvm.essential.systems.fly.FlySession;
import com.imyvm.essential.systems.fly.FlySystem;
import com.imyvm.essential.systems.fly.PaidFlyGui;
import com.imyvm.essential.systems.fly.PurchaseType;
import com.imyvm.essential.util.TimeUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

public class PaidFlyCommand extends BaseCommand {
    private static final Dynamic2CommandExceptionType INCOMPATIBLE_PLAN_EXCEPTION =
        new Dynamic2CommandExceptionType((current, wanted) ->
            tr("commands.buyfly.failed.incompatible_plan", current, wanted));
    private static final SimpleCommandExceptionType NOT_ENABLED_FLYING_EXCEPTION =
        new SimpleCommandExceptionType(tr("commands.buyfly.cancel.failed.not_flying"));

    private static final long MILLISECONDS_OF_HOUR = 1000 * 3600;

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    public int runOpenGui(CommandContext<CommandSourceStack> context) {
        new PaidFlyGui(context.getSource().getPlayer()).open();
        return Command.SINGLE_SUCCESS;
    }

    public int runBuyOneHour(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return this.executeBuyHourly(context.getSource().getPlayer(), 1);
    }

    public int runBuyHours(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int hours = IntegerArgumentType.getInteger(context, "hours");
        return this.executeBuyHourly(context.getSource().getPlayer(), hours);
    }

    public int runBuyIntraWorld(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return this.universalBuy(context.getSource().getPlayer(), PurchaseType.INTRA_WORLD);
    }

    public int runBuyInterWorld(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return this.universalBuy(context.getSource().getPlayer(), PurchaseType.INTER_WORLD);
    }

    public int runBuyLifetime(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return this.universalBuy(context.getSource().getPlayer(), PurchaseType.LIFETIME);
    }

    public int runCancel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        return this.executeCancel(player);
    }

    public int runList(CommandContext<CommandSourceStack> context) {
        MutableComponent text = Component.empty();
        boolean isFirst = true;
        for (PurchaseType type : PurchaseType.values()) {
            if (!isFirst)
                text.append("\n");
            isFirst = false;

            text.append(tr("commands.buyfly.list.item", type.getName(), MoneyUtil.format(type.getPrice())));
        }

        Supplier<Component> textSupplier = () -> text;
        context.getSource().sendSuccess(textSupplier, false);

        return Command.SINGLE_SUCCESS;
    }

    public int runStatus(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        FlySession session = FlySystem.getInstance().getSession(player);

        if (session == null)
            player.sendSystemMessage(tr("commands.buyfly.status.not_flying"));
        else if (session.getType() == PurchaseType.HOURLY) {
            int duration = (int) (session.getTimeLeft() / 1000);
            player.sendSystemMessage(tr("commands.buyfly.status.hourly", TimeUtil.formatDuration(duration)));
        } else
            player.sendSystemMessage(tr("commands.buyfly.status.one_time", session.getType().getName()));

        return Command.SINGLE_SUCCESS;
    }

    public int executeBuyHourly(ServerPlayer player, int hours) throws CommandSyntaxException {
        long price = (long) CONFIG.FLY_HOURLY_PRICE.getValue() * hours;
        PlayerWallet wallet = DatabaseApi.getInstance().getPlayer(player);

        this.checkPurchasable(player, PurchaseType.HOURLY);
        wallet.buyGoodsWithNotificationInCommand(price, tr("goods.paid_fly.hourly", hours));

        long duration = MILLISECONDS_OF_HOUR * hours;
        FlySession currentSession = FlySystem.getInstance().getSession(player);
        if (currentSession != null && !currentSession.isEnded() && currentSession.getType() == PurchaseType.HOURLY) {
            currentSession.addTime(duration);
            Component nowTimeLeft = TimeUtil.formatDuration((int) (currentSession.getTimeLeft() / 1000));
            player.sendSystemMessage(tr("commands.buyfly.buy.success.hourly.extend", hours, nowTimeLeft));
        } else {
            FlySession session = new FlySession(player, PurchaseType.HOURLY);
            session.start(duration);
            FlySystem.getInstance().addSession(player, session);
            player.sendSystemMessage(tr("commands.buyfly.buy.success.hourly", hours));
        }

        return Command.SINGLE_SUCCESS;
    }

    public int executeCancel(ServerPlayer player) throws CommandSyntaxException {
        FlySession session = FlySystem.getInstance().getSession(player);

        if (session == null)
            throw NOT_ENABLED_FLYING_EXCEPTION.create();

        player.sendSystemMessage(tr("commands.buyfly.cancel.success"));
        session.endSession();

        return Command.SINGLE_SUCCESS;
    }

    public int universalBuy(ServerPlayer player, PurchaseType type) throws CommandSyntaxException {
        PlayerWallet wallet = DatabaseApi.getInstance().getPlayer(player);

        this.checkPurchasable(player, type);
        wallet.buyGoodsWithNotificationInCommand(type.getPrice(), type.getName());

        FlySession session = new FlySession(player, type);
        session.start(Long.MAX_VALUE);
        FlySystem.getInstance().addSession(player, session);

        player.sendSystemMessage(tr("commands.buyfly.buy.success." + type.getId()));
        return Command.SINGLE_SUCCESS;
    }

    public void checkPurchasable(ServerPlayer player, PurchaseType type) throws CommandSyntaxException {
        FlySession currentSession = FlySystem.getInstance().getSession(player);
        if (currentSession == null)
            return;

        PurchaseType currentType = currentSession.getType();
        if (!(type == PurchaseType.HOURLY && currentType == PurchaseType.HOURLY))
            throw INCOMPATIBLE_PLAN_EXCEPTION.create(currentType.getName(), type.getName());
    }
}
