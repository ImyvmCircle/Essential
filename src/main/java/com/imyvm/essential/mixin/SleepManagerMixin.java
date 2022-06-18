package com.imyvm.essential.mixin;

import com.imyvm.essential.interfaces.PlayerEntityMixinAccessor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SleepManager.class)
public class SleepManagerMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    private boolean hookPlayerIsSpectator(ServerPlayerEntity player) {
        return player.isSpectator() || ((PlayerEntityMixinAccessor) player).isAwayFromKeyboard();
    }
}
