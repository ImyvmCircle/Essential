package com.imyvm.essential.systems.fly;

import com.imyvm.essential.AbilitySources;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.util.TimeUtil;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

public class FlySession implements LazyTicker.LazyTickable {
    private final ServerPlayerEntity player;
    private final PurchaseType type;
    private boolean ended = false;
    private boolean notified = false;
    private long timeLeft;

    public FlySession(ServerPlayerEntity player, PurchaseType type) {
        this.player = player;
        this.type = type;
    }

    public SavedFlySession toSaved() {
        PlayerAbilities abilities = this.player.getAbilities();
        if (this.type == PurchaseType.HOURLY || this.type == PurchaseType.LIFETIME)
            return new SavedFlySession(this.type, this.timeLeft, abilities.flying);
        return null;
    }

    public static FlySession fromSaved(ServerPlayerEntity player, SavedFlySession savedSession) {
        FlySession session = new FlySession(player, savedSession.getType());

        if (savedSession.isFlying())
            player.getAbilities().flying = true;
        session.start(savedSession.getTimeLeft());

        return session;
    }

    public void start(long duration) {
        this.timeLeft = duration;
        this.setFlyAbility(true);
    }

    public void addTime(long duration) {
        this.timeLeft += duration;
        this.notified = false;
    }

    public void onWorldChange() {
        if (!this.allowWorldChange()) {
            this.player.sendMessage(tr("message.paid_fly.end.change_world"));
            this.endSession();
        }
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        this.timeLeft -= msSinceLastTick;

        if (!this.notified && this.timeLeft < CONFIG.FLY_HOURLY_NOTICE_AT.getValue()) {
            this.notified = true;
            Text duration = TimeUtil.formatDuration((int) (this.timeLeft / 1000));
            this.player.sendMessage(tr("message.paid_fly.reminder.expired", duration));
        }

        if (this.timeLeft <= 0) {
            this.player.sendMessage(tr("message.paid_fly.end.expired"));
            this.endSession();
        }
    }

    public boolean allowWorldChange() {
        return this.type != PurchaseType.INTRA_WORLD;
    }

    public void endSession() {
        FlySystem.getInstance().addFallProtect(this.player);
        this.setFlyAbility(false);
        this.ended = true;
    }

    private void setFlyAbility(boolean canFly) {
        if (canFly)
            Pal.grantAbility(this.player, VanillaAbilities.ALLOW_FLYING, AbilitySources.PAID_FLY);
        else
            Pal.revokeAbility(this.player, VanillaAbilities.ALLOW_FLYING, AbilitySources.PAID_FLY);
        this.player.sendAbilitiesUpdate();
    }

    public boolean isEnded() {
        return this.ended;
    }

    public PurchaseType getType() {
        return this.type;
    }

    public long getTimeLeft() {
        return this.timeLeft;
    }
}
