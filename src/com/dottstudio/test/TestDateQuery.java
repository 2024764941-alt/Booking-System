package com.dottstudio.test;

import com.dottstudio.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestDateQuery {
    public static void main(String[] args) {
        System.out.println("Testing Date Query Logic...");

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Insert/Reset Data for Today (Safe ID 4 used before)
            int dId = 4;
            String insert = "MERGE INTO DESIGNER_SCHEDULE d USING (SELECT ? as Did, TRUNC(SYSDATE) as dt FROM dual) s "
                    +
                    "ON (d.DesignerID = s.Did AND d.AvailableDate = s.dt) " +
                    "WHEN MATCHED THEN UPDATE SET IsAvailable = 1 " +
                    "WHEN NOT MATCHED THEN INSERT (DesignerID, AvailableDate, IsAvailable) VALUES (?, TRUNC(SYSDATE), 1)";

            try (PreparedStatement u = conn.prepareStatement(insert)) {
                u.setInt(1, dId);
                u.setInt(2, dId);
                int rows = u.executeUpdate();
                System.out.println("Upserted Today's Record using TRUNC(SYSDATE): " + rows);
            }
            // conn.commit(); // Auto-commit is likely on, skipping explicit commit to avoid
            // ORA-17273

            // 2. Run the Servlet Query
            System.out.println("\n--- Running Servlet Query ---");
            // The exact query from DesignersListServlet
            String sql = "SELECT AvailableDate FROM (SELECT DISTINCT AvailableDate FROM DESIGNER_SCHEDULE " +
                    "WHERE DesignerID = ? AND AvailableDate >= TRUNC(SYSDATE) AND IsAvailable = 1 " +
                    "ORDER BY AvailableDate ASC) WHERE ROWNUM <= 3";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, dId);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        System.out.println("Found Date: " + rs.getDate("AvailableDate"));
                        found = true;
                    }
                    if (!found)
                        System.out.println("NO DATES FOUND matching query.");
                }
            }

            // 3. Check what is actually in the table for this ID
            System.out.println("\n--- Dumping Table for ID " + dId + " ---");
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT AvailableDate, IsAvailable FROM DESIGNER_SCHEDULE WHERE DesignerID = ? ORDER BY AvailableDate DESC FETCH NEXT 5 ROWS ONLY")) {
                stmt.setInt(1, dId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(
                                "Date: " + rs.getDate("AvailableDate") + " | IsAvailable: " + rs.getInt("IsAvailable"));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
