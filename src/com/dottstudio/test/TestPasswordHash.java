package com.dottstudio.test;

import com.dottstudio.util.PasswordUtils;

public class TestPasswordHash {
    public static void main(String[] args) {
        String raw = "admin123";
        String hash = PasswordUtils.hashPassword(raw);
        System.out.println("Raw: " + raw);
        System.out.println("Hash: " + hash);

        System.out.println("Check: " + PasswordUtils.checkPassword(raw, hash));
    }
}
