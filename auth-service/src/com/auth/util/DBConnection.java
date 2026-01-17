package com.auth.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // PostgreSQL Configuration
    // Connecting to 'auth_db' as requested
    private static final String URL = "jdbc:postgresql://localhost:5432/booking_db"; 
    
    // Credentials - CHANGE THESE IF NEEDED
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres"; // User confirmed password is 'postgres'

    static {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println(">>> Auth Service: PostgreSQL Driver Loaded.");
        } catch (ClassNotFoundException e) {
            System.err.println(">>> Auth Service: PostgreSQL Driver NOT FOUND.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println(">>> Auth Service: Connection Successful!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
