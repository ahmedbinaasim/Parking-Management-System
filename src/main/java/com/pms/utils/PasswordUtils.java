package com.pms.utils;

public class PasswordUtils {

    public static String hashPassword(String password) {
        // Return password as-is (no hashing)
        return password;
    }

    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        // Directly compare plain-text passwords
        return plainPassword.equals(storedPassword);
    }
}
