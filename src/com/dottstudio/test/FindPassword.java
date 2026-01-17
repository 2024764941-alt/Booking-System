package com.dottstudio.test;

import com.dottstudio.util.PasswordUtils;

public class FindPassword {
    public static void main(String[] args) {
        System.out.println("Testing what password produces the stored hash...\n");

        String storedHash = "bB8UWCGb/JNGowIkU7+LiaHkloCeF4o1c6CiprXqE0M=";

        // Test common passwords and variations
        String[] testPasswords = {
                "w", "W", "ww", "www",
                "a", "A", "aa", "aaa",
                "password", "Password", "123456",
                "w gmail.com", "wgmail", "w@gmail.com"
        };

        for (String pwd : testPasswords) {
            try {
                String hash = PasswordUtils.hashPassword(pwd);
                if (hash.equals(storedHash)) {
                    System.out.println("✓ MATCH FOUND!");
                    System.out.println("  Password: \"" + pwd + "\"");
                    System.out.println("  Hash: " + hash);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error hashing: " + pwd);
            }
        }

        System.out.println("No match found in test passwords.");
        System.out.println("\nStored hash: " + storedHash);
        System.out.println("\nThis suggests the password was stored WITHOUT hashing,");
        System.out.println("or was hashed with a different algorithm/salt.");
    }
}
