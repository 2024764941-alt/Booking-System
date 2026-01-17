package com.dottstudio.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.dottstudio.util.DBConnection;
import com.dottstudio.util.PasswordUtils;

public class TestRegistration {
    public static void main(String[] args) {
        System.out.println(">>> TESTING REGISTRATION INSERT...");
        String email = "test_reg_" + System.currentTimeMillis() + "@example.com";
        String password = "password123";
        String firstName = "Test";
        String lastName = "User";
        String phone = "1234567890";
        String address = "123 Test Lane";
        String referral = "Google";

        String hashedPassword = PasswordUtils.hashPassword(password);

        String sql = "INSERT INTO users (email, password_hash, first_name, last_name, phone, address, referral_source) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, phone);
            stmt.setString(6, address);
            stmt.setString(7, referral);

            int rows = stmt.executeUpdate();
            System.out.println(">>> SUCCESS! Rows inserted: " + rows);
            System.out.println(">>> Created user: " + email);

        } catch (Exception e) {
            System.err.println(">>> FAILURE! Registration Failed.");
            e.printStackTrace();
        }
    }
}
