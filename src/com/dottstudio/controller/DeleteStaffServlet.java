package com.dottstudio.controller;

import com.dottstudio.util.DBConnection;
import com.dottstudio.model.User;
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

@WebServlet("/admin/delete-staff")
public class DeleteStaffServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // 1. Check Admin Session
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        // Allow based on session role (matches Dashboard logic)
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRoleName());

        if (!isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"success\": false, \"message\": \"Unauthorized\"}");
            return;
        }

        // 2. Get Target Param (This is StaffID from the UI)
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

            // 3. Lookup UserID from StaffID
            int targetUserId = -1;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT UserID FROM STAFF WHERE StaffID = ?")) {
                stmt.setInt(1, targetStaffId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        targetUserId = rs.getInt("UserID");
                    }
                }
            }

            if (targetUserId == -1) {
                out.print("{\"success\": false, \"message\": \"Staff member not found.\"}");
                return;
            }

            // 4. Prevent Self-Deletion (Check against UserID)
            if (targetUserId == currentUser.getId()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"You cannot delete your own account while logged in.\"}");
                return;
            }

            // 5. Perform Deletion with Manual Casualty (Transaction)
            conn.setAutoCommit(false);

            try {
                // A. Resolve DesignerID (if any)
                int designerId = -1;
                try (PreparedStatement s = conn
                        .prepareStatement("SELECT DesignerID FROM DESIGNERS WHERE StaffID = ?")) {
                    s.setInt(1, targetStaffId);
                    try (ResultSet rs = s.executeQuery()) {
                        if (rs.next())
                            designerId = rs.getInt("DesignerID");
                    }
                }

                // B. Delete BOOKING_DESIGNERS (Where this designer is assigned)
                // COMMENTED OUT - Table doesn't exist in final_normalized_schema.sql
                /*
                 * if (designerId != -1) {
                 * try (PreparedStatement s = conn
                 * .prepareStatement("DELETE FROM BOOKING_DESIGNERS WHERE DesignerID = ?")) {
                 * s.setInt(1, designerId);
                 * s.executeUpdate();
                 * }
                 * }
                 */

                // C. Delete DESIGNERS (Profile)
                // Note: If ON DELETE CASCADE is missing, we must do this.
                try (PreparedStatement s = conn.prepareStatement("DELETE FROM DESIGNERS WHERE StaffID = ?")) {
                    s.setInt(1, targetStaffId);
                    s.executeUpdate();
                }

                // D. Delete ADMINS (Profile)
                // COMMENTED OUT - Table doesn't exist in final_normalized_schema.sql (no
                // separate ADMINS table)
                /*
                 * try (PreparedStatement s =
                 * conn.prepareStatement("DELETE FROM ADMINS WHERE StaffID = ?")) {
                 * s.setInt(1, targetStaffId);
                 * s.executeUpdate();
                 * }
                 */

                // E. Delete STAFF
                try (PreparedStatement s = conn.prepareStatement("DELETE FROM STAFF WHERE StaffID = ?")) {
                    s.setInt(1, targetStaffId);
                    s.executeUpdate();
                }

                // F. Delete Customer Bookings (as Customer)
                // Assuming we want to wipe their history if we delete the user.
                // First delete BOOKING_DESIGNERS for those bookings to avoid orphan reference
                // (if Schema doesn't cascade)
                // But BOOKING_DESIGNERS links Booking->Designer. If we delete Booking, that
                // link should go.
                // Let's delete Bookings made BY this user.
                // We must query their IDs first to delete from child table BOOKING_DESIGNERS

                // F. Delete Customer Bookings (as Customer)
                // Assuming we want to wipe their history if we delete the user.
                
                // Get List of BookingIDs made by this user
                // BOOKING_DESIGNERS table does not exist in schema, so we skip that.
                
                // Direct delete from BOOKINGS is fine (other dependencies will cascade if defined)
                try (PreparedStatement s = conn.prepareStatement("DELETE FROM BOOKINGS WHERE CustomerID = ?")) {
                    s.setInt(1, targetUserId);
                    s.executeUpdate();
                }

                // G. Handle Referrals?
                // ReferredByUserID in USERS table. Set to NULL for others who were referred by
                // this user.
                try (PreparedStatement s = conn
                        .prepareStatement("UPDATE USERS SET ReferredByUserID = NULL WHERE ReferredByUserID = ?")) {
                    s.setInt(1, targetUserId);
                    s.executeUpdate();
                }

                // H. Finally Delete USER
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM USERS WHERE UserID = ?")) {
                    stmt.setInt(1, targetUserId);
                    int rows = stmt.executeUpdate();

                    if (rows > 0) {
                        conn.commit();
                        out.print(
                                "{\"success\": true, \"message\": \"Staff member and all related data deleted successfully.\"}");
                    } else {
                        conn.rollback();
                        out.print("{\"success\": false, \"message\": \"Failed to delete user record.\"}");
                    }
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String safeMsg = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ").replace("\r", " ") : "Unknown Error";
            out.print("{\"success\": false, \"message\": \"Database error: " + safeMsg + "\"}");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
