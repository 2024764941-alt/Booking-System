package com.dottstudio.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaUpdater {
    public static void main(String[] args) {
        System.out.println("Starting Schema Update...");

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);

            // 1. Add Columns
            try (Statement stmt = conn.createStatement()) {
                System.out.println("Adding columns (ClientName, DesignerName) to BOOKINGS...");
                // Note: Providing default NULL. Oracle 12c+ supports IF NOT EXISTS syntax but
                // strictly speaking standard SQL throws generic error if exists.
                // We'll try/catch to ignore 'column already exists' error (ORA-01430)
                try {
                    stmt.executeUpdate("ALTER TABLE BOOKINGS ADD (ClientName VARCHAR(100), DesignerName VARCHAR(100))");
                    System.out.println("Columns added.");
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1430) {
                        System.out.println("Columns already exist. Skipping add.");
                    } else {
                        throw e;
                    }
                }
            }

            // 2. Backfill ClientName
            System.out.println("Backfilling ClientName...");
            String updateClient = "UPDATE BOOKINGS b SET ClientName = (" +
                    "SELECT u.FirstName || ' ' || u.LastName FROM USERS u WHERE u.UserID = b.CustomerID" +
                    ")";
            try (PreparedStatement start = conn.prepareStatement(updateClient)) {
                int rows = start.executeUpdate();
                System.out.println("Updated " + rows + " rows with ClientName.");
            }

            // 3. Backfill DesignerName
            // Note: Fetches ONE designer if multiple.
            System.out.println("Backfilling DesignerName...");
            String updateDesigner = "UPDATE BOOKINGS b SET DesignerName = (" +
                    "SELECT MAX(u.FirstName || ' ' || u.LastName) " + // MAX to pick one if dupes, aggregate required
                                                                      // for subquery returning 1 value
                    "FROM BOOKING_DESIGNERS bd " +
                    "JOIN DESIGNERS d ON bd.DesignerID = d.DesignerID " +
                    "JOIN STAFF s ON d.StaffID = s.StaffID " +
                    "JOIN USERS u ON s.UserID = u.UserID " +
                    "WHERE bd.BookingID = b.BookingID" +
                    ")";
            try (PreparedStatement start = conn.prepareStatement(updateDesigner)) {
                int rows = start.executeUpdate();
                System.out.println("Updated " + rows + " rows with DesignerName.");
            }

            System.out.println("Schema Update Complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
