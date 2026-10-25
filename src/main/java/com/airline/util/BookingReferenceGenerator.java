package com.airline.util;
import java.security.SecureRandom;

public class BookingReferenceGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public static String generate() {
        StringBuilder reference = new StringBuilder("BK");

        for (int i = 0; i < 6; i++) {
            reference.append(
                    CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length()))
            );
        }

        return reference.toString();
    }
}