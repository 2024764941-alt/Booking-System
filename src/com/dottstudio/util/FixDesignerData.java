package com.dottstudio.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FixDesignerData {

    public static void main(String[] args) {
        System.out.println("Beginning data fix for Bio/Specialty mismatch...");
        try (Connection conn = DBConnection.getConnection()) {

            // 1. Identify rows to fix
            String selectSql = "SELECT StaffID, Bio, Specialty FROM DESIGNERS";
            List<Integer> toFix = new ArrayList<>();
            List<String> newSpecialties = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(selectSql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("StaffID");
                    String bio = rs.getString("Bio");
                    String specialty = rs.getString("Specialty");
                    System.out.println("Row: ID=" + id + " Bio=[" + bio + "] Specialty=[" + specialty + "]");

                    // Logic: If Specialty is null/empty AND Bio looks like a specialty (short)
                    boolean specialtyEmpty = (specialty == null || specialty.trim().isEmpty() || specialty.equals("-"));

                    if (specialtyEmpty && bio != null && !bio.trim().isEmpty()) {
                        // Check if bio is short enough to trigger valid swap
                        // "modern", "classic" are very short. Real bios are usually longer.
                        if (bio.length() < 30) {
                            System.out.println(
                                    "Found candidate: ID=" + id + ", Bio='" + bio + "', Specialty='" + specialty + "'");
                            toFix.add(id);
                            newSpecialties.add(bio);
                        }
                    }
                }
            }

            if (toFix.isEmpty()) {
                System.out.println("No records found requiring fix.");
                return;
            }

            // 2. Perform updates
            String updateSql = "UPDATE DESIGNERS SET Specialty = ?, Bio = NULL WHERE StaffID = ?";
            try (PreparedStatement uStmt = conn.prepareStatement(updateSql)) {
                for (int i = 0; i < toFix.size(); i++) {
                    uStmt.setString(1, newSpecialties.get(i));
                    uStmt.setInt(2, toFix.get(i));
                    uStmt.addBatch();
                }
                int[] results = uStmt.executeBatch();
                System.out.println("Successfully updated " + results.length + " records.");
            }

            // conn.commit(); // Auto-commit is usually true
            System.out.println("Data fix complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
