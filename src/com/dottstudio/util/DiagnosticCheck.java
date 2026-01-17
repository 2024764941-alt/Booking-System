package com.dottstudio.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DiagnosticCheck {
    public static void main(String[] args) {
        System.out.println("=== DIAGNOSTIC CHECK ===\n");

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("✓ Database connection successful\n");

            // Check which tables exist
            System.out.println("--- Tables in Database ---");
            String tablesSql = "SELECT table_name FROM user_tables WHERE table_name IN ('USERS', 'STAFF', 'ADMINS', 'DESIGNERS') ORDER BY table_name";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(tablesSql)) {
                while (rs.next()) {
                    System.out.println("  ✓ " + rs.getString("table_name"));
                }
            }

            // Check if ADMINS table exists
            String adminCheckSql = "SELECT COUNT(*) as cnt FROM user_tables WHERE table_name = 'ADMINS'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(adminCheckSql)) {
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    if (count > 0) {
                        System.out.println("\n⚠️  WARNING: ADMINS table exists (OLD SCHEMA)");
                    } else {
                        System.out.println("\n✓ ADMINS table does not exist (NEW SCHEMA)");
                    }
                }
            }

            // Check STAFF table structure
            System.out.println("\n--- STAFF Table Columns ---");
            String staffColsSql = "SELECT column_name FROM user_tab_columns WHERE table_name = 'STAFF' ORDER BY column_id";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(staffColsSql)) {
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("column_name"));
                }
            }

            // Check admin user
            System.out.println("\n--- Admin User Check ---");
            String adminUserSql = "SELECT u.UserID, u.Email, u.FirstName, u.LastName, r.RoleName " +
                    "FROM USERS u JOIN ROLES r ON u.RoleID = r.RoleID " +
                    "WHERE u.Email = 'admin@dottstudio.com'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(adminUserSql)) {
                if (rs.next()) {
                    System.out.println("  ✓ Admin user found:");
                    System.out.println("    UserID: " + rs.getInt("UserID"));
                    System.out.println("    Email: " + rs.getString("Email"));
                    System.out.println("    Name: " + rs.getString("FirstName") + " " + rs.getString("LastName"));
                    System.out.println("    Role: " + rs.getString("RoleName"));

                    int userId = rs.getInt("UserID");

                    // Check if staff record exists
                    String staffCheckSql = "SELECT StaffID, StaffType, Department FROM STAFF WHERE UserID = " + userId;
                    try (Statement stmt2 = conn.createStatement(); ResultSet rs2 = stmt2.executeQuery(staffCheckSql)) {
                        if (rs2.next()) {
                            System.out.println("\n  ✓ Staff record found:");
                            System.out.println("    StaffID: " + rs2.getInt("StaffID"));
                            System.out.println("    StaffType: " + rs2.getString("StaffType"));
                            System.out.println("    Department: " + rs2.getString("Department"));
                        } else {
                            System.out.println("\n  ✗ ERROR: No staff record for this admin user!");
                        }
                    }
                } else {
                    System.out.println("  ✗ ERROR: Admin user not found!");
                }
            }

            System.out.println("\n=== END DIAGNOSTIC ===");

        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
