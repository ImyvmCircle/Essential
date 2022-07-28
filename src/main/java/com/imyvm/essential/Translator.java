package com.imyvm.essential;

import com.imyvm.hoki.i18n.HokiLanguage;
import com.imyvm.hoki.i18n.HokiTranslator;
import net.minecraft.text.Text;

import java.io.InputStream;

import static com.imyvm.essential.EssentialMod.CONFIG;

public class Translator extends HokiTranslator {
    private static HokiLanguage INSTANCE = createLanguage(CONFIG.LANGUAGE.getValue());

    static {
        CONFIG.LANGUAGE.changeEvents.register((option, oldValue, newValue) -> {
            INSTANCE = createLanguage(option.getValue());
        });
    }

    public static Text tr(String key, Object... args) {
        return HokiTranslator.translate(getLanguageInstance(), key, args);
    }

    public static HokiLanguage getLanguageInstance() {
        return INSTANCE;
    }

    private static HokiLanguage createLanguage(String languageId) {
        String path = HokiLanguage.getResourcePath(EssentialMod.MOD_ID, languageId);
        InputStream inputStream = Translator.class.getResourceAsStream(path);
        return HokiLanguage.create(inputStream);
    }
}
