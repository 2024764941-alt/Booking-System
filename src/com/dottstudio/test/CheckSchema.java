package com.dottstudio.test;

import java.sql.*;
import com.dottstudio.util.DBConnection;

public class CheckSchema {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "BOOKINGS", "%");
            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME");
                String typeName = rs.getString("TYPE_NAME");
                System.out.println("Column: " + colName + " | Type: " + typeName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
