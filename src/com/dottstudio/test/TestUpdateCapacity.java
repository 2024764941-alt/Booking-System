package com.dottstudio.test;

import com.dottstudio.util.UserDAO;
import com.dottstudio.util.DBConnection;
import java.sql.*;

public class TestUpdateCapacity {
    public static void main(String[] args) {
        // Test with StaffID 8 (Harry)
        int testStaffId = 8;
        int targetCapacity = 3;

        try {
            System.out.println("--- Starting Capacity Update Test ---");

            // 1. Check Initial State
            int currentCap = getCapacity(testStaffId);
            System.out.println("Initial Capacity: " + currentCap);

            // 2. Perform Update
            System.out.println("Attempting update to: " + targetCapacity);
            boolean success = UserDAO.updateStaff(testStaffId, "Harry", "Daniell", "1234567890",
                    "Modern", "Bio...", 40, 0, targetCapacity, 20, "Active");
            System.out.println("Update Status: " + success);

            // 3. Verify Result
            int newCap = getCapacity(testStaffId);
            System.out.println("New Capacity: " + newCap);

            if (newCap == targetCapacity) {
                System.out.println("TEST PASSED: Capacity updated successfully.");
            } else {
                System.out.println("TEST FAILED: Capacity did not change.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getCapacity(int staffId) {
        String sql = "SELECT MaxSimultaneousProjects FROM DESIGNERS WHERE StaffID = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    System.out.println("No Designer record found for StaffID " + staffId);
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -999;
        }
    }
}
