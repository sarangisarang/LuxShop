package com.luxshop;

import com.luxshop.shop.security.JwtSecretValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The signing secret is the whole of the auth guarantee: anyone who knows it can
 * mint a token for any user. These tests pin the rule that a missing or weak
 * secret stops the application instead of being silently accepted.
 */
class JwtSecretValidatorTest {

    private static String base64Of(int bytes) {
        return Base64.getEncoder().encodeToString(new byte[bytes]);
    }

    @Test
    @DisplayName("a secret of at least 32 bytes is accepted and decoded")
    void acceptsStrongSecret() {
        byte[] key = JwtSecretValidator.validate(base64Of(48));
        assertEquals(48, key.length);
    }

    @Test
    @DisplayName("exactly 32 bytes is the boundary and is accepted")
    void acceptsMinimumLength() {
        assertEquals(32, JwtSecretValidator.validate(base64Of(32)).length);
    }

    @Test
    @DisplayName("null is rejected — an unset JWT_SECRET must not boot the app")
    void rejectsNull() {
        assertThrows(IllegalStateException.class, () -> JwtSecretValidator.validate(null));
    }

    @Test
    @DisplayName("blank is rejected — an empty env var is not a secret")
    void rejectsBlank() {
        assertThrows(IllegalStateException.class, () -> JwtSecretValidator.validate("   "));
    }

    @Test
    @DisplayName("a secret shorter than 32 bytes is rejected with a usable message")
    void rejectsShortSecret() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> JwtSecretValidator.validate(base64Of(31)));
        assertTrue(e.getMessage().contains("too short"), e.getMessage());
    }

    @Test
    @DisplayName("surrounding whitespace does not make a valid secret look short")
    void tolerantOfWhitespace() {
        assertEquals(48, JwtSecretValidator.validate("  " + base64Of(48) + "\n").length);
    }
}
