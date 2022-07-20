package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.systems.fly.FlySystem;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.imyvm.essential.Translator.tr;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityMixinInterface {
    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void fallDamageProtect(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (FlySystem.getInstance().checkAndClearFallProtect(player))
            ci.setReturnValue(false);
    }

    private boolean isAwayFromKeyboard = false;
    private long lastActivity = System.currentTimeMillis();
    private Vec3d lastActiveCoordinate = Vec3d.ZERO;

    public void imyvm$updateAwayFromKeyboard(boolean awayFromKeyboard) {
        if (awayFromKeyboard == this.imyvm$isAwayFromKeyboard())
            return;
        this.setAwayFromKeyboard(awayFromKeyboard);

        ServerPlayerEntity player = this.asServerPlayerEntity();
        PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
        player.getServer().getPlayerManager().sendToAll(packet);

        String baseKey = this.imyvm$isAwayFromKeyboard() ? "commands.afk.away" : "commands.afk.back";
        player.sendMessage(tr(baseKey));
        Text broadcastMessage = tr(baseKey + ".broadcast", player.getName());
        player.getServer().getPlayerManager().getPlayerList().stream()
            .filter(u -> u != player)
            .forEach(u -> u.sendMessage(broadcastMessage));

        player.getWorld().updateSleepingPlayers();
    }

    public void imyvm$updateActivity() {
        this.imyvm$updateAwayFromKeyboard(false);
        this.lastActivity = System.currentTimeMillis();
        this.lastActiveCoordinate = this.asServerPlayerEntity().getPos();
    }

    @Override
    public void imyvm$lazyTick() {
        if (!this.imyvm$isAwayFromKeyboard() && this.movedSquaredDistance() > 0.04)
            this.imyvm$updateActivity();
        if (this.imyvm$isAwayFromKeyboard() && this.movedSquaredDistance() > 9)
            this.imyvm$updateActivity();

        if (!this.imyvm$isAwayFromKeyboard() && System.currentTimeMillis() > this.lastActivity + EssentialMod.CONFIG.AFK_AFTER_NO_ACTION.getValue())
            this.imyvm$updateAwayFromKeyboard(true);
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
