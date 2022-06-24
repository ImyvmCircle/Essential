package com.imyvm.essential;

import com.imyvm.essential.mixin.TranslatableTextContentAccessorInvoker;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public class ImmediatelyTranslator {
    public static Text translatable(String key) {
        TranslatableTextContent context = new TranslatableTextContent(key);
        return translateImmediately(context);
    }

    public static Text translatable(String key, Object... args) {
        TranslatableTextContent context = new TranslatableTextContent(key, args);
        return translateImmediately(context);
    }

    private static Text translateImmediately(TranslatableTextContent context) {
        MutableText mutableText = Text.empty().copy();
        TranslatableTextContentAccessorInvoker accessorInvoker = (TranslatableTextContentAccessorInvoker) context;

        accessorInvoker.imyvm$updateTranslations();
        for (StringVisitable translation : accessorInvoker.imyvm$getTranslations()) {
			Text text = translation instanceof Text ? ((Text) translation) : Text.of(translation.getString());
			mutableText.append(text);
		}

        return mutableText;
    }
}
