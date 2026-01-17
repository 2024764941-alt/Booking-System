package com.dottstudio.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.dottstudio.util.DBConnection;
import com.dottstudio.util.PasswordUtils;

public class TestLogin {
    public static void main(String[] args) {
        System.out.println(">>> TESTING LOGIN LOGIC...");

        // 1. Fetch the most recently created user
        String sql = "SELECT email, password_hash FROM users WHERE email LIKE 'test_reg%' ORDER BY email DESC FETCH FIRST 1 ROWS ONLY";
        // Note: ORDER BY email DESC is a rough proxy for "latest" if we don't have
        // created_at,
        // but seeing as our emails have timestamps (test_reg_TIMESTAMP), this works.

        String email = null;
        String storedHash = null;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                email = rs.getString("email");
                storedHash = rs.getString("password_hash");
                System.out.println(">>> Found User: " + email);
                System.out.println(">>> Stored Hash: " + storedHash);
            } else {
                System.out.println(">>> NO USERS FOUND IN DATABASE!");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 2. Test Password
        String testPassword = "password123";
        System.out.println(">>> Testing password: '" + testPassword + "'");

        boolean matches = PasswordUtils.checkPassword(testPassword, storedHash);

        if (matches) {
            System.out.println(">>> SUCCESS! Password matches.");
        } else {
            System.out.println(">>> FAILURE! Password does NOT match.");
            // Debug: Show what the hash SHOULD be
            String expectedHash = PasswordUtils.hashPassword(testPassword);
            System.out.println(">>> Expected Hash for '" + testPassword + "': " + expectedHash);
        }
    }
}
