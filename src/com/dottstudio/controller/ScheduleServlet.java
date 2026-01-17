package com.dottstudio.controller;

import com.dottstudio.util.DBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/schedule")
public class ScheduleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // GET: Fetch availability for a designer (month view or specific date)
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String designerIdStr = request.getParameter("designerId");
        String monthStr = request.getParameter("month"); // Format "YYYY-MM"

        if (designerIdStr == null) {
            out.print("{\"error\":\"Missing designerId\"}");
            return;
        }

        int designerId;
        try {
            designerId = Integer.parseInt(designerIdStr);
        } catch (NumberFormatException e) {
            // In case they passed STAFF ID by mistake, we might need lookup. But let's
            // assume DesignerID provided from UI.
            // If needed, lookup DesignerID from StaffID.
            out.print("{\"error\":\"Invalid ID\"}");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT AvailableDate, StartTime, EndTime, IsAvailable FROM DESIGNER_SCHEDULE WHERE DesignerID = ?");
            if (monthStr != null) {
                sql.append(" AND TO_CHAR(AvailableDate, 'YYYY-MM') = ?");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                stmt.setInt(1, designerId);
                if (monthStr != null) {
                    stmt.setString(2, monthStr);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    StringBuilder json = new StringBuilder("[");
                    boolean first = true;
                    while (rs.next()) {
                        if (!first)
                            json.append(",");
                        json.append("{");
                        json.append("\"date\":\"").append(rs.getDate("AvailableDate")).append("\",");
                        json.append("\"start\":\"").append(rs.getString("StartTime")).append("\",");
                        json.append("\"end\":\"").append(rs.getString("EndTime")).append("\",");
                        json.append("\"available\":").append(rs.getInt("IsAvailable") == 1);
                        json.append("}");
                        first = false;
                    }
                    json.append("]");
                    out.print(json.toString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"error\":\"Database error\"}");
        }
    }

    // POST: Update/Set availability for a date
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Fix: Set content type specifically for errors or success json
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Read Form Parameters
        // Frontend now sends application/x-www-form-urlencoded
        String dIdStr = request.getParameter("designerId");
        String date = request.getParameter("date");
        String availStr = request.getParameter("available");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");

        System.out.println("ScheduleServlet POST: ID=" + dIdStr + " Date=" + date + " Avail=" + availStr);

        if (dIdStr == null || date == null) {
            response.setStatus(400);
            response.getWriter().print("{\"error\":\"Missing parameters\"}");
            return;
        }

        int designerId = Integer.parseInt(dIdStr);
        boolean available = "true".equalsIgnoreCase(availStr);
        int isAvail = available ? 1 : 0;

        // Handle defaults/nulls for time
        if (startTime == null || startTime.isEmpty())
            startTime = "09:00";
        if (endTime == null || endTime.isEmpty())
            endTime = "17:00";

        // DEBUG LOGGING REMOVED

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Enable transaction control
            boolean exists = false;

            // Step 1: Check if record exists
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT Count(*) FROM DESIGNER_SCHEDULE WHERE DesignerID = ? AND AvailableDate = TO_DATE(?, 'YYYY-MM-DD')")) {
                checkStmt.setInt(1, designerId);
                checkStmt.setString(2, date);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        exists = rs.getInt(1) > 0;
                    }
                }
            }

            // Step 2: Update or Insert (Connection is still open, but previous stmt is closed)
            if (exists) {
                // UPDATE
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE DESIGNER_SCHEDULE SET IsAvailable = ?, StartTime = ?, EndTime = ? WHERE DesignerID = ? AND AvailableDate = TO_DATE(?, 'YYYY-MM-DD')")) {
                    updateStmt.setInt(1, isAvail);
                    updateStmt.setString(2, startTime);
                    updateStmt.setString(3, endTime);
                    updateStmt.setInt(4, designerId);
                    updateStmt.setString(5, date);
                    updateStmt.executeUpdate();
                }
            } else {
                // INSERT
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO DESIGNER_SCHEDULE (DesignerID, AvailableDate, IsAvailable, StartTime, EndTime) VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)")) {
                    insertStmt.setInt(1, designerId);
                    insertStmt.setString(2, date);
                    insertStmt.setInt(3, isAvail);
                    insertStmt.setString(4, startTime);
                    insertStmt.setString(5, endTime);
                    insertStmt.executeUpdate();
                }
            } // Close else/if

            conn.commit(); // FORCE COMMIT
            
            response.getWriter().print("{\"success\":true}");
        
        } catch (SQLException e) { // Close try(conn)
            e.printStackTrace();
            // Debug logging removed
            
            response.setStatus(500);
            String errorMsg = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "Unknown Error";
            response.getWriter().print("{\"error\":\"Database Error: " + errorMsg + "\"}");
        }
    }
}


