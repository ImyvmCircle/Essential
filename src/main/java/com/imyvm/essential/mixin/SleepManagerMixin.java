package com.imyvm.essential.mixin;

import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(SleepStatus.class)
public class SleepManagerMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"))
    private boolean hookPlayerIsSpectator(ServerPlayer player) {
        return player.isSpectator() || ((PlayerEntityMixinInterface) player).imyvm$isAwayFromKeyboard();
    }

}
