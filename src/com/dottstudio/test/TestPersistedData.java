package com.dottstudio.test;

import com.dottstudio.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestPersistedData {
    public static void main(String[] args) {
        System.out.println("Checking Persisted Data...");

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Check DESIGNERS Table for AvailableHours
            System.out.println("\n--- DESIGNERS TABLE (AvailableHours) ---");
            String sql = "SELECT DesignerID, AvailableHours, MaxSimultaneousProjects FROM DESIGNERS";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("ID: " + rs.getInt("DesignerID"));
                        System.out.println("MaxProjects: " + rs.getInt("MaxSimultaneousProjects"));
                        System.out.println("AvailableHours (Raw JSON): " + rs.getString("AvailableHours"));
                        System.out.println("--------------------------------");
                    }
                }
            }

            // 2. Check DESIGNER_SCHEDULE Table
            System.out.println("\n--- DESIGNER_SCHEDULE TABLE ---");
            String sql2 = "SELECT DesignerID, AvailableDate, IsAvailable FROM DESIGNER_SCHEDULE ORDER BY AvailableDate DESC FETCH NEXT 10 ROWS ONLY";
            // Native query might fail if FETCH NEXT not supported, fallback to simple
            // select
            try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("DesignerID: " + rs.getInt("DesignerID") +
                                " | Date: " + rs.getDate("AvailableDate") +
                                " | Avail: " + rs.getString("IsAvailable"));
                    }
                }
            } catch (Exception e) {
                System.out.println("Fetch query failed, trying standard: " + e.getMessage());
                String sql3 = "SELECT DesignerID, AvailableDate, IsAvailable FROM DESIGNER_SCHEDULE";
                try (PreparedStatement stmt = conn.prepareStatement(sql3); ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("DesignerID: " + rs.getInt("DesignerID") +
                                " | Date: " + rs.getDate("AvailableDate") +
                                " | Avail: " + rs.getString("IsAvailable"));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
