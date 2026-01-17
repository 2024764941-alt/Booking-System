package com.dottstudio.test;

import com.dottstudio.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestSearch {
    public static void main(String[] args) {
        System.out.println("Testing Search Logic...");
        String term = "ja";

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.Email, r.RoleName " +
                    "FROM USERS u " +
                    "JOIN ROLES r ON u.RoleID = r.RoleID " +
                    "WHERE LOWER(u.Email) LIKE LOWER(?) AND r.RoleName = 'CUSTOMER'";

            System.out.println("Executing Query: " + sql);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + term + "%");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Found: " + rs.getString("Email") + " | Role: " + rs.getString("RoleName"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
