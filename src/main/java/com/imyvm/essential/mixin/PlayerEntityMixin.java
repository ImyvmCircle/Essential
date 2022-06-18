package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.ImmediatelyTranslator;
import com.imyvm.essential.interfaces.LazyTickable;
import com.imyvm.essential.interfaces.PlayerEntityMixinAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements LazyTickable, PlayerEntityMixinAccessor {
    private boolean isAwayFromKeyboard = false;
    private long lastActivity = System.currentTimeMillis();
    private Vec3d lastActiveCoordinate = Vec3d.ZERO;

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
        if (this.asServerPlayerEntity().getPos().squaredDistanceTo(this.lastActiveCoordinate) > 9)
            updateActivity();

        if (!isAwayFromKeyboard() && System.currentTimeMillis() > this.lastActivity + EssentialMod.config.getAfkAfterNoAction())
            updateAwayFromKeyboard(true);
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
}
