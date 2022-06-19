package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.ImmediatelyTranslator;
import com.imyvm.essential.control.TeleportRequest;
import com.imyvm.essential.interfaces.LazyTickable;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements LazyTickable, PlayerEntityMixinInterface {
    private boolean isAwayFromKeyboard = false;
    private long lastActivity = System.currentTimeMillis();
    private Vec3d lastActiveCoordinate = Vec3d.ZERO;

    private TeleportRequest requestAsSender;
    private TeleportRequest requestAsReceiver;

    public void updateAwayFromKeyboard(boolean awayFromKeyboard) {
        if (awayFromKeyboard == this.isAwayFromKeyboard())
            return;
        setAwayFromKeyboard(awayFromKeyboard);

        ServerPlayerEntity player = this.asServerPlayerEntity();
        PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
        player.getServer().getPlayerManager().sendToAll(packet);

        String baseKey = this.isAwayFromKeyboard() ? "commands.afk.away" : "commands.afk.back";
        player.sendMessage(ImmediatelyTranslator.translatable(baseKey));
        Text broadcastMessage = ImmediatelyTranslator.translatable(baseKey + ".broadcast", player.getName());
        player.getServer().getPlayerManager().getPlayerList().stream()
            .filter(u -> u != player)
            .forEach(u -> u.sendMessage(broadcastMessage));

        player.getWorld().updateSleepingPlayers();
    }

    public void updateActivity() {
        updateAwayFromKeyboard(false);
        this.lastActivity = System.currentTimeMillis();
        this.lastActiveCoordinate = this.asServerPlayerEntity().getPos();
    }

    @Override
    public void lazyTick() {
        if (!this.isAwayFromKeyboard() && this.movedSquaredDistance() > 0.04)
            updateActivity();
        if (this.isAwayFromKeyboard() && this.movedSquaredDistance() > 9)
            updateActivity();

        if (!this.isAwayFromKeyboard() && System.currentTimeMillis() > this.lastActivity + EssentialMod.config.getAfkAfterNoAction())
            updateAwayFromKeyboard(true);
    }

    private double movedSquaredDistance() {
        return this.asServerPlayerEntity().getPos().squaredDistanceTo(this.lastActiveCoordinate);
    }

    public ServerPlayerEntity asServerPlayerEntity() {
        return (ServerPlayerEntity) (Object) this;
    }

    public boolean isAwayFromKeyboard() {
        return this.isAwayFromKeyboard;
    }

    public void setAwayFromKeyboard(boolean awayFromKeyboard) {
        this.isAwayFromKeyboard = awayFromKeyboard;
    }

    public TeleportRequest getRequestAsSender() {
        return this.requestAsSender;
    }

    public void setRequestAsSender(TeleportRequest requestAsSender) {
        this.requestAsSender = requestAsSender;
    }

    public TeleportRequest getRequestAsReceiver() {
        return this.requestAsReceiver;
    }

    public void setRequestAsReceiver(TeleportRequest requestAsReceiver) {
        this.requestAsReceiver = requestAsReceiver;
    }
}
