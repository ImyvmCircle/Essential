package com.imyvm.essential.mixin;

import com.imyvm.essential.data.PlayerData;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;
import static com.imyvm.essential.Translator.tr;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
    private void getAfkDisplayName(CallbackInfoReturnable<Text> ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerEntityMixinInterface accessor = (PlayerEntityMixinInterface) player;
        Text name = player.getName();
        Text displayName = accessor.imyvm$isAwayFromKeyboard() ? tr("gui.tab_list.player_away", name) : name;
        ci.setReturnValue(displayName);
    }

    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void checkShouldKeepInventory(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY))
            return;

        if (!alive && !oldPlayer.isSpectator()) {
            PlayerData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUuid());
            if (data.shouldKeepInventoryAtRespawn()) {
                player.getInventory().clone(oldPlayer.getInventory());
                data.setKeepInventoryAtRespawn(false);
            }
        }
    }
}