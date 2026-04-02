package com.guts.Guts_IAM.utils;

import java.security.SecureRandom;

public class OtpUtil {


    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(CHAR_POOL.length());
            otp.append(CHAR_POOL.charAt(index));
        }

        return otp.toString();
    }
}
