package com.imyvm.essential.mixin;

import com.imyvm.essential.ImmediatelyTranslator;
import com.imyvm.essential.interfaces.PlayerEntityMixinAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityMixinAccessor {
    private boolean isAwayFromKeyboard = false;

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