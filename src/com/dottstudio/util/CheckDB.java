package com.dottstudio.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CheckDB {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@localhost:1521/free";
        String user = "dott_app";
        String pass = "password123";

        System.out.println("--------------------------------------------------");
        System.out.println("Testing Connection for user: " + user);
        System.out.println("URL: " + url);
        System.out.println("--------------------------------------------------");

        try {
            Class.forName("oracle.jdbc.OracleDriver");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                System.out.println("SUCCESS! Connected to database.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: Oracle Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("FAILURE! Could not connect.");
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
        }
        System.out.println("--------------------------------------------------");
    }
}
