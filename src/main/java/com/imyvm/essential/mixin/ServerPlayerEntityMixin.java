package com.imyvm.essential.mixin;

import com.imyvm.essential.ImmediatelyTranslator;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
    private void getAfkDisplayName(CallbackInfoReturnable<Text> ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerEntityMixinInterface accessor = (PlayerEntityMixinInterface) player;
        Text name = player.getName();
        Text displayName = accessor.isAwayFromKeyboard() ? ImmediatelyTranslator.translatable("gui.tab_list.player_away", name) : name;
        ci.setReturnValue(displayName);
    }
}
