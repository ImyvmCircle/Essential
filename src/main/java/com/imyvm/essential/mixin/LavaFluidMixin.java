package com.imyvm.essential.mixin;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.imyvm.essential.EssentialMod.CONFIG;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
    @Inject(method = "onRandomTick", at = @At("HEAD"), cancellable = true)
    private void shouldSpawnFire(World world, BlockPos pos, FluidState state, Random random, CallbackInfo ci) {
        if (!CONFIG.DO_LAVA_SPREAD_FIRE.getValue())
            ci.cancel();
    }
}
