package com.gate.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 60000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    // =========================================================
    // HASH
    // =========================================================

    public static String hash(String password) {

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password, salt, ITERATIONS);

        return PREFIX + ":" + ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    // =========================================================
    // VERIFY
    // =========================================================

    /**
     * Verifies a password against a stored value.
     * Supports legacy plaintext rows (anything not in the
     * "pbkdf2:..." format) so old accounts keep working.
     */
    public static boolean verify(String password,
                                 String stored) {

        if (password == null || stored == null) {
            return false;
        }

        if (!stored.startsWith(PREFIX + ":")) {

            // Legacy plaintext entry
            return constantTimeEquals(
                    password.getBytes(),
                    stored.getBytes());
        }

        String[] parts = stored.split(":");

        if (parts.length != 4) {
            return false;
        }

        try {

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);

            byte[] actual = pbkdf2(password, salt, iterations);

            return constantTimeEquals(actual, expected);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * True when the stored value is already a hash
     * (i.e. no migration needed).
     */
    public static boolean isHashed(String stored) {
        return stored != null
                && stored.startsWith(PREFIX + ":");
    }

    // =========================================================
    // INTERNALS
    // =========================================================

    private static byte[] pbkdf2(String password,
                                 byte[] salt,
                                 int iterations) {

        try {

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    iterations,
                    KEY_BITS);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Password hashing failed", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a,
                                              byte[] b) {

        return MessageDigest.isEqual(a, b);
    }
}
