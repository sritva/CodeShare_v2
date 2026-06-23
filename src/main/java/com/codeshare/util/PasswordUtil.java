package com.codeshare.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return encoder.encode(password);
    }

    public static boolean verify(String password, String hashed) {
        if (password == null || hashed == null) {
            return false;
        }
        try {
            return encoder.matches(password, hashed);
        } catch (Exception e) {
            return false;
        }
    }
}
