package com.imyvm.essential.util;

import net.minecraft.text.Text;

import static com.imyvm.essential.Translator.tr;

public class TimeUtil {
    public static Text formatDuration(int duration) {
        int hour = duration / 3600;
        int minute = duration % 3600 / 60;
        int second = duration % 60;
        return tr("common.duration", hour, minute, second);
    }
}
