package com.imyvm.essential.mixin;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(TranslatableTextContent.class)
public interface TranslatableTextContentAccessorInvoker {
    @Accessor
    List<StringVisitable> getTranslations();

    @Invoker("updateTranslations")
    void imyvm$updateTranslations();
}
