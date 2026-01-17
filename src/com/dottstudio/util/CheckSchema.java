package com.dottstudio.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckSchema {
    public static void main(String[] args) {
        System.out.println(">>> CHECKING DATABASE SCHEMA...");
        try (Connection conn = DBConnection.getConnection()) {
            checkTable(conn, "USERS");
            checkTable(conn, "STAFF");
            checkTable(conn, "ADMINS");
            checkTable(conn, "DESIGNERS");
            checkTable(conn, "REFERRALS");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void checkTable(Connection conn, String tableName) throws SQLException {
        System.out.println("--- Table: " + tableName + " ---");
        try (Statement stmt = conn.createStatement()) {
            // This query works for Oracle to list columns
            // user_tab_columns view describes columns for tables owned by the user
            String sql = "SELECT column_name, data_type, data_length, nullable " +
                    "FROM user_tab_columns " +
                    "WHERE table_name = '" + tableName + "'";
            try (ResultSet rs = stmt.executeQuery(sql)) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    String col = rs.getString("column_name");
                    String type = rs.getString("data_type");
                    String nullalbe = rs.getString("nullable");
                    System.out.println("   " + col + " (" + type + ", " + nullalbe + ")");
                }
                if (!found) {
                    System.out.println("   (Table not found or no columns)");
                }
            }
        }
    }
}
