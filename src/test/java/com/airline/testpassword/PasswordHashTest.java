package com.airline.testpassword;

import com.airline.util.security.PasswordHasher;
import org.junit.jupiter.api.Test;

public class PasswordHashTest {

    @Test
    void generatePasswordHash() {

        String password = "thisisthepass";

        String hash =
                PasswordHasher.hashPassword(password);

        System.out.println("Password hash:");
        System.out.println(hash);
    }
}
