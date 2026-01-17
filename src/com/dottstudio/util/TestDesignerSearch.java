package com.dottstudio.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TestDesignerSearch {
    public static void main(String[] args) {
        // Simulate inputs
        String category = "Modern,Industrial";

        System.out.println("Testing Search with category: " + category);

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder();

            // Base Select (Simplified)
            String baseSelect = "SELECT d.DesignerID, d.Specialty, u.FirstName, s.Status, " +
                    "(SELECT COUNT(*) FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.Status NOT IN ('Cancelled', 'Completed')) AS CurrentProjects "
                    +
                    "FROM STAFF s " +
                    "JOIN USERS u ON s.UserID = u.UserID " +
                    "JOIN ROLES r ON u.RoleID = r.RoleID " +
                    "LEFT JOIN DESIGNERS d ON s.StaffID = d.StaffID " +
                    "WHERE r.RoleName = 'DESIGNER' ";

            sql.append(baseSelect);

            List<String> validCats = new ArrayList<>();
            if (category != null && !category.isEmpty()) {
                String[] cats = category.split(",");
                for (String c : cats) {
                    if (!c.trim().isEmpty())
                        validCats.add(c.trim());
                }

                if (!validCats.isEmpty()) {
                    sql.append("AND (");
                    for (int i = 0; i < validCats.size(); i++) {
                        if (i > 0)
                            sql.append(" OR ");
                        sql.append("(d.Specialty IS NOT NULL AND LOWER(d.Specialty) LIKE ?)");
                    }
                    sql.append(") ");
                }
            }

            System.out.println("Generated SQL: " + sql.toString());

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                if (!validCats.isEmpty()) {
                    for (String c : validCats) {
                        String val = "%" + c.toLowerCase() + "%";
                        System.out.println("Setting Param " + paramIdx + ": " + val);
                        stmt.setString(paramIdx++, val);
                    }
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String status = rs.getString("Status");
                        if (status == null)
                            status = "NULL";

                        System.out.println("MATCH FOUND: " +
                                rs.getString("FirstName") +
                                " | Specialty: " + rs.getString("Specialty") +
                                " | Status: " + status +
                                " | Projects: " + rs.getInt("CurrentProjects"));
                    }
                    if (!found) {
                        System.out.println("NO MATCHES FOUND.");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
