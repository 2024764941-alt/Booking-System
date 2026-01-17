package com.dottstudio.controller;

import com.dottstudio.util.DBConnection;
import com.dottstudio.model.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/remove-staff")
public class RemoveStaffServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // 1. Check Admin Session
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRoleName());

        if (!isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"success\": false, \"message\": \"Unauthorized\"}");
            return;
        }

        // 2. Get Target Param
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Missing ID\"}");
            return;
        }

        int targetStaffId;
        try {
            targetStaffId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid ID format\"}");
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // 3. Prevent Self-Delete
            int currentStaffId = -1;
            try (PreparedStatement s = conn.prepareStatement("SELECT StaffID FROM STAFF WHERE UserID = ?")) {
                s.setInt(1, currentUser.getId());
                var rs = s.executeQuery();
                if (rs.next()) {
                    currentStaffId = rs.getInt("StaffID");
                }
            }

            if (currentStaffId == targetStaffId) {
                out.print("{\"success\": false, \"message\": \"You cannot delete your own account while logged in.\"}");
                return;
            }

            // 4. Perform Deletion with Transaction
            conn.setAutoCommit(false);

            try {
                // Get UserID before deletion
                int targetUserId = -1;
                try (PreparedStatement s = conn.prepareStatement("SELECT UserID FROM STAFF WHERE StaffID = ?")) {
                    s.setInt(1, targetStaffId);
                    try (java.sql.ResultSet rs = s.executeQuery()) {
                        if (rs.next()) {
                            targetUserId = rs.getInt("UserID");
                        }
                    }
                }

                // 2026-01-15 FIX: Handle Blocking Constraints
                
                // Clear ManagerID References
                try (PreparedStatement s = conn.prepareStatement("UPDATE STAFF SET ManagerID = NULL WHERE ManagerID = ?")) {
                    s.setInt(1, targetStaffId);
                    s.executeUpdate();
                }

                // Clear ReferredBy References
                if (targetUserId != -1) {
                    try (PreparedStatement s = conn.prepareStatement("UPDATE USERS SET ReferredByUserID = NULL WHERE ReferredByUserID = ?")) {
                        s.setInt(1, targetUserId);
                        s.executeUpdate();
                    }
                }

                // Delete DESIGNERS table entry (will cascade to DESIGNER_SPECIALTIES if FK set)
                try (PreparedStatement s = conn.prepareStatement("DELETE FROM DESIGNERS WHERE StaffID = ?")) {
                    s.setInt(1, targetStaffId);
                    s.executeUpdate();
                }

                // Delete STAFF table entry (cascade to USERS via FK)
                try (PreparedStatement s = conn.prepareStatement("DELETE FROM STAFF WHERE StaffID = ?")) {
                    s.setInt(1, targetStaffId);
                    s.executeUpdate();
                }

                // Delete USERS table entry
                if (targetUserId != -1) {
                    try (PreparedStatement s = conn.prepareStatement("DELETE FROM USERS WHERE UserID = ?")) {
                        s.setInt(1, targetUserId);
                        s.executeUpdate();
                    }
                }

                // Commit transaction
                conn.commit();
                out.print("{\"success\": true, \"message\": \"Staff member and User account deleted successfully\"}");

            } catch (SQLException ex) {
                // Rollback on error
                conn.rollback();
                ex.printStackTrace();
                String errMsg = ex.getMessage().replace("\"", "\\\"").replace("\n", " ");
                out.print("{\"success\": false, \"message\": \"Database error: " + errMsg + "\"}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            String errMsg = e.getMessage().replace("\"", "\\\"").replace("\n", " ");
            out.print("{\"success\": false, \"message\": \"Database error: " + errMsg + "\"}");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
