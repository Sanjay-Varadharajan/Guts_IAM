package com.guts.Guts_IAM.unit_testing.common;

import com.guts.Guts_IAM.common.util.OtpUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class OtpUtilTest {

    @Test
    void testGenerateOtp_Length() {

        int length = 6;

        String otp = OtpUtil.generateOtp(length);

        assertNotNull(otp);
        assertEquals(length, otp.length());
    }

    @Test
    void testGenerateOtp_OnlyAllowedCharacters() {

        String otp = OtpUtil.generateOtp(100);

        String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        for (char c : otp.toCharArray()) {
            assertTrue(
                    allowed.indexOf(c) >= 0,
                    "Invalid character found: " + c
            );
        }
    }

    @Test
    void testGenerateOtp_IsRandom() {

        String otp1 = OtpUtil.generateOtp(8);
        String otp2 = OtpUtil.generateOtp(8);

        assertNotEquals(otp1, otp2);
    }

    @Test
    void testGenerateOtp_MinLength() {

        String otp = OtpUtil.generateOtp(1);

        assertEquals(1, otp.length());
    }
}
