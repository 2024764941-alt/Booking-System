package com.dottstudio.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminSeeder {

    public static void main(String[] args) {
        String email = "admin@dottstudio.com";
        String password = "admin"; // Default password
        String firstName = "System";
        String lastName = "Admin";

        Connection conn = null;
        try {
            System.out.println("Connecting to database...");
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Check if exists
            try (PreparedStatement check = conn.prepareStatement("SELECT UserID FROM USERS WHERE Email = ?")) {
                check.setString(1, email);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    System.out.println("Admin user already exists (UserID: " + rs.getInt(1) + ")");
                    return;
                }
            }

            // 2. Insert User
            System.out.println("Creating User...");
            String passwordHash = PasswordUtils.hashPassword(password);
            String userSql = "INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, RoleID) VALUES (?, ?, ?, ?, 1)";

            int userId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(userSql, new String[] { "USERID" })) {
                stmt.setString(1, email);
                stmt.setString(2, passwordHash);
                stmt.setString(3, firstName);
                stmt.setString(4, lastName);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        userId = rs.getInt(1);
                }
            }

            if (userId == -1) {
                // Fallback fetch if generated keys fail (rare in Oracle JDBC sometimes)
                try (PreparedStatement p = conn.prepareStatement("SELECT UserID FROM USERS WHERE Email = ?")) {
                    p.setString(1, email);
                    ResultSet rs = p.executeQuery();
                    if (rs.next())
                        userId = rs.getInt(1);
                }
            }
            System.out.println("User Created with ID: " + userId);

            // 3. Insert Staff
            System.out.println("Creating Staff Entry...");
            String staffSql = "INSERT INTO STAFF (UserID, Position) VALUES (?, 'System Administrator')";
            int staffId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(staffSql, new String[] { "STAFFID" })) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        staffId = rs.getInt(1);
                }
            }

            if (staffId == -1) {
                try (PreparedStatement p = conn.prepareStatement("SELECT StaffID FROM STAFF WHERE UserID = ?")) {
                    p.setInt(1, userId);
                    ResultSet rs = p.executeQuery();
                    if (rs.next())
                        staffId = rs.getInt(1);
                }
            }
            System.out.println("Staff Created with ID: " + staffId);

            // 4. Insert Admin
            System.out.println("Creating Admin Entry...");
            String adminSql = "INSERT INTO ADMINS (StaffID, Department) VALUES (?, 'IT')";
            try (PreparedStatement stmt = conn.prepareStatement(adminSql)) {
                stmt.setInt(1, staffId);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("SUCCESS! Admin created.");
            System.out.println("Email: " + email);
            System.out.println("Password: " + password);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception ex) {
            }
        }
    }
}
