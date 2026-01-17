package com.dottstudio.util;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaReverter {
    public static void main(String[] args) {
        System.out.println("Starting Schema Revert (Dropping Columns)...");

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);

            try (Statement stmt = conn.createStatement()) {
                System.out.println("Dropping ClientName column...");
                try {
                    stmt.executeUpdate("ALTER TABLE BOOKINGS DROP COLUMN ClientName");
                } catch (Exception e) {
                    System.out.println("Error dropping ClientName (might not exist): " + e.getMessage());
                }

                System.out.println("Dropping DesignerName column...");
                try {
                    stmt.executeUpdate("ALTER TABLE BOOKINGS DROP COLUMN DesignerName");
                } catch (Exception e) {
                    System.out.println("Error dropping DesignerName (might not exist): " + e.getMessage());
                }
            }

            System.out.println("Schema Revert Complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
