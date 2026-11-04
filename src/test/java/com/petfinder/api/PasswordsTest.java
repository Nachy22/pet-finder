package com.petfinder.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordsTest {

    @Test
    void passwordIsHashedAndCorrectPasswordVerifies() {
        String encoded = Passwords.hash("SafeTesting123!");
        assertTrue(encoded.startsWith("pbkdf2-sha256$"));
        assertNotEquals("SafeTesting123!", encoded);
        assertTrue(Passwords.verify("SafeTesting123!", encoded));
        assertFalse(Passwords.verify("Incorrect123!", encoded));
    }

    @Test
    void passwordsUseDifferentSalts() {
        String one = Passwords.hash("SafeTesting123!");
        String two = Passwords.hash("SafeTesting123!");
        assertNotEquals(one, two);
    }

    @Test
    void invalidOrWeakPasswordsAreRejectedSafely() {
        assertThrows(ApiException.class, () -> Passwords.hash("weak"));
        assertThrows(ApiException.class, () -> Passwords.hash("NoNumbersHere"));
        assertThrows(ApiException.class, () -> Passwords.hash(null));
        assertFalse(Passwords.verify(null, "not-a-hash"));
        assertFalse(Passwords.verify("SafeTesting123!", "not-a-hash"));
    }

    @Test
    void apiExceptionContainsStatusCode() {
        ApiException error = new ApiException(403, "Access denied");
        assertEquals(403, error.status());
        assertEquals("Access denied", error.getMessage());
    }
}
