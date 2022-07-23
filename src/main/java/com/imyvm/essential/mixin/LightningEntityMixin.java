package com.imyvm.essential.mixin;

import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.imyvm.essential.EssentialMod.CONFIG;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {
    @Inject(method = "spawnFire", at = @At("HEAD"), cancellable = true)
    private void shouldSpawnFire(int spreadAttempts, CallbackInfo ci) {
        if (!CONFIG.DO_LIGHTNING_SPAWN_FIRE.getValue())
            ci.cancel();
    }
}
