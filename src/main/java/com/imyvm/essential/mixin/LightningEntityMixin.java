package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialGameRules;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LightningEntity;spawnFire(I)V"))
    private void redirectSpawnFire(LightningEntity entity, int spreadAttempts) {
        if (entity.world.getGameRules().getBoolean(EssentialGameRules.DO_LIGHTNING_SPAWN_FIRE)) {
            ((LightningEntityInvoker) entity).imyvm$spawnFire(spreadAttempts);
        }
    }
}
