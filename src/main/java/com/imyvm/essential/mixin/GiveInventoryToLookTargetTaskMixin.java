package com.imyvm.essential.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.task.GiveInventoryToLookTargetTask;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LOGGER;

@Mixin(GiveInventoryToLookTargetTask.class)
public class GiveInventoryToLookTargetTaskMixin<E extends LivingEntity> {
    @Inject(
        method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;J)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/task/GiveInventoryToLookTargetTask;playThrowSound(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Vec3d;)V"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void skipThrowingWhenRemoved(ServerWorld world, E entity, long time, CallbackInfo ci, Optional<LookTarget> optional, LookTarget lookTarget, double distance, ItemStack itemStack) {
        if (CONFIG.FIX_ALLAY_DUPLICATE_ITEM.getValue() && entity.isRemoved()) {
            this.logSuspiciousBehaviour(entity, itemStack);
            ci.cancel();
        }
    }

    @Surrogate
    private void skipThrowingWhenRemoved(ServerWorld world, E entity, long time, CallbackInfo ci) {
        if (CONFIG.FIX_ALLAY_DUPLICATE_ITEM.getValue() && entity.isRemoved()) {
            this.logSuspiciousBehaviour(entity, null);
            ci.cancel();
        }
    }

    private void logSuspiciousBehaviour(LivingEntity entity, ItemStack itemStack) {
        String item = itemStack == null ? "[unknown]" : itemStack.getItem().toString();
        LOGGER.warn("Allay" + entity.getPos() + " try to duplicate " + item);
    }
}
