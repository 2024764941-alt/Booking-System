package com.dottstudio.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDbUser {
    public static void main(String[] args) {
        System.out.println(">>> CREATING NEW DATABASE USER (dott_app)...");

        // We use the existing DBConnection (System/Admin) to create the new user
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            System.out.println(">>> Connected as Admin. Setting up environment...");

            // Required for modern Oracle to allow simple usernames
            try {
                stmt.execute("ALTER SESSION SET \"_ORACLE_SCRIPT\"=true");
            } catch (SQLException e) {
                System.out.println("    (Warning: Could not set _ORACLE_SCRIPT, proceeding...)");
            }

            // Create User
            try {
                System.out.println(">>> Creating user 'dott_app'...");
                stmt.execute("CREATE USER dott_app IDENTIFIED BY password123");
                System.out.println("    User created.");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1920) { // ORA-01920: user name conflicts
                    System.out.println("    User 'dott_app' already exists. Resetting password...");
                    stmt.execute("ALTER USER dott_app IDENTIFIED BY password123");
                } else {
                    throw e;
                }
            }

            // Grant Permissions
            System.out.println(">>> Granting permissions...");
            stmt.execute("GRANT CONNECT, RESOURCE, DBA TO dott_app");
            stmt.execute("GRANT CREATE SESSION TO dott_app");
            stmt.execute("GRANT UNLIMITED TABLESPACE TO dott_app");

            System.out.println(">>> SUCCESS! New user 'dott_app' is ready.");
            System.out.println(">>> You can now update DBConnection.java to use: dott_app / password123");

        } catch (SQLException e) {
            System.err.println(">>> FAILED TO CREATE USER!");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());

            if (e.getErrorCode() == 1017) {
                System.err.println("\n>>> CRITICAL: AUTHENTICATION FAILED (ORA-01017)");
                System.err.println("The password for 'system' in DBConnection.java is incorrect.");
                System.err.println(
                        "Please open src/com/dottstudio/util/DBConnection.java and update the PASSWORD field.");
            }
            System.exit(1);
        }
    }
}
