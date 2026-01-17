package com.dottstudio.test;

import com.dottstudio.util.DBConnection;
import com.dottstudio.util.PasswordUtils;
import java.sql.*;

public class ResetCustomerPasswords {
    public static void main(String[] args) {
        System.out.println("Resetting customer passwords to 'password123'...\n");

        try {
            String newPassword = "password123";
            String hashedPassword = PasswordUtils.hashPassword(newPassword);

            System.out.println("New password hash: " + hashedPassword);

            String sql = "UPDATE USERS SET PasswordHash = ? " +
                    "WHERE UserID IN (SELECT UserID FROM USERS u JOIN ROLES r ON u.RoleID = r.RoleID WHERE r.RoleName = 'CUSTOMER')";

            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, hashedPassword);
                int count = stmt.executeUpdate();

                System.out.println("\n✓ Updated " + count + " customer accounts");
                System.out.println("All customers can now login with password: password123");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
