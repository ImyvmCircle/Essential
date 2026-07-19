package com.imyvm.essential.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.GoAndGiveItemsToTarget;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.EssentialMod.LOGGER;

@Mixin(GoAndGiveItemsToTarget.class)
public class GiveInventoryToLookTargetTaskMixin<E extends LivingEntity & InventoryCarrier> {
    @Inject(
        method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;J)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/BehaviorUtils;throwItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;F)V"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void skipThrowingWhenRemoved(ServerLevel world, E entity, long time, CallbackInfo ci, Optional<PositionTracker> targetPosition, PositionTracker depositTarget, Vec3 depositPosition, double distance, ItemStack itemStack) {
        if (CONFIG.FIX_ALLAY_DUPLICATE_ITEM.getValue() && entity.isRemoved()) {
            this.logSuspiciousBehaviour(entity, itemStack);
            ci.cancel();
        }
    }


    private void logSuspiciousBehaviour(LivingEntity entity, ItemStack itemStack) {
        String item = itemStack == null ? "[unknown]" : itemStack.getItem().toString();
        LOGGER.warn("Allay" + entity.position() + " try to duplicate " + item);
    }
}
