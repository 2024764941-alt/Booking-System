package com.dottstudio.controller;

import com.dottstudio.util.DBConnection;
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

@WebServlet("/api/bookings/update")
public class BookingUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String status = request.getParameter("status"); // For update
        String action = request.getParameter("action"); // 'update' or 'delete'

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (idStr == null) {
            out.print("{\"success\": false, \"message\": \"Missing ID\"}");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if ("delete".equals(action)) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM BOOKINGS WHERE BookingID = ?")) {
                    stmt.setInt(1, Integer.parseInt(idStr));
                    stmt.executeUpdate();
                    out.print("{\"success\": true}");
                }
            } else {
                // Update Status
                if (status == null) {
                    out.print("{\"success\": false, \"message\": \"Missing status\"}");
                    return;
                }
                try (PreparedStatement stmt = conn
                        .prepareStatement("UPDATE BOOKINGS SET Status = ? WHERE BookingID = ?")) {
                    stmt.setString(1, status);
                    stmt.setInt(2, Integer.parseInt(idStr));
                    stmt.executeUpdate();
                    out.print("{\"success\": true}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error: " + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            out.print("{\"success\": false, \"message\": \"Invalid ID format\"}");
        }
    }
}
