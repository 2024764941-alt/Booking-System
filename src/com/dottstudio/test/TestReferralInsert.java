package com.dottstudio.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.dottstudio.util.DBConnection;

public class TestReferralInsert {
    public static void main(String[] args) {
        System.out.println(">>> STARTING RECURSIVE REFERRAL TEST");

        String referrerEmail = "referrer_" + System.currentTimeMillis() + "@test.com";
        String refereeEmail = "referee_" + System.currentTimeMillis() + "@test.com";

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Create Referrer (User A)
            System.out.println(">>> Creating Referrer: " + referrerEmail);
            int referrerId = -1;
            String sqlA = "INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, RoleID) VALUES (?, 'hash', 'Referrer', 'User', 3)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlA, new String[] { "UserID" })) {
                stmt.setString(1, referrerEmail);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next())
                    referrerId = rs.getInt(1);
            }

            if (referrerId == -1) {
                System.out.println("FAILED to create referrer.");
                return;
            }
            System.out.println("    Referrer ID: " + referrerId);

            // 2. Create Referee (User B) linked to User A
            System.out.println(">>> Creating Referee linked to Referrer ID " + referrerId);
            String sqlB = "INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, RoleID, ReferredByUserID) VALUES (?, 'hash', 'Referee', 'User', 3, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlB)) {
                stmt.setString(1, refereeEmail);
                stmt.setInt(2, referrerId); // The Recursive Link
                int rows = stmt.executeUpdate();
                System.out.println("    Referee Inserted. Rows affected: " + rows);
            }

            System.out.println(">>> TEST COMPLETE: Check USERS table for correct linking.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
