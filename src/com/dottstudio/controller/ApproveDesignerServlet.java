package com.dottstudio.controller;

import com.dottstudio.util.DBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.dottstudio.model.User;

@WebServlet("/admin/approve-designer")
public class ApproveDesignerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"success\": false, \"message\": \"Unauthorized\"}");
            return;
        }

        String email = request.getParameter("email");
        String specialty = request.getParameter("specialty");
        String availability = request.getParameter("availability");

        // New Designer Management Fields
        String employmentType = request.getParameter("employmentType");
        String status = request.getParameter("status");
        String bio = request.getParameter("bio"); // Retrieve Bio
        String workDays = request.getParameter("workDays");
        String workHoursStart = request.getParameter("workHoursStart");
        String workHoursEnd = request.getParameter("workHoursEnd");
        int maxHoursPerWeek = parseIntOrDefault(request.getParameter("maxHoursPerWeek"), 40);
        int minHoursGuaranteed = parseIntOrDefault(request.getParameter("minHoursGuaranteed"), 0);
        int maxSimultaneousProjects = parseIntOrDefault(request.getParameter("maxSimultaneousProjects"), 5);
        int maxBookingsPerWeek = parseIntOrDefault(request.getParameter("maxBookingsPerWeek"), 20);

        // Optional new user fields
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String password = request.getParameter("password");

        String role = request.getParameter("role"); // "DESIGNER" or "ADMIN"
        if (role == null || role.isEmpty()) {
            role = "DESIGNER"; // Default
        }
        role = role.toUpperCase();

        // Validation removed - specialty and availability can be added later via edit

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Get RoleID
            int targetRoleId = -1;
            try (PreparedStatement s = conn.prepareStatement("SELECT RoleID FROM ROLES WHERE RoleName = ?")) {
                s.setString(1, role);
                try (ResultSet rs = s.executeQuery()) {
                    if (rs.next())
                        targetRoleId = rs.getInt("RoleID");
                }
            }
            if (targetRoleId == -1)
                throw new SQLException("Invalid Role: " + role);

            // 2. Get/Create User
            int userId = -1;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT UserID FROM USERS WHERE Email = ?")) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("UserID");
                        // Update existing user role
                        try (PreparedStatement up = conn
                                .prepareStatement("UPDATE USERS SET RoleID = ? WHERE UserID = ?")) {
                            up.setInt(1, targetRoleId);
                            up.setInt(2, userId);
                            up.executeUpdate();
                        }
                    } else {
                        // Create User
                        if (firstName == null || password == null) {
                            conn.rollback();
                            out.print(
                                    "{\"success\": false, \"message\": \"User not found. Provide details to create.\"}");
                            return;
                        }
                        String insertUser = "INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, Phone, RoleID) VALUES (?, ?, ?, ?, 'N/A', ?)";
                        // PostgreSQL standard
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertUser, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                            insertStmt.setString(1, email);
                            insertStmt.setString(2, password); // Should hash in prod
                            insertStmt.setString(3, firstName);
                            insertStmt.setString(4, (lastName != null) ? lastName : "");
                            insertStmt.setInt(5, targetRoleId);
                            insertStmt.executeUpdate();
                            try (ResultSet genKeys = insertStmt.getGeneratedKeys()) {
                                if (genKeys.next())
                                    userId = genKeys.getInt(1);
                            }
                        }
                    }
                }
            }

            // 3. Ensure STAFF entry exists
            int staffId = -1;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT StaffID FROM STAFF WHERE UserID = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        staffId = rs.getInt("StaffID");
                    } else {
                        // NEW SCHEMA: Use StaffType instead of Position
                        try (PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO STAFF (UserID, StaffType) VALUES (?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                            ins.setInt(1, userId);
                            ins.setString(2, role); // ADMIN or DESIGNER
                            ins.executeUpdate();
                            try (ResultSet genKeys = ins.getGeneratedKeys()) {
                                if (genKeys.next())
                                    staffId = genKeys.getInt(1);
                            }
                        }
                    }
                }
            }

            // 4. Insert/Update Subclass (DESIGNERS / ADMINS)
            if ("DESIGNER".equals(role)) {
                // Check if exists in DESIGNERS
                boolean exists = false;
                int designerId = -1;

                try (PreparedStatement chk = conn
                        .prepareStatement("SELECT DesignerID FROM DESIGNERS WHERE StaffID = ?")) {
                    chk.setInt(1, staffId);
                    try (ResultSet rs = chk.executeQuery()) {
                        if (rs.next()) {
                            exists = true;
                            designerId = rs.getInt("DesignerID");
                        }
                    }
                }

                String empCat = (employmentType != null && employmentType.toUpperCase().startsWith("FULL")) ? "FULL"
                        : "PART";

                // Combine Time Fields into JSON for AvailableHours column
                // Schema has no separate Start/End columns, so we pack them.
                String safeAvail = (availability != null ? availability.replace("\"", "'") : "");
                String combinedAvail = "{\"start\":\"" + (workHoursStart != null ? workHoursStart : "09:00") + "\", " +
                        "\"end\":\"" + (workHoursEnd != null ? workHoursEnd : "17:00") + "\", " +
                        "\"details\":\"" + safeAvail + "\"}";

                if (exists) {
                    // Update DESIGNERS Table - NEW SCHEMA
                    String updateSql = "UPDATE DESIGNERS SET PrimarySpecialty=?, Bio=?, EmploymentType=?, Status=?, MaxHoursPerWeek=?, MinHoursGuaranteed=?, MaxSimultaneousProjects=?, MaxBookingsPerWeek=?, AvailableHours=? WHERE DesignerID=?";

                    try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                        up.setString(1, specialty != null ? specialty : "");
                        up.setString(2, bio != null ? bio : "");
                        up.setString(3, empCat.equals("FULL") ? "FULL_TIME" : "PART_TIME");
                        up.setString(4, status != null ? status : "Active");
                        up.setInt(5, maxHoursPerWeek);
                        up.setInt(6, minHoursGuaranteed);
                        up.setInt(7, maxSimultaneousProjects);
                        up.setInt(8, maxBookingsPerWeek);
                        up.setString(9, combinedAvail);
                        up.setInt(10, designerId);
                        up.executeUpdate();
                    }
                } else {
                    // Insert into DESIGNERS Table - NEW SCHEMA
                    String insertSql = "INSERT INTO DESIGNERS (StaffID, PrimarySpecialty, Bio, EmploymentType, Status, MaxHoursPerWeek, MinHoursGuaranteed, MaxSimultaneousProjects, MaxBookingsPerWeek, AvailableHours) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement ins = conn.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                        ins.setInt(1, staffId);
                        ins.setString(2, specialty != null ? specialty : "");
                        ins.setString(3, bio != null ? bio : "");
                        ins.setString(4, empCat.equals("FULL") ? "FULL_TIME" : "PART_TIME");
                        ins.setString(5, status != null ? status : "Active");
                        ins.setInt(6, maxHoursPerWeek);
                        ins.setInt(7, minHoursGuaranteed);
                        ins.setInt(8, maxSimultaneousProjects);
                        ins.setInt(9, maxBookingsPerWeek);
                        ins.setString(10, combinedAvail);

                        ins.executeUpdate();
                        try (ResultSet genKeys = ins.getGeneratedKeys()) {
                            if (genKeys.next())
                                designerId = genKeys.getInt(1);
                        }
                    }
                }

                // Removed separate FULL_TIME_DESIGNERS / PART_TIME_DESIGNERS logic blocks
                
                // Update Bridge Table
                updateDesignerSpecialties(conn, designerId, specialty);

            } else if ("ADMIN".equals(role)) {
                // NEW SCHEMA: No ADMINS table, just update STAFF with StaffType='ADMIN'
                String department = request.getParameter("department");
                if (department == null || department.isEmpty()) {
                    department = "General";
                }

                // Update STAFF record to set StaffType and Department
                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE STAFF SET StaffType = 'ADMIN', Department = ? WHERE StaffID = ?")) {
                    up.setString(1, department);
                    up.setInt(2, staffId);
                    up.executeUpdate();
                }
            }

            conn.commit();
            conn.commit();
            out.print("{\"success\": true, \"message\": \"Saved! Role=" + role + ", Status=" + status + ", MaxProj="
                    + maxSimultaneousProjects + "\"}");

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Database error: " + e.getMessage() + "\"}");
        } finally {
            if (conn != null)
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }

    private int parseIntOrDefault(String val, int def) {
        if (val == null || val.trim().isEmpty())
            return def;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void updateDesignerSpecialties(Connection conn, int designerId, String specialtiesStr) throws SQLException {
        System.out.println(">>> DEBUG: updateDesignerSpecialties called for DesignerID=" + designerId + " Specs=" + specialtiesStr);

        if (specialtiesStr == null || specialtiesStr.trim().isEmpty()) {
            // Clear all if empty
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM DESIGNER_SPECIALTIES WHERE DesignerID = ?")) {
                del.setInt(1, designerId);
                del.executeUpdate();
            }
            return;
        }

        // 1. Clear existing
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM DESIGNER_SPECIALTIES WHERE DesignerID = ?")) {
            del.setInt(1, designerId);
            del.executeUpdate();
        }

        // 2. Insert new
        String[] specs = specialtiesStr.split(",");
        for (String specName : specs) {
            specName = specName.trim();
            if (specName.isEmpty()) continue;

            // Find ID
            int specId = -1;
            try (PreparedStatement find = conn.prepareStatement("SELECT SpecialtyID FROM SPECIALTIES WHERE SpecialtyName = ?")) {
                find.setString(1, specName);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) {
                        specId = rs.getInt("SpecialtyID");
                    }
                }
            }

            // If ID not found, Insert it
            if (specId == -1) {
                System.out.println(">>> DEBUG: Specialty '" + specName + "' not found. Creating new...");
                try (PreparedStatement createSpec = conn.prepareStatement(
                        "INSERT INTO SPECIALTIES (SpecialtyName, Description) VALUES (?, 'Auto-created')",
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    createSpec.setString(1, specName);
                    createSpec.executeUpdate();
                    try (ResultSet gk = createSpec.getGeneratedKeys()) {
                        if (gk.next()) {
                            specId = gk.getInt(1);
                        }
                    }
                }
            }

            // If ID found (or created), insert into Bridge
            if (specId != -1) {
                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO DESIGNER_SPECIALTIES (DesignerID, SpecialtyID, ProficiencyLevel, YearsExperience) VALUES (?, ?, 'INTERMEDIATE', 0)")) {
                    ins.setInt(1, designerId);
                    ins.setInt(2, specId);
                    ins.executeUpdate();
                    System.out.println(">>> DEBUG: Linked Designer " + designerId + " to Specialty " + specName + " (" + specId + ")");
                }
            } else {
                 System.out.println(">>> DEBUG: Failed to resolve/create specialty: " + specName);
            }
        }
    }
}
