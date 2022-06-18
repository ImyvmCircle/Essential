package com.imyvm.essential.mixin;

import com.imyvm.essential.interfaces.PlayerEntityMixinAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(SleepManager.class)
public class SleepManagerMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    private boolean hookPlayerIsSpectator(ServerPlayerEntity player) {
        return player.isSpectator() || ((PlayerEntityMixinAccessor) player).isAwayFromKeyboard();
    }

    @ModifyArg(method = "canResetTime", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"), index = 0)
    private Predicate<PlayerEntity> hookCanResetTimeBySleeping(Predicate<PlayerEntity> predicate) {
        return player -> player.canResetTimeBySleeping() && !((PlayerEntityMixinAccessor) player).isAwayFromKeyboard();
    }
}
