package com.imyvm.essential.util;

import net.minecraft.network.chat.Component;

import static com.imyvm.essential.Translator.tr;

public class TimeUtil {
    private TimeUtil() {
    }

    public static Component formatDuration(int duration) {
        int hour = duration / 3600;
        int minute = duration % 3600 / 60;
        int second = duration % 60;
        return tr("common.duration", hour, minute, second);
    }
}
