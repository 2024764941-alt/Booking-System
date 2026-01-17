package com.dottstudio.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckSchedule {
    public static void main(String[] args) {
        System.out.println("Checking DESIGNER_SCHEDULE table...");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM DESIGNER_SCHEDULE";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        System.out.println("Row: ID=" + rs.getInt("ScheduleID") + 
                                         ", DesignerID=" + rs.getInt("DesignerID") +
                                         ", Date=" + rs.getDate("AvailableDate") +
                                         ", IsAvailable=" + rs.getInt("IsAvailable"));
                    }
                    if (!found) System.out.println("Table is EMPTY.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
