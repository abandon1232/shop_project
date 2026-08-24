package com.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    private final PasswordService passwordService =
            new PasswordService(new BCryptPasswordEncoder());

    @Test
    void encodesAndMatchesPasswordWithBcrypt() {
        String rawPassword = "correct-horse-battery-staple";

        String encodedPassword = passwordService.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordService.matches(rawPassword, encodedPassword));
        assertFalse(passwordService.matches("wrong-password", encodedPassword));
        assertFalse(passwordService.needsUpgrade(encodedPassword));
    }

    @Test
    void acceptsLegacyPlaintextOnlyForMigration() {
        assertTrue(passwordService.matches("legacy-password", "legacy-password"));
        assertFalse(passwordService.matches("wrong-password", "legacy-password"));
        assertTrue(passwordService.needsUpgrade("legacy-password"));
    }
}
