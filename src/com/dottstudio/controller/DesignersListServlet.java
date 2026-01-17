package com.dottstudio.controller;
// Trigger redeploy for JSP updates

import com.dottstudio.util.DBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/designers")
public class DesignersListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String type = request.getParameter("type");
        String category = request.getParameter("category");
        String dateStr = request.getParameter("date");

        StringBuilder json = new StringBuilder("[");

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder();

            // Simplified Select for Unified Designer Model
            // FIX: Updated Column Names to match Schema (d.Status,
            // d.MaxSimultaneousProjects, d.MaxBookingsPerWeek)
            String baseSelect = "SELECT s.StaffID, u.FirstName, u.LastName, u.FirstName || ' ' || u.LastName AS StaffName, " +
                    "u.Email AS StaffEmail, u.Phone AS StaffPhone, r.RoleName AS StaffType, " +
                    "s.JoinDate, " +
                    "d.DesignerID, d.Bio, " +
                    "(SELECT STRING_AGG(sp.SpecialtyName, ', ' ORDER BY sp.SpecialtyName) FROM DESIGNER_SPECIALTIES ds JOIN SPECIALTIES sp ON ds.SpecialtyID = sp.SpecialtyID WHERE ds.DesignerID = d.DesignerID) AS Specialty, " +
                    "d.Status, d.MaxHoursPerWeek AS MaxHours, d.MinHoursGuaranteed AS MinHours, " +
                    "d.MaxSimultaneousProjects AS MaxProjects, d.MaxBookingsPerWeek AS MaxBookings, d.AVAILABLEHOURS AS AvailableHours, "
                    +
                    "(SELECT COUNT(*) FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.Status NOT IN ('Cancelled', 'Completed')) AS CurrentProjects, "
                    +
                    "(SELECT COUNT(*) FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.Status = 'Completed') AS CompletedProjects "
                    +
                    "FROM STAFF s " +
                    "JOIN USERS u ON s.UserID = u.UserID " +
                    "JOIN ROLES r ON u.RoleID = r.RoleID " +
                    "LEFT JOIN DESIGNERS d ON s.StaffID = d.StaffID ";

            // Track categories for parameter binding
            java.util.List<String> validCats = new java.util.ArrayList<>();

            if ("all".equalsIgnoreCase(type)) {
                // Admin dashboard view might use this
                sql.append(baseSelect);
                sql.append("ORDER BY u.FirstName");
            } else {
                // Customer Booking View
                sql.append(baseSelect)
                        .append("WHERE r.RoleName = 'DESIGNER' ");

                // Category Filtering (Supports CSV: "Modern,Industrial")
                if (category != null && !category.isEmpty()) {
                    String[] cats = category.split(",");
                    for (String c : cats) {
                        String trimmed = c.trim();
                        if (!trimmed.isEmpty()) {
                            validCats.add(trimmed);
                        }
                    }

                    if (!validCats.isEmpty()) {
                        sql.append("AND (");
                        for (int i = 0; i < validCats.size(); i++) {
                            if (i > 0)
                                sql.append(" OR ");
                            // Updated to query Bridge Table
                            sql.append("EXISTS (SELECT 1 FROM DESIGNER_SPECIALTIES ds JOIN SPECIALTIES sp ON ds.SpecialtyID = sp.SpecialtyID WHERE ds.DesignerID = d.DesignerID AND LOWER(sp.SpecialtyName) LIKE ?)");
                        }
                        sql.append(") ");
                    }
                }

                    if (dateStr != null && !dateStr.isEmpty()) {
                         // Schedule Availability Check (Hybrid Mode)
                         // 1. If entries exist for this designer, they MUST be available on this date.
                         // 2. If NO entries exist for this designer (legacy), they are shown (Opt-out vs Opt-in)
                         sql.append(" AND (");
                         sql.append(" EXISTS (SELECT 1 FROM DESIGNER_SCHEDULE ds WHERE ds.DesignerID = d.DesignerID AND ds.AvailableDate = TO_DATE(?, 'YYYY-MM-DD') AND ds.IsAvailable = '1') ");
                         sql.append(" OR NOT EXISTS (SELECT 1 FROM DESIGNER_SCHEDULE ds WHERE ds.DesignerID = d.DesignerID) ");
                         sql.append(") ");

                        // Time Filtering - If Time is also Provided (Flow B)
                        String timeStr = request.getParameter("time");
                        if (timeStr != null && !timeStr.isEmpty()) {
                            sql.append(
                                    "AND NOT EXISTS (SELECT 1 FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.BookingTime = ? AND TO_CHAR(b.BookingDate, 'YYYY-MM-DD') = ? AND b.Status != 'Cancelled') ");
                        }
                    }

                    sql.append("ORDER BY u.FirstName");
                }
    
                System.out.println("DesignersListServlet SQL: " + sql.toString()); // Debug Log
    
                try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                    int paramIdx = 1;
                    if (!"all".equalsIgnoreCase(type)) {
                        // Category Filter (Multi-Support)
                        if (!validCats.isEmpty()) {
                            for (String c : validCats) {
                                String val = "%" + c.toLowerCase() + "%"; // Handle casing here
                                stmt.setString(paramIdx++, val);
                            }
                        }
    
                        if (dateStr != null && !dateStr.isEmpty()) {
                            // Bind Date for Availability Check
                            stmt.setString(paramIdx++, dateStr);

                            // Time Filter
                            String timeStr = request.getParameter("time");
                            if (timeStr != null && !timeStr.isEmpty()) {
                                stmt.setString(paramIdx++, timeStr);
                                stmt.setString(paramIdx++, dateStr);
                            }
                        }
                    }

                try (ResultSet rs = stmt.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        // Check Fullness if date provided
                        if (dateStr != null && !dateStr.isEmpty()) {
                            if (isFullyBooked(conn, rs.getInt("DesignerID"), dateStr)) {
                                continue;
                            }
                        }

                        if (!first)
                            json.append(",");
                        first = false;

                        String name = rs.getString("StaffName");
                        String firstName = rs.getString("FirstName");
                        String lastName = rs.getString("LastName");
                        String role = rs.getString("StaffType");
                        String specialty = rs.getString("Specialty");
                        String status = rs.getString("Status"); // From d.Status
                        String phone = rs.getString("StaffPhone");
                        java.sql.Date joinDate = rs.getDate("JoinDate");
                        String bio = rs.getString("Bio");
                        int currentProjects = rs.getInt("CurrentProjects");
                        int completedProjects = rs.getInt("CompletedProjects");
                        int designerId = rs.getInt("DesignerID");

                        // Aliases used in SQL Select
                        int maxHours = rs.getInt("MaxHours");
                        int minHours = rs.getInt("MinHours");
                        int maxProjects = rs.getInt("MaxProjects");
                        int maxBookings = rs.getInt("MaxBookings");

                        if (rs.wasNull()) { // Handle defaults if null
                            maxHours = 40;
                            minHours = 0;
                            maxProjects = 5;
                            maxBookings = 20;
                        }

                        if (specialty == null)
                            specialty = "";
                        if (status == null)
                            status = "Active"; // Default fallback

                        if (bio == null)
                            bio = "";

                        String availableHoursJson = rs.getString("AvailableHours");
                        String workHoursStart = "09:00";
                        String workHoursEnd = "17:00";
                        String availDetails = "Not Set";

                        // Parse simple JSON: {"start":"...", "end":"...", "details":"..."}
                        if (availableHoursJson != null && availableHoursJson.startsWith("{")) {
                            // Helper extraction would be better, but quick parsing here:
                            // We assume format is relatively consistent from ApproveDesignerServlet
                            int sIdx = availableHoursJson.indexOf("\"start\":\"");
                            if (sIdx > -1) {
                                int endIdx = availableHoursJson.indexOf("\"", sIdx + 9);
                                if (endIdx > -1)
                                    workHoursStart = availableHoursJson.substring(sIdx + 9, endIdx);
                            }
                            int eIdx = availableHoursJson.indexOf("\"end\":\"");
                            if (eIdx > -1) {
                                int endIdx = availableHoursJson.indexOf("\"", eIdx + 7);
                                if (endIdx > -1)
                                    workHoursEnd = availableHoursJson.substring(eIdx + 7, endIdx);
                            }
                            int dIdx = availableHoursJson.indexOf("\"details\":\"");
                            if (dIdx > -1) {
                                int endIdx = availableHoursJson.indexOf("\"", dIdx + 11);
                                if (endIdx > -1)
                                    availDetails = availableHoursJson.substring(dIdx + 11, endIdx);
                            }
                        }

                        // Determine availability output (Dates prioritized, then hours)
                        // This block is for "availability": [...] array which frontend uses
                        // But we also need to send workHoursStart/End separately

                        StringBuilder availArray = new StringBuilder("[");

                        // Logic:
                        // 1. If we have manual text (details), we PREFER that.
                        // 2. If the time is just the default 9-5, and we have manual text, HIDE the
                        // time.
                        // 3. If the time is CUSTOM (e.g. 10-2), SHOW the time AND the manual text.

                        String timeStr = workHoursStart + " - " + workHoursEnd;
                        boolean isDefaultTime = "09:00".equals(workHoursStart) && "17:00".equals(workHoursEnd);
                        boolean hasDetails = availDetails != null && !availDetails.isEmpty()
                                && !"Not Set".equals(availDetails);

                        boolean showTime = true;
                        if (isDefaultTime && hasDetails) {
                            showTime = false;
                        }

                        boolean commaNeeded = false;
                        if (showTime) {
                            availArray.append("\"").append(timeStr).append("\"");
                            commaNeeded = true;
                        }

                        if (hasDetails) {
                            if (commaNeeded)
                                availArray.append(",");
                            availArray.append("\"").append(escape(availDetails)).append("\"");
                        }
                        availArray.append("]");

                        json.append("{")
                                .append("\"id\":").append(rs.getInt("StaffID")).append(",")
                                .append("\"designerId\":").append(designerId).append(",")
                                .append("\"name\":\"").append(escape(name)).append("\",")
                                .append("\"firstName\":\"").append(escape(firstName)).append("\",")
                                .append("\"lastName\":\"").append(escape(lastName)).append("\",")
                                .append("\"role\":\"").append(escape(role)).append("\",")
                                .append("\"email\":\"").append(escape(rs.getString("StaffEmail"))).append("\",")
                                .append("\"phone\":\"").append(escape(phone)).append("\",")
                                .append("\"joinDate\":\"").append(joinDate != null ? joinDate.toString() : "")
                                .append("\",")
                                .append("\"bio\":\"").append(escape(bio)).append("\",")
                                .append("\"specialty\":\"").append(escape(specialty)).append("\",")
                                .append("\"status\":\"").append(escape(status)).append("\",")
                                .append("\"currentProjects\":").append(currentProjects).append(",")
                                .append("\"completedProjects\":").append(completedProjects).append(",")
                                .append("\"maxHours\":").append(maxHours).append(",")
                                .append("\"minHours\":").append(minHours).append(",")
                                .append("\"maxProjects\":").append(maxProjects).append(",")
                                .append("\"maxBookings\":").append(maxBookings).append(",")
                                .append("\"upcomingDates\":").append(getUpcomingDatesJson(conn, designerId)).append(",")
                                .append("\"workHoursStart\":\"").append(escape(workHoursStart)).append("\",")
                                .append("\"workHoursEnd\":\"").append(escape(workHoursEnd)).append("\",")
                                .append("\"availability\":").append(availArray.toString()).append(",")
                                .append("\"version\":\"v2\"")
                                .append("}");
                    }
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

    private String getUpcomingDatesJson(Connection conn, int designerId) {
        StringBuilder sb = new StringBuilder("[");
        // PostgreSQL: Use CURRENT_DATE and LIMIT
        String sql = "SELECT DISTINCT AvailableDate FROM DESIGNER_SCHEDULE " +
                     "WHERE DesignerID = ? AND AvailableDate >= CURRENT_DATE AND IsAvailable = '1' " +
                     "ORDER BY AvailableDate ASC LIMIT 3";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, designerId);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        sb.append(",");
                    first = false;
                    java.sql.Date d = rs.getDate("AvailableDate");
                    sb.append("\"").append(d.toString()).append("\"");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log but don't fail the whole request
        }
        sb.append("]");
        return sb.toString();
    }

    private boolean isFullyBooked(Connection conn, int designerId, String date) throws SQLException {
        // 1. Max capacity check (Simple: 8 bookings hard limit per day)
        String countSql = "SELECT COUNT(*) FROM BOOKINGS WHERE DesignerID = ? AND TO_CHAR(BookingDate, 'YYYY-MM-DD') = ? AND Status != 'Cancelled'";
        try (PreparedStatement s = conn.prepareStatement(countSql)) {
            s.setInt(1, designerId);
            s.setString(2, date);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    return r.getInt(1) >= 8;
                }
            }
        }
        return false;
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
