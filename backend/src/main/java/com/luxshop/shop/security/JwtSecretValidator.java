package com.luxshop.shop.security;

import io.jsonwebtoken.io.Decoders;

/**
 * Validates the configured JWT signing secret so the application fails fast on a
 * missing, malformed or weak key instead of quietly signing tokens anyone can forge.
 *
 * <p>A static helper rather than a bean, so the rule can be unit-tested without
 * bootstrapping the Spring context.</p>
 */
public final class JwtSecretValidator {

    /** Minimum key length: 32 bytes = 256 bits, the floor for HMAC-SHA256. */
    public static final int MIN_SECRET_BYTES = 32;

    private JwtSecretValidator() {
    }

    /**
     * @param secret the raw {@code jwt.secret} value, Base64-encoded
     * @return the decoded key material
     * @throws IllegalStateException if the secret is blank, not valid Base64, or
     *                               shorter than {@link #MIN_SECRET_BYTES} once decoded
     */
    public static byte[] validate(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret is not configured. Set the JWT_SECRET environment variable to a "
                    + "Base64-encoded key of at least " + MIN_SECRET_BYTES + " bytes "
                    + "(e.g. `openssl rand -base64 48`).");
        }

        byte[] key;
        try {
            key = Decoders.BASE64.decode(secret.strip());
        } catch (RuntimeException e) {
            throw new IllegalStateException("jwt.secret is not valid Base64.", e);
        }

        if (key.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret is too short: " + key.length + " bytes decoded, but HMAC-SHA256 "
                    + "needs at least " + MIN_SECRET_BYTES + ". Generate one with "
                    + "`openssl rand -base64 48`.");
        }
        return key;
    }
}
