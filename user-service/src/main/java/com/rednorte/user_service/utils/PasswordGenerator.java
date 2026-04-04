package com.rednorte.user_service.utils;

import java.util.Random;

public class PasswordGenerator {

    private static final String[] WORDS = {
        "sol", "luna", "mar", "nube", "roca",
        "fuego", "tierra", "viento", "rio", "cielo"
    };

    private static final Random RANDOM = new Random();

    public static String generate() {
        return WORDS[RANDOM.nextInt(WORDS.length)] + "-" +
               RANDOM.nextInt(100) + "-" +
               WORDS[RANDOM.nextInt(WORDS.length)] + "-" +
               RANDOM.nextInt(100);
    }
}