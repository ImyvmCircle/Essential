package com.imyvm.essential.util;

import java.util.Random;

public class RandomUtil {
    public static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final Random RANDOM = new Random();

    private RandomUtil() {
    }

    public static String getRandomString(int length, String alphabet) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        return builder.toString();
    }

    public static String getRandomString(int length) {
        return getRandomString(length, ALPHABET);
    }
}
