package com.auth.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database connection details (will be overridden by Heroku's DATABASE_URL if available)
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    // Load Driver and parse DATABASE_URL
    static {
        try {
            // Explicitly load the driver
            Class.forName("org.postgresql.Driver");
            System.out.println(">>> Auth Service: PostgreSQL Driver Loaded.");
            
            // Parse DATABASE_URL from environment (Heroku) or use localhost defaults
            String databaseUrl = System.getenv("DATABASE_URL");
            
            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                // Heroku provides DATABASE_URL in format: postgres://user:password@host:port/database
                System.out.println(">>> Auth Service: Using Heroku DATABASE_URL");
                URI dbUri = new URI(databaseUrl);
                
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort();
                String database = dbUri.getPath().substring(1); // Remove leading '/'
                
                // Construct JDBC URL
                dbUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
                dbUser = username;
                dbPassword = password;
                
                System.out.println(">>> Auth Service: Connected to Heroku Postgres: " + host);
            } else {
                // Local development fallback
                System.out.println(">>> Auth Service: Using localhost PostgreSQL (local development)");
                dbUrl = "jdbc:postgresql://localhost:5432/booking_db";
                dbUser = "postgres";
                dbPassword = "postgres";
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println(">>> Auth Service: PostgreSQL Driver NOT FOUND.");
            e.printStackTrace();
            throw new RuntimeException("CRITICAL ERROR: Failed to load PostgreSQL JDBC Driver!", e);
        } catch (URISyntaxException e) {
            System.err.println(">>> Auth Service: DATABASE_URL parsing failed! Using localhost fallback.");
            e.printStackTrace();
            // Fallback to localhost
            dbUrl = "jdbc:postgresql://localhost:5432/booking_db";
            dbUser = "postgres";
            dbPassword = "postgres";
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
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
