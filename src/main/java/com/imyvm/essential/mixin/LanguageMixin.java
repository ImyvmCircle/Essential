package com.imyvm.essential.mixin;

import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.InputStream;
import java.util.function.BiConsumer;

@Mixin(Language.class)
public abstract class LanguageMixin {
    @ModifyArg(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Language;load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V"), index = 1)
    private static BiConsumer<String, String> accessAndPutItems(BiConsumer<String, String> consumer) {
        InputStream inputStream = Language.class.getResourceAsStream("/assets/imyvm_essential/lang/en_us.json");
        Language.load(inputStream, consumer);
        return consumer;
    }
}
