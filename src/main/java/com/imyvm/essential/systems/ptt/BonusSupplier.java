package com.imyvm.essential.systems.ptt;

import com.imyvm.economy.api.DatabaseApi;
import com.imyvm.economy.util.MoneyUtil;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.TradeType;
import com.imyvm.hoki.util.CommandUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LAZY_TICKER;
import static com.imyvm.essential.Translator.tr;

public class BonusSupplier implements LazyTicker.LazyTickable {
    private static final String BONUS_ACQUIRE_COMMAND_FMT = "/bonus %s %s";
    private static final SimpleCommandExceptionType NO_SUCH_TICKET_EXCEPTION =
        new SimpleCommandExceptionType(tr("commands.bonus.failed.no_such_ticket"));
    private static final SimpleCommandExceptionType TOKEN_NOT_MATCHED_EXCEPTION =
        new SimpleCommandExceptionType(tr("commands.bonus.failed.token_not_matched"));
    private static final BonusSupplier INSTANCE = new BonusSupplier();

    private final Set<Ticket> tickets = ConcurrentHashMap.newKeySet();

    private BonusSupplier() {
    }

    public static BonusSupplier getInstance() {
        return INSTANCE;
    }

    public void register() {
        LAZY_TICKER.register(this);
        ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> this.onPlayerDisconnected(handler.getPlayer()));
    }

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        Text name = tr("name.bonus." + ticket.typeId);
        Text command = CommandUtil.getSuggestCommandText(
            String.format(BONUS_ACQUIRE_COMMAND_FMT, ticket.typeId, ticket.token));
        ticket.player.sendMessage(tr("message.ptt.bonus.to_receive", name, command));
    }

    public void onTicketAcquire(ServerPlayerEntity player, String typeId, String token) throws CommandSyntaxException {
        Optional<Ticket> optional = this.tickets.stream()
            .filter((ticket) -> ticket.player == player && ticket.typeId.equals(typeId))
            .findFirst();
        if (optional.isEmpty())
            throw NO_SUCH_TICKET_EXCEPTION.create();

        Ticket ticket = optional.get();
        if (!ticket.token.equals(token))
            throw TOKEN_NOT_MATCHED_EXCEPTION.create();

        DatabaseApi.getInstance().getPlayer(player).addMoney(ticket.bonus, TradeType.BONUS);
        ticket.data.setStatus(TrackData.Status.OBTAINED);
        this.tickets.remove(ticket);

        Text name = tr("name.bonus." + typeId);
        player.sendMessage(tr("commands.bonus.success", MoneyUtil.format(ticket.bonus), name));
    }

    public void quickTransfer(ServerPlayerEntity player, int bonus, String typeId) {
        DatabaseApi.getInstance().getPlayer(player).addMoney(bonus, TradeType.BONUS);
        Text name = tr("name.bonus." + typeId);
        player.sendMessage(tr("commands.bonus.success", MoneyUtil.format(bonus), name));
    }

    public void onPlayerDisconnected(ServerPlayerEntity player) {
        for (Ticket ticket : this.tickets) {
            if (ticket.player == player) {
                ticket.data.setStatus(TrackData.Status.NOT_MEET);
                this.tickets.remove(ticket);
            }
        }
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        long timestamp = System.currentTimeMillis();
        for (Ticket ticket : this.tickets) {
            if (timestamp > ticket.expiredAt) {
                ticket.player.sendMessage(tr("message.ptt.bonus.expired", tr("name.bonus." + ticket.typeId)));
                long bonusFinal = (long) (ticket.bonus * CONFIG.PTT_REISSUE_RATIO.getValue());
                DatabaseApi.getInstance().getPlayer(ticket.player).addMoney(bonusFinal, TradeType.BONUS);
                ticket.player.sendMessage(
                        tr("message.ptt.bonus.expired.reissue",
                                (bonusFinal - bonusFinal * TradeType.BONUS.getTax()) / 1000));
                ticket.data.setStatus(TrackData.Status.EXPIRED);
                this.tickets.remove(ticket);
            }
        }
    }

    public record Ticket(
        ServerPlayerEntity player,
        TrackData data,
        String typeId,
        String token,
        int bonus,
        long expiredAt
    ) {}
}
