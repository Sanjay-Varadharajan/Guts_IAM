package com.guts.Guts_IAM.unit_testing.security;

import com.guts.Guts_IAM.security.util.hashutil.HashUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilTest {

    @Test
    void shouldReturnSameHashForSameInput() {

        String input = "hello";

        String hash1 = HashUtil.sha256(input);
        String hash2 = HashUtil.sha256(input);

        assertEquals(hash1, hash2);
    }

    @Test
    void shouldReturnDifferentHashForDifferentInputs() {

        String hash1 = HashUtil.sha256("hello");
        String hash2 = HashUtil.sha256("world");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void shouldReturnValidSha256Length() {

        String hash = HashUtil.sha256("test");

        // SHA-256 = 256 bits = 64 hex chars
        assertEquals(64, hash.length());
    }

    @Test
    void shouldThrowExceptionForNullInput() {

        assertThrows(RuntimeException.class, () -> {
            HashUtil.sha256(null);
        });
    }
}