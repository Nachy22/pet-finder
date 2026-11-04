package com.petfinder.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2: salted, intentionally slow password hashing, not reversible encryption. */
public final class Passwords {
    private static final int ITERATIONS = 310_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Passwords() { }

    public static String hash(String password) {
        if (password == null || password.length() < 8 ||
                !password.matches("(?s).*\\d.*") || password.length() > 256) {
            throw new ApiException(400, "Password must be 8-256 characters and contain a number");
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hashed = derive(password, salt, ITERATIONS);
        return "pbkdf2-sha256$" + ITERATIONS + "$"
            + Base64.getEncoder().encodeToString(salt) + "$"
            + Base64.getEncoder().encodeToString(hashed);
    }

    public static boolean verify(String password, String encoded) {
        if (password == null || encoded == null || password.length() > 256) return false;
        try {
            String[] parts = encoded.split("\\$");
            if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) return false;
            int rounds = Integer.parseInt(parts[1]);
            if (rounds < 100_000 || rounds > 1_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, derive(password, salt, rounds));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int rounds) {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, rounds, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded();
        } catch (Exception error) {
            throw new IllegalStateException("Password hashing unavailable", error);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(chars, '\0');
        }
    }
}
