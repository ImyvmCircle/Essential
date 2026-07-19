package com.imyvm.essential.mixin;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.imyvm.essential.EssentialMod.CONFIG;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
    @Inject(method = "onRandomTick", at = @At("HEAD"), cancellable = true)
    private void shouldSpawnFire(Level world, BlockPos pos, FluidState state, RandomSource random, CallbackInfo ci) {
        if (!CONFIG.DO_LAVA_SPREAD_FIRE.getValue())
            ci.cancel();
    }
}
