package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialGameRules;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {
    @Inject(method = "spawnFire", at = @At("HEAD"), cancellable = true)
    private void shouldSpawnFire(int spreadAttempts, CallbackInfo ci) {
        LightningEntity self = (LightningEntity) (Object) this;
        if (!self.getWorld().getGameRules().getBoolean(EssentialGameRules.DO_LIGHTNING_SPAWN_FIRE))
            ci.cancel();
    }
}
