package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.ImmediatelyTranslator;
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

    public void imyvm$updateAwayFromKeyboard(boolean awayFromKeyboard) {
        if (awayFromKeyboard == this.imyvm$isAwayFromKeyboard())
            return;
        setAwayFromKeyboard(awayFromKeyboard);

        ServerPlayerEntity player = this.asServerPlayerEntity();
        PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
        player.getServer().getPlayerManager().sendToAll(packet);

        String baseKey = this.imyvm$isAwayFromKeyboard() ? "commands.afk.away" : "commands.afk.back";
        player.sendMessage(ImmediatelyTranslator.translatable(baseKey));
        Text broadcastMessage = ImmediatelyTranslator.translatable(baseKey + ".broadcast", player.getName());
        player.getServer().getPlayerManager().getPlayerList().stream()
            .filter(u -> u != player)
            .forEach(u -> u.sendMessage(broadcastMessage));

        player.getWorld().updateSleepingPlayers();
    }

    public void imyvm$updateActivity() {
        imyvm$updateAwayFromKeyboard(false);
        this.lastActivity = System.currentTimeMillis();
        this.lastActiveCoordinate = this.asServerPlayerEntity().getPos();
    }

    @Override
    public void imyvm$lazyTick() {
        if (!this.imyvm$isAwayFromKeyboard() && this.movedSquaredDistance() > 0.04)
            imyvm$updateActivity();
        if (this.imyvm$isAwayFromKeyboard() && this.movedSquaredDistance() > 9)
            imyvm$updateActivity();

        if (!this.imyvm$isAwayFromKeyboard() && System.currentTimeMillis() > this.lastActivity + EssentialMod.config.getAfkAfterNoAction())
            imyvm$updateAwayFromKeyboard(true);
    }

    private double movedSquaredDistance() {
        return this.asServerPlayerEntity().getPos().squaredDistanceTo(this.lastActiveCoordinate);
    }

    private ServerPlayerEntity asServerPlayerEntity() {
        return (ServerPlayerEntity) (Object) this;
    }

    public boolean imyvm$isAwayFromKeyboard() {
        return this.isAwayFromKeyboard;
    }

    private void setAwayFromKeyboard(boolean awayFromKeyboard) {
        this.isAwayFromKeyboard = awayFromKeyboard;
    }
}
