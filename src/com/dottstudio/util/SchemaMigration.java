package com.dottstudio.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaMigration {

    public static void main(String[] args) {
        Connection conn = null;
        try {
            System.out.println("Starting Schema Migration...");
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Alter BOOKINGS Table
            System.out.println("Checking BOOKINGS table...");
            try (Statement stmt = conn.createStatement()) {
                // Check if column exists
                boolean colExists = false;
                try (ResultSet rs = stmt.executeQuery("SELECT BookingCategory FROM BOOKINGS FETCH FIRST 1 ROWS ONLY")) {
                    colExists = true;
                } catch (Exception e) {
                    // Column likely missing
                }

                if (!colExists) {
                    System.out.println("Adding BookingCategory column...");
                    stmt.execute("ALTER TABLE BOOKINGS ADD BookingCategory VARCHAR2(50)");
                    System.out.println("Column added successfully.");
                } else {
                    System.out.println("Column BookingCategory already exists.");
                }
            }

            // 2. Seed Specialties
            System.out.println("Seeding Specialties...");
            String[] categories = {
                    "Modern", "Minimalist", "Bohemian", "Industrial",
                    "Scandinavian", "Traditional", "Eclectic"
            };

            String checkSql = "SELECT SpecialtyID FROM SPECIALTIES WHERE SpecialtyName = ?";
            String insertSql = "INSERT INTO SPECIALTIES (SpecialtyName) VALUES (?)";

            for (String cat : categories) {
                try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                    check.setString(1, cat);
                    if (!check.executeQuery().next()) {
                        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                            insert.setString(1, cat);
                            insert.executeUpdate();
                            System.out.println("+ Added Specialty: " + cat);
                        }
                    } else {
                        System.out.println("= Specialty exists: " + cat);
                    }
                }
            }

            conn.commit();
            System.out.println("Migration Completed Successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception ex) {
            }
        }
    }
}
