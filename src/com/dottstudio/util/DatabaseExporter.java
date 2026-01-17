package com.dottstudio.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseExporter {

    private static final String OUTPUT_FILE = "full_db_backup.sql";

    public static void main(String[] args) {
        System.out.println("Starting Database Export...");

        try (Connection conn = DBConnection.getConnection();
                PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_FILE))) {

            writer.println("-- DotsStudio Database Backup");
            writer.println("-- Generated automatically");
            writer.println("");

            // 1. DATA IMPORT
            writer.println("-- 1. DATA IMPORT");
            writer.println("-- Note: Verify tables match database_schema.sql");
            writer.println("");

            // Order required: ROLES -> REFERRALS -> USERS -> STAFF -> ADMINS/DESIGNERS ->
            // SPECIALTIES -> BOOKINGS
            exportTable(conn, writer, "ROLES", "RoleID");
            exportTable(conn, writer, "REFERRALS", "ReferralID");
            exportTable(conn, writer, "USERS", "UserID");
            exportTable(conn, writer, "STAFF", "StaffID");
            exportTable(conn, writer, "ADMINS", "AdminID");
            exportTable(conn, writer, "DESIGNERS", "DesignerID");
            exportTable(conn, writer, "SPECIALTIES", "SpecialtyID");
            exportTable(conn, writer, "DESIGNER_SPECIALTIES", null);
            exportTable(conn, writer, "BOOKINGS", "BookingID");
            exportTable(conn, writer, "BOOKING_DESIGNERS", null);

            System.out.println("Export Complete! File saved to: " + OUTPUT_FILE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void exportTable(Connection conn, PrintWriter writer, String tableName, String pkCol)
            throws SQLException {
        System.out.println("Exporting table: " + tableName);
        writer.println("-- Table: " + tableName);

        String sql = "SELECT * FROM " + tableName;
        if (pkCol != null)
            sql += " ORDER BY " + pkCol;

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO ").append(tableName).append(" (");

                // Columns
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1)
                        sb.append(", ");
                    sb.append(meta.getColumnName(i));
                }
                sb.append(") VALUES (");

                // Values
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1)
                        sb.append(", ");

                    Object val = rs.getObject(i);
                    if (val == null) {
                        sb.append("NULL");
                    } else if (val instanceof String || val instanceof Clob) {
                        // Handle Clob as String for simple export
                        String s = (val instanceof Clob) ? ((Clob) val).getSubString(1, (int) ((Clob) val).length())
                                : val.toString();
                        sb.append("'").append(s.replace("'", "''")).append("'");
                    } else if (val instanceof Date || val instanceof Timestamp) {
                        // Oracle To_Date format
                        // Simple: TO_DATE('2023-01-01', 'YYYY-MM-DD')
                        // We'll use simple string representation
                        sb.append("TIMESTAMP '").append(val.toString()).append("'");
                    } else {
                        sb.append(val.toString());
                    }
                }
                sb.append(");");
                writer.println(sb.toString());
            }
            writer.println("");
        } catch (SQLException e) {
            System.err
                    .println("Warning: Could not export table " + tableName + " (might not exist): " + e.getMessage());
            writer.println("-- Error processing table " + tableName + ": " + e.getMessage());
        }
    }
}
