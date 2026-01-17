package com.dottstudio.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database Connection for PostgreSQL (booking_db)
    private static final String URL = "jdbc:postgresql://localhost:5432/booking_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres"; // Assuming default password or what was used in Auth Service

    // Load Driver (PostgreSQL)
    static {
        try {
            // Explicitly load the driver
            Class.forName("org.postgresql.Driver");
            System.out.println(">>> DRIVER LOADED CORRECTLY: org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println(">>> DRIVER LOADING FAILED! postgresql-42.7.8.jar is missing from WEB-INF/lib.");
            e.printStackTrace();
            throw new RuntimeException("CRITICAL ERROR: Failed to load PostgreSQL JDBC Driver!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        // System.out.println(">>> Attempting connection to: " + URL); // Optional debug, can keep if noisy
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
