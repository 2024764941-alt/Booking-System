package com.dottstudio.test;

import com.dottstudio.util.DBConnection;
import com.dottstudio.util.PasswordUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestPromotion {
    public static void main(String[] args) {
        System.out.println(">>> TESTING PROMOTION LOGIC...");
        String testEmail = "promote_test_" + System.currentTimeMillis() + "@example.com";
        String testPass = "password123";

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Create Test User
            System.out.println(">>> Creating test user: " + testEmail);
            String insertSql = "INSERT INTO users (email, password_hash, first_name, last_name, phone, address, role) VALUES (?, ?, 'Test', 'User', '123', 'Address', 'CUSTOMER')";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, testEmail);
                stmt.setString(2, PasswordUtils.hashPassword(testPass));
                stmt.executeUpdate();
            }

            // 2. Verify Search (Simulate DesignerSearchServlet)
            System.out.println(">>> Simulating Search for 'promote_test'...");
            boolean found = false;
            String searchSql = "SELECT email FROM users WHERE email LIKE ? AND role = 'CUSTOMER'";
            try (PreparedStatement stmt = conn.prepareStatement(searchSql)) {
                stmt.setString(1, "%promote_test%");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getString("email").equals(testEmail)) {
                            System.out.println(">>> Found user in search results.");
                            found = true;
                        }
                    }
                }
            }
            if (!found) {
                System.err.println(">>> FAILED: Search did not return the test user.");
                return;
            }

            // 3. Promote User (Simulate ApproveDesignerServlet)
            System.out.println(">>> Simulating Promotion...");

            conn.setAutoCommit(false); // Tx

            // Get ID
            int userId = -1;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                stmt.setString(1, testEmail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        userId = rs.getInt("id");
                }
            }

            if (userId == -1)
                throw new RuntimeException("User ID not found!");

            // Update Role
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET role = 'ADMIN' WHERE id = ?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            // Insert Designer
            try (PreparedStatement stmt = conn
                    .prepareStatement("INSERT INTO designers (user_id, specialty, availability) VALUES (?, ?, ?)")) {
                stmt.setInt(1, userId);
                stmt.setString(2, "Modern Test");
                stmt.setString(3, "9am-5pm");
                stmt.executeUpdate();
            }
            conn.commit();
            System.out.println(">>> Promotion Transaction Committed.");

            // 4. Verify Results
            // Check Role
            String role = "";
            try (PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE id = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        role = rs.getString("role");
                }
            }

            // Check Designer Table
            boolean inDesigners = false;
            try (PreparedStatement stmt = conn
                    .prepareStatement("SELECT cnt FROM (SELECT count(*) as cnt FROM designers WHERE user_id = ?)")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0)
                        inDesigners = true;
                }
            }

            if ("ADMIN".equals(role) && inDesigners) {
                System.out.println(">>> SUCCESS! User promoted to ADMIN and added to Designers table.");
            } else {
                System.err.println(">>> FAILURE! Role: " + role + ", In Designers: " + inDesigners);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
