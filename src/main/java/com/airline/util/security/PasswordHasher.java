package com.airline.util.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(
            String password,
            String passwordHash) {

        return BCrypt.checkpw(password, passwordHash);
    }
}