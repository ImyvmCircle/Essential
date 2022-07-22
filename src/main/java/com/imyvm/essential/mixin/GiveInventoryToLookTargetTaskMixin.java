package com.imyvm.essential.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.GiveInventoryToLookTargetTask;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GiveInventoryToLookTargetTask.class)
public class GiveInventoryToLookTargetTaskMixin<E extends LivingEntity> {
    @Inject(method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;J)V", at = @At("HEAD"), cancellable = true)
    private void skipRunningWhenRemoved(ServerWorld world, E entity, long time, CallbackInfo ci) {
        if (entity.isRemoved())
            ci.cancel();
    }
}
