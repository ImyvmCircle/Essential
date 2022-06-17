package com.imyvm.essential.mixin;

import com.imyvm.essential.interfaces.ServerPlayerEntityMixinAccessor;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityMixinAccessor {
    private final Text AFK_SUFFIX = Text.of(" | AFK").copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY));

    private boolean isAwayFromKeyboard = false;

    @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
    private void getAfkDisplayName(CallbackInfoReturnable<Text> ci) {
        MutableText name = this.asServerPlayerEntity().getName().copy();
        if (this.isAwayFromKeyboard())
            name.append(AFK_SUFFIX);
        ci.setReturnValue(name);
    }

    public void updateAwayFromKeyboard(boolean awayFromKeyboard) {
        if (awayFromKeyboard == this.isAwayFromKeyboard())
            return;
        setAwayFromKeyboard(awayFromKeyboard);

        PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.asServerPlayerEntity());
        this.asServerPlayerEntity().getServer().getPlayerManager().sendToAll(packet);
        this.asServerPlayerEntity().getWorld().updateSleepingPlayers();
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
