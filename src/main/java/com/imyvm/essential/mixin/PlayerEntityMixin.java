package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.data.PlayerData;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.systems.fly.FlySystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;
import static com.imyvm.essential.Translator.tr;

@SuppressWarnings("DeclarationOrder")
@Mixin(value = Player.class, priority = 900)
public class PlayerEntityMixin implements PlayerEntityMixinInterface {
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void fallDamageProtect(double fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> ci) {
        Player player = (Player) (Object) this;
        if (FlySystem.getInstance().checkAndClearFallProtect(player))
            ci.setReturnValue(false);
    }

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void checkDeathProtect(ServerLevel level, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        PlayerData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID());
        int deathProtectLevel = data.getDeathProtectLevel();
        if (deathProtectLevel > 0) {
            data.setDeathProtectLevel(deathProtectLevel - 1);
            data.setKeepInventoryAtRespawn(true);
            player.sendSystemMessage(tr("message.death_protect.consume", deathProtectLevel - 1));
            ci.cancel();
        }
    }

    @Unique
    private boolean isAwayFromKeyboard = false;
    @Unique
    private long lastActivity = System.currentTimeMillis();
    @Unique
    private Vec3 lastActiveCoordinate = Vec3.ZERO;

    public void imyvm$updateAwayFromKeyboard(boolean awayFromKeyboard) {
        if (awayFromKeyboard == this.imyvm$isAwayFromKeyboard())
            return;
        this.setAwayFromKeyboard(awayFromKeyboard);

        ServerPlayer player = this.asServerPlayerEntity();
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);
        Objects.requireNonNull(player.level().getServer()).getPlayerList().broadcastAll(packet);

        String baseKey = this.imyvm$isAwayFromKeyboard() ? "commands.afk.away" : "commands.afk.back";
        player.sendSystemMessage(tr(baseKey));
        Component broadcastMessage = tr(baseKey + ".broadcast", player.getName());
        player.level().getServer().getPlayerList().getPlayers().stream()
                .filter(u -> u != player)
                .forEach(u -> u.sendSystemMessage(broadcastMessage));

        player.level().updateSleepingPlayerList();
    }

    public void imyvm$updateActivity() {
        this.imyvm$updateAwayFromKeyboard(false);
        this.lastActivity = System.currentTimeMillis();
        this.lastActiveCoordinate = this.asServerPlayerEntity().position();
    }

    @Override
    public void imyvm$lazyTick() {
        if (!this.imyvm$isAwayFromKeyboard() && this.movedSquaredDistance() > 0.04)
            this.imyvm$updateActivity();
        if (this.imyvm$isAwayFromKeyboard() && this.movedSquaredDistance() > 9)
            this.imyvm$updateActivity();

        long timeoutAt = this.lastActivity + EssentialMod.CONFIG.AFK_AFTER_NO_ACTION.getValue();
        if (!this.imyvm$isAwayFromKeyboard() && System.currentTimeMillis() > timeoutAt)
            this.imyvm$updateAwayFromKeyboard(true);
    }

    @Unique
    private double movedSquaredDistance() {
        return this.asServerPlayerEntity().position().distanceToSqr(this.lastActiveCoordinate);
    }

    @Unique
    private ServerPlayer asServerPlayerEntity() {
        return (ServerPlayer) (Object) this;
    }

    public boolean imyvm$isAwayFromKeyboard() {
        return this.isAwayFromKeyboard;
    }

    @Unique
    private void setAwayFromKeyboard(boolean awayFromKeyboard) {
        this.isAwayFromKeyboard = awayFromKeyboard;
    }
}