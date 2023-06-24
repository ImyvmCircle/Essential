package com.imyvm.essential.commands;

import com.imyvm.economy.api.DatabaseApi;
import com.imyvm.economy.api.PlayerWallet;
import com.imyvm.economy.util.MoneyUtil;
import com.imyvm.essential.TradeTypeRegistry;
import com.imyvm.essential.data.PlayerData;
import com.imyvm.hoki.util.CommandUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;
import static com.imyvm.essential.Translator.tr;

public class DeathProtectCommand extends BaseCommand {
    private static final SimpleCommandExceptionType LEVEL_TOO_HIGH_EXCEPTION =
        new SimpleCommandExceptionType(tr("commands.death_protect.failed.level_too_high"));
    private static final SimpleCommandExceptionType NO_PENDING_TRANSACTION_EXCEPTION =
        new SimpleCommandExceptionType(tr("commands.death_protect.failed.no_pending"));
    private static final String CONFIRM_COMMAND = "/death_protect confirm";
    private static final long PENDING_TIME_LIMIT = 60 * 1000;  // 1 minute

    private final Map<UUID, Long> pendingTransaction = new ConcurrentHashMap<>();

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUuid());
        int level = data.getDeathProtectLevel();
        long money = this.calculateNextLevelPrice(level);

        PlayerWallet wallet = DatabaseApi.getInstance().getPlayer(player);
        if (wallet.getMoney() < money)  // if insufficient, throw an `INSUFFICIENT_BALANCE_EXCEPTION` by ImyvmEconomy
            wallet.buyGoodsWithNotificationInCommand(money, tr("goods.death_protect.with_level", level + 1), TradeTypeRegistry.TradeType.DEATHPROTECT);

        this.pendingTransaction.put(player.getUuid(), System.currentTimeMillis() + PENDING_TIME_LIMIT);
        player.sendMessage(tr("commands.death_protect.message.to_confirm",
            MoneyUtil.format(money), level + 1, CommandUtil.getSuggestCommandText(CONFIRM_COMMAND)));

        return Command.SINGLE_SUCCESS;
    }

    public int runConfirm(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (this.pendingTransaction.getOrDefault(player.getUuid(), 0L) < System.currentTimeMillis())
            throw NO_PENDING_TRANSACTION_EXCEPTION.create();
        this.pendingTransaction.remove(player.getUuid());

        PlayerData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUuid());
        int level = data.getDeathProtectLevel();
        long money = this.calculateNextLevelPrice(level);

        DatabaseApi.getInstance().getPlayer(player).buyGoodsWithNotificationInCommand(money,
            tr("goods.death_protect.with_level", level + 1), TradeTypeRegistry.TradeType.DEATHPROTECT);
        data.setDeathProtectLevel(level + 1);

        return Command.SINGLE_SUCCESS;
    }

    public int runStatus(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        PlayerData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUuid());
        int level = data.getDeathProtectLevel();
        player.sendMessage(tr("commands.death_protect.status", level));

        return Command.SINGLE_SUCCESS;
    }

    private long calculateNextLevelPrice(int level) throws CommandSyntaxException {
        long price = CONFIG.DEATH_PROTECT_BASE_PRICE.getValue();
        for (int i = 0; i < level; i++) {
            price *= 2;
            if (price < 0)  // in case of overflow
                throw LEVEL_TOO_HIGH_EXCEPTION.create();
        }
        return price;
    }
}
