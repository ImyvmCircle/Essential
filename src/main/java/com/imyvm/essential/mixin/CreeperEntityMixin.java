package com.imyvm.essential.mixin;

import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static com.imyvm.essential.EssentialMod.CONFIG;

@Mixin(Creeper.class)
public class CreeperEntityMixin {
    @ModifyArg(method = "explodeCreeper", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"), index = 5)
    private Level.ExplosionInteraction modifyDestructionType(Level.ExplosionInteraction explosionSourceType) {
        return CONFIG.DO_CREEPER_GRIEFING.getValue() ? explosionSourceType : Level.ExplosionInteraction.NONE;
    }
}