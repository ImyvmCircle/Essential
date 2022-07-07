package com.imyvm.essential.commands;

import com.imyvm.economy.api.DatabaseApi;
import com.imyvm.economy.api.PlayerWallet;
import com.imyvm.economy.util.MoneyUtil;
import com.imyvm.essential.systems.fly.FlySession;
import com.imyvm.essential.systems.fly.FlySystem;
import com.imyvm.essential.systems.fly.PurchaseType;
import com.imyvm.essential.util.TimeUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

public class PaidFlyCommand extends BaseCommand {
    private static final long MILLISECONDS_OF_HOUR = 1000 * 3600;

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    public int runBuyOneHour(CommandContext<ServerCommandSource> context) {
        return this.executeBuyHourly(context.getSource().getPlayer(), 1);
    }

    public int runBuyHours(CommandContext<ServerCommandSource> context) {
        int hours = IntegerArgumentType.getInteger(context, "hours");
        return this.executeBuyHourly(context.getSource().getPlayer(), hours);
    }

    public int runBuyIntraWorld(CommandContext<ServerCommandSource> context) {
        return this.universalBuy(context.getSource().getPlayer(), PurchaseType.INTRA_WORLD);
    }

    public int runBuyInterWorld(CommandContext<ServerCommandSource> context) {
        return this.universalBuy(context.getSource().getPlayer(), PurchaseType.INTER_WORLD);
    }

    public int runBuyLifetime(CommandContext<ServerCommandSource> context) {
        return this.universalBuy(context.getSource().getPlayer(), PurchaseType.LIFETIME);
    }

    public int runCancel(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        FlySession session = FlySystem.getInstance().getSession(player);

        if (session != null) {
            player.sendMessage(tr("commands.buyfly.cancel.success"));
            session.endSession();
        }
        else {
            player.sendMessage(tr("commands.buyfly.cancel.failed.not_flying"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public int runList(CommandContext<ServerCommandSource> context) {
        MutableText text = Text.empty();
        boolean isFirst = true;
        for (PurchaseType type : PurchaseType.values()) {
            if (!isFirst)
                text.append("\n");
            isFirst = false;

            text.append(tr("commands.buyfly.list_price.item",
                tr("goods.paid_fly." + type.getId(), 1), MoneyUtil.format(type.getPrice())));
        }

        context.getSource().sendFeedback(text, false);

        return Command.SINGLE_SUCCESS;
    }

    public int executeBuyHourly(ServerPlayerEntity player, int hours) {
        long price = (long) CONFIG.FLY_HOURLY_PRICE.getValue() * hours;
        PlayerWallet wallet = DatabaseApi.getInstance().getPlayer(player);

        if (!this.checkPurchasable(player, PurchaseType.HOURLY) || !wallet.buyGoodsWithNotification(price, tr("goods.paid_fly.hourly", hours))) {
            return 0;
        }

        long duration = MILLISECONDS_OF_HOUR * hours;
        FlySession currentSession = FlySystem.getInstance().getSession(player);
        if (currentSession != null && !currentSession.isEnded() && currentSession.getType() == PurchaseType.HOURLY) {
            currentSession.addTime(duration);
            Text nowTimeLeft = TimeUtil.formatDuration((int) (currentSession.getTimeLeft() / 1000));
            player.sendMessage(tr("commands.buyfly.success.hourly.extend", hours, nowTimeLeft));
        }
        else {
            FlySession session = new FlySession(player, PurchaseType.HOURLY);
            session.start(duration);
            FlySystem.getInstance().addSession(player, session);
            player.sendMessage(tr("commands.buyfly.success.hourly", hours));
        }

        return Command.SINGLE_SUCCESS;
    }

    public int universalBuy(ServerPlayerEntity player, PurchaseType type) {
        PlayerWallet wallet = DatabaseApi.getInstance().getPlayer(player);
        if (!this.checkPurchasable(player, type) || !wallet.buyGoodsWithNotification(type.getPrice(), tr("goods.paid_fly." + type.getId()))) {
            return 0;
        }

        FlySession session = new FlySession(player, type);
        session.start(Long.MAX_VALUE);
        FlySystem.getInstance().addSession(player, session);

        player.sendMessage(tr("commands.buyfly.success." + type.getId()));
        return Command.SINGLE_SUCCESS;
    }

    public boolean checkPurchasable(ServerPlayerEntity player, PurchaseType type) {
        FlySession currentSession = FlySystem.getInstance().getSession(player);
        if (currentSession == null)
            return true;

        PurchaseType currentType = currentSession.getType();
        if (type == PurchaseType.HOURLY && currentType == PurchaseType.HOURLY)
            return true;

        player.sendMessage(tr("commands.buyfly.failed.incompatible_plan", currentType, type));
        return false;
    }
}
