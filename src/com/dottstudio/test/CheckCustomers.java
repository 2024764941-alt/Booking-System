package com.dottstudio.test;

import com.dottstudio.util.DBConnection;
import java.sql.*;

public class CheckCustomers {
    public static void main(String[] args) {
        System.out.println("Checking all CUSTOMER accounts in database...\n");

        String sql = "SELECT u.UserID, u.Email, u.FirstName, u.LastName, u.PasswordHash, r.RoleName " +
                "FROM USERS u " +
                "JOIN ROLES r ON u.RoleID = r.RoleID " +
                "WHERE r.RoleName = 'CUSTOMER' " +
                "ORDER BY u.UserID";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("Customer #" + count);
                System.out.println("  UserID: " + rs.getInt("UserID"));
                System.out.println("  Email: " + rs.getString("Email"));
                System.out.println("  Name: " + rs.getString("FirstName") + " " + rs.getString("LastName"));
                System.out.println("  Password Hash: " + rs.getString("PasswordHash"));
                System.out.println("  Role: " + rs.getString("RoleName"));
                System.out.println();
            }

            if (count == 0) {
                System.out.println("No customers found in database!");
            } else {
                System.out.println("Total customers: " + count);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
