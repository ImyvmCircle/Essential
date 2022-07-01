package com.imyvm.essential.i18n;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.imyvm.essential.EssentialMod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class Language {
    private static final Gson GSON = new Gson();

    private static Language instance = create("en_us");

    public static Language create(String languageId) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        String path = String.format("/assets/%s/lang/%s.json", EssentialMod.MOD_ID, languageId);
        try (InputStream inputStream = Language.class.getResourceAsStream(path)) {
            JsonObject json = GSON.fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
            json.entrySet().forEach((entry) -> builder.put(entry.getKey(), entry.getValue().getAsString()));
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load language " + languageId, e);
        }

        ImmutableMap<String, String> map = builder.build();
        return new Language() {
            @Override
            public String get(String key) {
                return Objects.requireNonNullElse(map.get(key), key);
            }

            @Override
            public boolean hasTranslation(String key) {
                return map.containsKey(key);
            }
        };
    }

    public static Language getInstance() {
        return instance;
    }

    public abstract String get(String key);

    public abstract boolean hasTranslation(String key);
}
