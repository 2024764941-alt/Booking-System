package com.dottstudio.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtils {

    /**
     * Hashes a password using SHA-256 with a simple salt.
     * Note: In production, use a library like BCrypt (e.g., maintain 'spring-security-crypto') 
     * but this uses standard Java libraries as requested for simplicity without external dependencies if not available.
     */
    public static String hashPassword(String password) {
        String salt = "DottStudioSalt"; // In real apps, generate unique salt per user
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not defined", e);
        }
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return hashPassword(plainPassword).equals(hashedPassword);
    }
}
