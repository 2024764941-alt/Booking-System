package com.dottstudio.test;

import java.sql.*;
import com.dottstudio.util.DBConnection;

public class CheckUserName {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            // Check what's in the database for harry@dottstudio.com
            String sql = "SELECT u.UserID, u.Email, u.FirstName, u.LastName, u.Phone " +
                        "FROM USERS u WHERE u.Email = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "harry@dottstudio.com");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("=== CURRENT DATABASE VALUES ===");
                        System.out.println("UserID: " + rs.getInt("UserID"));
                        System.out.println("Email: " + rs.getString("Email"));
                        System.out.println("FirstName: '" + rs.getString("FirstName") + "'");
                        System.out.println("LastName: '" + rs.getString("LastName") + "'");
                        System.out.println("Phone: " + rs.getString("Phone"));
                        
                        // Now UPDATE it to the correct name
                        String updateSql = "UPDATE USERS SET FirstName = ?, LastName = ? WHERE Email = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, "harry");
                            updateStmt.setString(2, "daniell");
                            updateStmt.setString(3, "harry@dottstudio.com");
                            
                            int rows = updateStmt.executeUpdate();
                            System.out.println("\n=== UPDATE RESULT ===");
                            System.out.println("Rows updated: " + rows);
                            
                            if (rows > 0) {
                                System.out.println("✅ Successfully updated name to 'harry daniell'");
                            }
                        }
                    } else {
                        System.out.println("❌ User not found with email: harry@dottstudio.com");
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
