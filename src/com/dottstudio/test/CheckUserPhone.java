package com.dottstudio.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.dottstudio.util.DBConnection;

public class CheckUserPhone {
    public static void main(String[] args) {
        String email = "d@dottstudio.com";
        System.out.println("Checking phone number for: " + email);
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT UserID, FirstName, LastName, Phone FROM USERS WHERE Email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Found User:");
                        System.out.println("ID: " + rs.getInt("UserID"));
                        System.out.println("Name: " + rs.getString("FirstName") + " " + rs.getString("LastName"));
                        String phone = rs.getString("Phone");
                        System.out.println("Phone in DB: '" + (phone == null ? "NULL" : phone) + "'");
                    } else {
                        System.out.println("User not found in DB.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
