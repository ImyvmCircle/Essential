package com.imyvm.essential.mixin;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static com.imyvm.essential.EssentialMod.CONFIG;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin {
    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"), index = 5)
    private World.ExplosionSourceType modifyDestructionType(World.ExplosionSourceType explosionSourceType) {
        return CONFIG.DO_CREEPER_GRIEFING.getValue() ? explosionSourceType : World.ExplosionSourceType.NONE;
    }
}
