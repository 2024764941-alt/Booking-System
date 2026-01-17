package com.dottstudio.controller;

import com.dottstudio.util.DBConnection;
import java.io.BufferedReader;
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

@WebServlet("/api/bookings")
public class BookingsListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // GET: List all appointments
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Unauthorized\"}");
            return;
        }

        StringBuilder json = new StringBuilder("[");

        try (Connection conn = DBConnection.getConnection()) {
            String sql;
            PreparedStatement stmt;

            // Updated Query for Booking Bridge (Direct Link)
            String baseSql = "SELECT b.BookingID, TO_CHAR(b.BookingDate, 'YYYY-MM-DD') as BookingDateStr, b.BookingTime, b.ServiceType AS BookingCategory, b.Status, b.Notes, TO_CHAR(b.CreatedAt, 'YYYY-MM-DD HH24:MI') as CreatedAtStr, "
                    + "c.FirstName AS CustFirst, c.LastName AS CustLast, c.Email AS CustEmail, c.Phone AS CustPhone, "
                    + "du.FirstName || ' ' || du.LastName AS DesignerFullName "
                    + "FROM BOOKINGS b "
                    + "JOIN USERS c ON b.CustomerID = c.UserID "
                    + "JOIN DESIGNERS d ON b.DesignerID = d.DesignerID "
                    + "JOIN STAFF s ON d.StaffID = s.StaffID "
                    + "JOIN USERS du ON s.UserID = du.UserID ";

            if ("ADMIN".equalsIgnoreCase(user.getRoleName())) {
                sql = baseSql + "ORDER BY b.BookingID DESC";
                stmt = conn.prepareStatement(sql);
            } else if ("DESIGNER".equalsIgnoreCase(user.getRoleName())) {
                // Fetch bookings where this designer is involved
                sql = baseSql + "WHERE s.UserID = ? ORDER BY b.BookingID DESC";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, user.getId());
            } else {
                // Customer
                sql = baseSql + "WHERE b.CustomerID = ? ORDER BY b.BookingID DESC";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, user.getId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    first = false;

                    String custName = rs.getString("CustFirst") + " " + rs.getString("CustLast");
                    String designerName = rs.getString("DesignerFullName");

                    // Simple JSON construction
                    json.append("{")
                            .append("\"id\":").append(rs.getInt("BookingID")).append(",")
                            .append("\"client\":\"").append(escape(custName)).append("\",")
                            .append("\"email\":\"").append(escape(rs.getString("CustEmail"))).append("\",")
                            .append("\"phone\":\"").append(escape(rs.getString("CustPhone"))).append("\",")
                            .append("\"designer\":\"").append(escape(designerName)).append("\",")
                            .append("\"date\":\"").append(escape(rs.getString("BookingDateStr"))).append("\",")
                            .append("\"created\":\"").append(escape(rs.getString("CreatedAtStr"))).append("\",")
                            .append("\"time\":\"").append(escape(rs.getString("BookingTime"))).append("\",")
                            .append("\"notes\":\"").append(escape(rs.getString("Notes"))).append("\",")
                            .append("\"category\":\"").append(escape(rs.getString("BookingCategory"))).append("\",")
                            .append("\"status\":\"").append(escape(rs.getString("Status"))).append("\"")
                            .append("}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + escape(e.getMessage()) + "\"}");
            return;
        }

        json.append("]");
        out.print(json.toString());
    }

    // POST: Create a new appointment
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\": false, \"message\": \"Unauthorized\"}");
            return;
        }

        // Read JSON
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null)
            buffer.append(line);
        String data = buffer.toString();

        String designerName = extract(data, "designer"); // Single name from UI
        String date = extract(data, "date");
        String time = extract(data, "time");
        String notes = extract(data, "notes");

        if (designerName == null || date == null || time == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Missing required fields\"}");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaction

            try {
                // 1. Find DesignerID (UI sends Name)
                int designerId = -1;
                String lookupSql = "SELECT d.DesignerID FROM DESIGNERS d " +
                        "JOIN STAFF s ON d.StaffID = s.StaffID " +
                        "JOIN USERS u ON s.UserID = u.UserID " +
                        "WHERE (u.FirstName || ' ' || u.LastName) = ?";

                try (PreparedStatement lookupStmt = conn.prepareStatement(lookupSql)) {
                    lookupStmt.setString(1, designerName);
                    try (ResultSet rs = lookupStmt.executeQuery()) {
                        if (rs.next()) {
                            designerId = rs.getInt("DesignerID");
                        }
                    }
                }

                if (designerId == -1) {
                    out.print("{\"success\": false, \"message\": \"Designer not found\"}");
                    return;
                }

                // 1.5 CHECK CAPACITY
                String capacitySql = "SELECT d.MaxSimultaneousProjects, " +
                        "(SELECT COUNT(*) FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.Status NOT IN ('Cancelled', 'Completed')) AS CurrentCount "
                        +
                        "FROM DESIGNERS d WHERE d.DesignerID = ?";

                try (PreparedStatement capStmt = conn.prepareStatement(capacitySql)) {
                    capStmt.setInt(1, designerId);
                    try (ResultSet rs = capStmt.executeQuery()) {
                        if (rs.next()) {
                            int max = rs.getInt("MaxSimultaneousProjects");
                            int current = rs.getInt("CurrentCount");

                            if (max > 0 && current >= max) {
                                conn.rollback();
                                out.print(
                                        "{\"success\": false, \"message\": \"Designer has reached their maximum active project capacity.\"}");
                                return;
                            }
                        }
                    }
                }

                // 1.8 CONCURRENCY CHECK (Prevent Double Booking)
                String checkSql = "SELECT COUNT(*) FROM BOOKINGS WHERE DesignerID = ? AND TO_CHAR(BookingDate, 'YYYY-MM-DD') = ? AND BookingTime = ? AND Status != 'Cancelled'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, designerId);
                    checkStmt.setString(2, date);
                    checkStmt.setString(3, time);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            out.print("{\"success\": false, \"message\": \"This time slot is already booked.\"}");
                            return;
                        }
                    }
                }

                String category = extract(data, "category");

                // 2. Insert into BOOKINGS (With DesignerID)
                String sql = "INSERT INTO BOOKINGS (CustomerID, DesignerID, BookingDate, BookingTime, Notes, Status, ServiceType) "
                        +
                        "VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, 'Confirmed', ?)";

                int newBookingId = 0;
                // PostgreSQL standard generated keys
                try (PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, user.getId());
                    stmt.setInt(2, designerId);
                    stmt.setString(3, date);
                    stmt.setString(4, time);
                    stmt.setString(5, notes != null ? notes : "");
                    stmt.setString(6, category != null ? category : ""); // ServiceType

                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next())
                            newBookingId = rs.getInt(1);
                    }
                }

                if (newBookingId != 0) {
                    conn.commit();
                    out.print("{\"success\": true, \"id\": " + newBookingId + "}");
                } else {
                    conn.rollback();
                    out.print("{\"success\": false, \"message\": \"Failed to insert booking\"}");
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String extract(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1)
            return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1)
            return null;
        return json.substring(start, end);
    }
}
